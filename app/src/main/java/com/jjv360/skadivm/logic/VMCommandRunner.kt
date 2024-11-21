package com.jjv360.skadivm.logic

import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.jjv360.skadivm.services.MonitorService
import com.jjv360.skadivm.utils.ArgumentTokenizer
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/** Runs a set of commands in context of a VM. This should be called from a background thread. */
class VMCommandRunner(val vm : VM) {

    /** Statics */
    companion object {

        /** Operations */
        val commands = mapOf<String, (runner: VMCommandRunner, args: List<String>) -> Unit>(
            "echo"                      to { runner, args -> opEcho(runner, args) },
            "qemu-img"                  to { runner, args -> opQemuImg(runner, args) },
            "internal:markInstalled"    to { runner, args -> opInternalMarkInstalled(runner, args) },
            "download"                  to { runner, args -> opDownload(runner, args) },
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
    val workDir = runner.vm.path
    val exe = File(runner.vm.ctx.applicationInfo.nativeLibraryDir, "libqemu-img.so")
    val exitCode = runner.executeProcessWithLines(workDir, exe, args) {

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

/** Download a file */
fun opDownload(runner: VMCommandRunner, args: List<String>) {

    // Start download
    runner.vm.overlaySubStatus = "Downloading..."
    val file = File(runner.vm.path, args[0])
    val url = URL(args[1])

    // Ensure directory exists
    file.parentFile!!.mkdirs()

    // Open URL connection
    val urlConnection = url.openConnection()

    // Get file size
    val totalSize = urlConnection.contentLengthLong

    // Open stream
    urlConnection.getInputStream().use { inputStream ->

        // Open connection to file
        FileOutputStream(file).use { outputStream ->

            // Pipe it across
            val buffer = ByteArray(1024*1024)
            var amountLoaded = 0L
            while (true) {

                // Load next chunk
                val amount = inputStream.read(buffer)
                if (amount == -1)
                    break

                // Write it out
                outputStream.write(buffer, 0, amount)

                // Update UI
                amountLoaded += amount
                if (totalSize == -1L)
                    runner.vm.overlaySubStatus = "Downloading ${bytesToHumanReadableSize(amountLoaded)}"
                else
                    runner.vm.overlaySubStatus = "Downloading ${bytesToHumanReadableSize(amountLoaded)} of ${bytesToHumanReadableSize(totalSize)}"

                // Log
//                println("[VM ${runner.vm.id}] ${runner.vm.overlaySubStatus}")

            }

        }
    }

    // Done
    runner.vm.overlaySubStatus = ""
    println("[VM ${runner.vm.id}] Download complete")

}

// Convert byte size to readable string
fun bytesToHumanReadableSize(bytes: Long) = when {
    bytes >= 1024*1024*1024 -> "%.2f GB".format(bytes.toDouble() / (1024*1024*1024))
    bytes >= 1024*1024 -> "%.2f MB".format(bytes.toDouble() / (1024*1024))
    bytes >= 1024 -> "%.2f KB".format(bytes.toDouble() / (1024))
    else -> "$bytes B"
}
