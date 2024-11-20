package com.jjv360.skadivm.utils

import android.content.Context
import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.nio.charset.Charset

/** Represents a runnable VM. */
class VM(

    /** Reference to the VM manager */
    val manager: VMManager,

    /** Context */
    val ctx: Context,

    /** Path to the folder containing the VM files */
    val path: File,

    /** Unique ID */
    val id: String,

    /** Template used to create this VM */
    val template: VMTemplate,

) {

    /** List of properties */
    var props = mutableMapOf<String, String>()

    /** Name of this VM */
    var name: String
        get() = props.getOrDefault("name", template.name)
        set(v) {
            props["name"] = v
            save()
        }

    /** True if this VM has been installed */
    var isInstalled: Boolean
        get() = props.getOrDefault("isInstalled", "") == ""
        set(v) {
            props["isInstalled"] = if (v) "yes" else ""
            save()
        }

    /** Load details */
    fun load() {

        // Stop if props file doesn't exist
        val file = File(path, "props.yaml")
        if (!file.exists())
            return

        // Load props
        val yaml = file.readText(Charset.forName("UTF-8"))
        val props = Yaml.default.decodeFromString<Map<String, String>>(yaml)
        this.props = props.toMutableMap()

    }

    /** Save prop changes */
    @Synchronized
    fun save() {
        val yaml = Yaml.default.encodeToString(props)
        File(path, "props.yaml").bufferedWriter(Charset.forName("UTF-8")).use {
            it.write(yaml)
        }
    }

    /** Activity thread */
    var thread: Thread? = null

    /** Current running state. Running if the thread is active. */
    val isRunning: Boolean
        get() = thread != null

    /** Start the VM */
    fun start() {

        // Stop if already running
        if (isRunning)
            return

        // Create thread
        thread = Thread() { threadStart() }
        thread!!.name = "SkadiVM: id=$id"
        thread!!.start()

    }

    /** Current error */
    var error: Throwable? = null

    /** Current overlay status. If non-blank, the overlay should show this over the UI. */
    var overlayStatus = ""

    /** THREAD: Start the thread */
    private fun threadStart() {

        // Create list of operations
        println("[VM $id] Started.")
        val ops = mutableListOf<String>()

        // If we haven't yet initialized the VM, add init commands
        if (!isInstalled) {

            // Add install commands
            ops.addAll(template.installTasks ?: listOf())

            // Add command to mark as installed
            ops.add("internal:markInstalled")

        }

        // Add run commands
        ops.addAll(template.runTasks)

        // Catch errors
        try {

            // Run each op
            for (op in ops)
                threadRunOp(op)

        } catch (err: Throwable) {

            // Failed!
            err.printStackTrace()
            error = err
            overlayStatus = err.message ?: "An unknown error occurred."

        }

        // When thread is done, remove it
        println("[VM $id] Stopped.")
        thread = null

    }

    /** Run an operation */
    private fun threadRunOp(op: String) {

        // Split command into arguments, shell style
        val cmdline = ArgumentTokenizer.tokenize(op)
        val cmd = cmdline[0]
        val args = cmdline.subList(1, cmdline.size)
        println("[VM $id] Running: $op")

        // Check which command to run
        if (cmd == "echo")
            threadOpEcho(args)
        else
            throw Exception("Unknown command: $cmd")

    }

    /** Echo operation, shows a UI overlay */
    private fun threadOpEcho(args: List<String>) {

        // Update UI
        val text = args.joinToString(" ")
        overlayStatus = text

    }

}