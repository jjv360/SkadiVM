package com.jjv360.skadivm.logic

import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.jjv360.skadivm.commands.VMCommand
import com.jjv360.skadivm.services.MonitorService
import com.jjv360.skadivm.utils.ArgumentTokenizer
import okhttp3.internal.notifyAll
import okhttp3.internal.wait
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/** Runs a set of commands in context of a VM. This should be called from a background thread. */
class VMCommandRunner(val vm : VM) {

    /** Access to the main thread */
    val mainThreadHandler = Handler(Looper.getMainLooper())

    /** Executed commands */
    val executedCommands = mutableListOf<VMCommand>()

    /** Start running */
    fun start() {

        // Register built-in commands


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

        // Get command runner and clone it
        val executor = VMCommand.get(cmd)?.clone() ?: throw Exception("Unknown command: $cmd")

        // Store it
        executedCommands.add(executor)

        // Run command
        executor.run(this, cmd, args)

    }

    /** Cleanup after all commands have finished */
    fun finish() {

        // Clean up commands
        for (cmd in executedCommands)
            cmd.finish(this)

    }

    /** Execute a process */
    fun executeProcess(workingDir: File, exe: File, args: List<String>): Process {

        // Build process
        val builder = ProcessBuilder()
        builder.directory(workingDir)
        builder.command(exe.absolutePath, *args.toTypedArray())
        builder.redirectErrorStream(true)

        // Add our lib directory to the linker directories so it can find dynamic libs
        val existingLibPath = System.getenv("LD_LIBRARY_PATH")
        val ourLibPath = vm.ctx.applicationInfo.nativeLibraryDir
        builder.environment()["LD_LIBRARY_PATH"] = "$ourLibPath:$existingLibPath"

        // Start the process
        println("Executing: $exe $args")
        val process = builder.start()

        // Ensure that if our process dies, then so does this process
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                if (!process.isAlive) return
                process.destroyForcibly()
            }
        })

        // Done
        return process

    }

    /** Execute a process and listen for each output line, and return the exit code. */
    fun executeProcessWithLines(workingDir: File, exe: File, args: List<String>, onLine: (line: String) -> Unit = {}): Int {

        // Start the process
        val process = executeProcess(workingDir, exe, args)

        // Read input stream
        process.inputStream.bufferedReader().forEachLine {
            onLine(it)
        }

        // Wait for process to exit
        return process.waitFor()

    }

}
