package com.jjv360.skadivm.commands

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.jjv360.skadivm.logic.Qemu
import com.jjv360.skadivm.logic.QemuQMPMonitor
import com.jjv360.skadivm.logic.VMCommandRunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.StringReader
import java.net.ServerSocket
import java.nio.charset.Charset

/** Run qemu-system-* to launch and manage the VM */
class VMCommandQemuSystem : VMCommand() {

    /** ID */
    override val id = "qemu-system-*"

    /** Return true if command is supported */
    override fun canRun(cmd: String): Boolean {
        return cmd.lowercase().startsWith("qemu-system-")
    }

    /** Store runner */
    var runner: VMCommandRunner? = null

    /** Active process */
    var qemuProcess: Process? = null

    /** Execute */
    override fun run(runner: VMCommandRunner, cmd: String, args: List<String>) {

        // Store it
        this.runner = runner

        // Ensure assets are extracted
        val qemu = Qemu(runner.vm.ctx)
        runner.vm.overlaySubStatus = "Extracting Qemu assets..."
        qemu.extractQemuAssets()

        // Show status
        runner.vm.overlaySubStatus = "Starting Qemu..."

        // Add path to the resources to the command line options
        val updatedArgs = mutableListOf<String>()
        updatedArgs.add("-L")
        updatedArgs.add(qemu.qemuResourcePath.absolutePath)

        // Add VNC params
        updatedArgs.add("-display")
        updatedArgs.add("none")
        updatedArgs.add("-vnc")
        updatedArgs.add(":5900")

        // Use stdio for the QMP connection
        updatedArgs.add("-qmp")
        updatedArgs.add("stdio")

        // Add input args
        updatedArgs.addAll(args)

        // Run command
        val workDir = runner.vm.path
        val exe = File(runner.vm.ctx.applicationInfo.nativeLibraryDir, "lib${cmd}.so")
        println("VM Path: $workDir")
        println("Qemu Resources: ${qemu.qemuResourcePath}")
        println("Qemu Binary: $exe")
        qemuProcess = runner.executeProcess(workDir, exe, updatedArgs)

        // Catch errors
        try {

            // Get reader and writer
            var exitCode = 0
            qemuProcess!!.outputStream.bufferedWriter(Charset.forName("UTF-8")).use { writer ->
                qemuProcess!!.inputStream.bufferedReader(Charset.forName("UTF-8")).use { reader ->

                    // Create QMP interface
                    runner.vm.qemuInterface = QemuQMPMonitor(reader, writer)
                    runner.vm.qemuInterface!!.start()

                    // Active! Move to main thread
                    runner.vm.mainThreadHandler.post {
                        onQemuStarted()
                    }

                    // Receive messages, this blocks until Qemu exits
                    runner.vm.qemuInterface!!.loop()

                    // Get Qemu exit code
                    exitCode = qemuProcess!!.waitFor()

                }
            }

            // Remove interface
            runner.vm.qemuInterface = null

            // Check exit code
            if (exitCode != 0)
                throw Exception("$cmd failed with exit code $exitCode")

        } catch (err: Throwable) {

            // Log it
            println("qemu-system: error: ${err.message}")

            // If we have a replacement error from the monitoring thread, throw that instead since
            // it'll be more descriptive, otherwise just throw this error
            throw qemuMonitorError ?: err

        }

    }

    /** Cleanup when VM exits */
    override fun finish(runner: VMCommandRunner) {

        // Clean up interface in case something threw an error
        runner.vm.qemuInterface = null

        // Remove qemu process
        qemuProcess?.destroyForcibly()
        qemuProcess = null

    }

    /** Stores the error from the Qemu monitor. */
    var qemuMonitorError: Throwable? = null

    /** Called on main thread to continue operation once Qemu is started */
    fun onQemuStarted() = MainScope().launch {

        // Catch errors
        try {

            // Get VNC info
            runner!!.vm.overlaySubStatus = "Querying display..."
            val vnc = runner!!.vm.qemuInterface!!.queryVNC()
            if (vnc.port == 0)
                throw Exception("Qemu did not start the VNC server.")

            // Done
            runner!!.vm.overlayStatus = ""
            runner!!.vm.overlaySubStatus = ""
            runner!!.vm.vncInfo = vnc

        } catch (err: Throwable) {

            // Log it
            println("qemu-system: Failed: ${err.message}")
            qemuMonitorError = err

            // Close the process to end the Qemu monitoring threads
            qemuProcess?.destroyForcibly()

        }

    }

}