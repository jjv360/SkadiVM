package com.jjv360.skadivm.logic

import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.jjv360.skadivm.services.MonitorService
import com.jjv360.skadivm.utils.ArgumentTokenizer
import java.io.File

/** Runs a set of commands in context of a VM. This should be called from a background thread. */
class VMCommandRunner(val vm : VM) {

    /** Statics */
    companion object {

        /** Operations */
        val commands = mapOf<String, (runner: VMCommandRunner, args: List<String>) -> Unit>(
            "echo" to { runner, args -> opEcho(runner, args) },
            "qemu-img" to { runner, args -> opQemuImg(runner, args) },
            "internal:markInstalled" to { runner, args -> opInternalMarkInstalled(runner, args) },
        )

    }

    /** Access to the main thread */
    val mainThreadHandler = Handler(Looper.getMainLooper())

    /** Start running */
    fun start() {

        // Start the foreground service to keep this thread running
        mainThreadHandler.post {
            val intent = Intent(vm.ctx, MonitorService::class.java)
            vm.ctx.startForegroundService(intent)
        }

    }

    /** Get value for a variable */
    fun getVar(name: String): String {
        if (name == "system.arch") return "aarch64" // TODO: This should fetch the Qemu arch code that matches this device's cpu
        if (name == "qemu.path") return Qemu(vm.ctx).qemuResourcePath.absolutePath
        if (name == "vm.path") return vm.path.absolutePath
        return vm.template.props?.get(name) ?: ""
    }

    /** Replace vars in a string */
    fun replaceVars(str: String): String {

        // Copy string
        var s = str

        // Loop
        var lastIdx = 0
        while (true) {

            // Find index of ${
            val idx1 = s.indexOf("\${", lastIdx)
            if (idx1 == -1) break
            val idx2 = s.indexOf("}", idx1)
            if (idx2 == -1) break

            // Get var
            val key = s.substring(idx1+2, idx2)
            val value = getVar(key)

            // Replace it
            s = s.substring(0, idx1) + value + s.substring(idx2+1)
            lastIdx = idx1 + value.length

        }

        // Done
        return s

    }

    /** Run a command */
    fun runCommand(command: String) {

        // Replace vars in string
        val commandExpanded = replaceVars(command)
        println("[VM ${vm.id}] Running: $commandExpanded")

        // Split command into arguments, shell style
        val cmdline = ArgumentTokenizer.tokenize(commandExpanded)
        val cmd = cmdline[0]
        val args = cmdline.subList(1, cmdline.size)

        // Fail if command doesn't exist
        if (!commands.containsKey(cmd))
            throw Exception("Unknown command: $cmd")

        // Run command
        val cmdOp = commands[cmd]!!
        cmdOp(this, args)

    }

    /** Cleanup after all commands have finished */
    fun finish() {



    }

    /** Execute a process and listen for each output line, and return the exit code. */
    fun executeProcessWithLines(workingDir: File, exe: File, args: List<String>, onLine: (line: String) -> Unit = {}): Int {

        // Build process
        val builder = ProcessBuilder()
        builder.directory(workingDir)
        builder.command(exe.absolutePath, *args.toTypedArray())

        // Add our lib directory to the linker directories so it can find dynamic libs
        val existingLibPath = System.getenv("LD_LIBRARY_PATH")
        val ourLibPath = vm.ctx.applicationInfo.nativeLibraryDir
        builder.environment()["LD_LIBRARY_PATH"] = "$ourLibPath:$existingLibPath"

        // Start the process
        val process = builder.start()

        // Ensure that if our process dies, then so does this process
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                if (!process.isAlive) return
                process.destroyForcibly()
            }
        })

        // Print output
        process.inputStream.bufferedReader().forEachLine {
            println(onLine(it))
        }

        // Print error
        process.errorStream.bufferedReader().forEachLine {
            println(onLine(it))
        }

        // Wait for process to exit
        return process.waitFor()

    }

}

/** Display overlay on the screen to indicate progress */
fun opEcho(runner: VMCommandRunner, args: List<String>) {

    // Update UI
    val text = args.joinToString(" ")
    runner.vm.overlayStatus = text
    runner.vm.overlaySubStatus = ""

}

/** Run qemu-img */
fun opQemuImg(runner: VMCommandRunner, args: List<String>) {

    // Ensure assets are extracted
    val qemu = Qemu(runner.vm.ctx)
    qemu.extractQemuAssets()

    // Run command
    val exe = File(runner.vm.ctx.applicationInfo.nativeLibraryDir, "libqemu-img.so")
    val exitCode = runner.executeProcessWithLines(qemu.qemuResourcePath, exe, args) {

        // If line has content, show it
        println("qemu-img: $it")
        if (it.isNotBlank()) {
            runner.vm.overlaySubStatus = it
        }

    }

    // Done
    runner.vm.overlaySubStatus = ""

    // Check exit code
    if (exitCode != 0)
        throw Exception("qemu-img failed with exit code $exitCode")

}

/** Marks the VM as having finished installation */
fun opInternalMarkInstalled(runner: VMCommandRunner, args: List<String>) {
    runner.vm.isInstalled = true
}
