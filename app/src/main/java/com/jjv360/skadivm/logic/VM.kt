package com.jjv360.skadivm.logic

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.charleskorn.kaml.Yaml
import com.jjv360.skadivm.qemu.QemuQMPMonitor
import com.jjv360.skadivm.qemu.QemuQueryVNCResponse
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
        get() = props.getOrDefault("isInstalled", "") != ""
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

    /** Stop the VM */
    fun stop() {

        // Stop if already stopped
        if (!isRunning)
            return

        // Interrupt thread
        thread?.interrupt()
        thread?.stop()
        thread = null

    }

    /** Delete this VM */
    fun delete() {
        manager.deleteVM(this)
    }

    /** Current error */
    var error: Throwable? = null

    /** Current overlay status. If non-blank, the overlay should show this over the UI. */
    var overlayStatus = ""

    /** Overlay substatus, usually the output from a command */
    var overlaySubStatus = ""

    /** Access to the main thread */
    val mainThreadHandler = Handler(Looper.getMainLooper())

    /** While Qemu is running, this contains the QMP interface. */
    var qemuInterface: QemuQMPMonitor? = null

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

        // Create command runner
        val runner = VMCommandRunner(this)

        // Catch errors
        try {

            // Start
            runner.start()

            // Run each op
            for (op in ops)
                runner.runCommand(op)

            // Show toast
            mainThreadHandler.post {
                Toast.makeText(ctx, "$name has closed.", Toast.LENGTH_LONG).show()
            }

        } catch (err: Throwable) {

            // Failed!
            err.printStackTrace()
            error = err
            overlayStatus = err.message ?: "An unknown error occurred."

            // Show toast
            mainThreadHandler.post {
                Toast.makeText(ctx, "$name error: ${err.message}", Toast.LENGTH_LONG).show()
            }

        }

        // Cleanup
        println("[VM $id] Stopped.")
        runner.finish()

        // When thread is done, remove it
        thread = null
        overlayStatus = ""
        overlaySubStatus = ""

    }

    /** VNC connection info */
    var vncInfo : QemuQueryVNCResponse? = null

}
