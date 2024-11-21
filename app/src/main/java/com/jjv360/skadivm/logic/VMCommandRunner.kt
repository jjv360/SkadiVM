package com.jjv360.skadivm.logic

import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.jjv360.skadivm.services.MonitorService
import com.jjv360.skadivm.utils.ArgumentTokenizer

/** Runs a set of commands in context of a VM. This should be called from a background thread. */
class VMCommandRunner(val vm : VM) {

    /** Statics */
    companion object {

        /** Operations */
        val commands = mapOf<String, (vm: VM, args: List<String>) -> Unit>(
            "echo" to { vm, args -> opEcho(vm, args) },
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

    /** Run a command */
    fun runCommand(command: String) {

        // Split command into arguments, shell style
        val cmdline = ArgumentTokenizer.tokenize(command)
        val cmd = cmdline[0]
        val args = cmdline.subList(1, cmdline.size)
        println("[VM ${vm.id}] Running: $command")

        // Fail if command doesn't exist
        if (!commands.containsKey(cmd))
            throw Exception("Unknown command: $cmd")

        // Run command
        val cmdOp = commands[cmd]!!
        cmdOp(vm, args)

        // Check which command to run
//        if (cmd == "echo")
//            threadOpEcho(args)
//        else if (cmd == "qemu-img")
//            threadOpQemuImg(args)
//        else
//            throw Exception("Unknown command: $cmd")

    }

    /** Cleanup after all commands have finished */
    fun finish() {



    }

}

/** Display overlay on the screen to indicate progress */
fun opEcho(vm: VM, args: List<String>) {

    // Update UI
    val text = args.joinToString(" ")
    vm.overlayStatus = text
    vm.overlaySubStatus = ""

    Thread.sleep(30000)

}