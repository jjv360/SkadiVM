package com.jjv360.skadivm.qemu

import android.content.Context
import com.jjv360.skadivm.utils.extractZip
import java.io.File

/** Interface with the Qemu binaries */
class Qemu(private val ctx: Context) {

    /** Get Qemu resource path */
    val qemuResourcePath
        get() = File(ctx.cacheDir, "qemu-assets-v9.1-5")

    /** Get list of supported architectures for emulation */
    fun getEmulationArchitectures(): ArrayList<String> {

        // Get from the list of extracted jni libs
        val archs = arrayListOf<String>()
        File(ctx.applicationInfo.nativeLibraryDir).listFiles { dir, name ->
            if (!name.startsWith("libqemu-system-")) return@listFiles true
            if (!name.endsWith(".so")) return@listFiles true
            archs.add(name.substring(15 ..< name.length - 3))
            return@listFiles true
        }

        // Done
        return archs

    }

    /** Get Qemu binary path for a specific architecture we want to emulate */
    fun getQemuBinaryPath(arch: String): File {
        return File(ctx.applicationInfo.nativeLibraryDir, "libqemu-system-$arch.so")
    }

    /** Process thread */
    private var thread : Thread? = null

    /** Extract Qemu */
    fun extractQemuAssets() {

        // Check if Qemu has been extracted already
        if (qemuResourcePath.exists())
            return

        // Remove old qemu assets folders
        if (ctx.cacheDir.exists()) {
            for (f in ctx.cacheDir.listFiles() ?: arrayOf()) {
                if (f.name.startsWith("qemu-assets-")) {
                    f.deleteRecursively()
                }
            }
        }

        // Create folder
        println("Extracting Qemu...")
        if (!qemuResourcePath.mkdirs())
            throw Exception("Unable to create temporary folder for Qemu resources.")

        // Get InputStream to the qemu lib zip file
        val qemuZipInputStream = ctx.assets.open("qemu-assets.zip")

        // Extract it
        extractZip(qemuZipInputStream, qemuResourcePath)

    }

    /** Run the Qemu process */
    private fun runQemu(arch: String, args: Array<String>): Process {

        // Extract Qemu assets
        extractQemuAssets()

        // Build process
        val qemuBinaryPath = getQemuBinaryPath(arch)
        val builder = ProcessBuilder()
        builder.command(qemuBinaryPath.absolutePath, *args)
        builder.directory(qemuResourcePath)

        // Add our lib directory to the linker directories so it can find dynamic libs
        val existingLibPath = System.getenv("LD_LIBRARY_PATH")
        val ourLibPath = ctx.applicationInfo.nativeLibraryDir
        builder.environment()["LD_LIBRARY_PATH"] = "$ourLibPath:$existingLibPath"

        // Start the process
        return builder.start()

    }

    /** True if Qemu is currently running */
    val isRunning : Boolean
        get() = thread?.isAlive ?: false

    /** Start the VM */
    fun start(arch: String, qemuFlags: Array<String>): Qemu {

        // Do nothing if already started
        if (isRunning)
            throw Exception("Qemu is already running.")

        // Create thread
        thread = Thread() {
            runThread(arch, qemuFlags)
        }

        // Start thread
        thread!!.start()

        // Chainable
        return this

    }

    /** Stop the VM */
    fun stop() {

        // Stop the thread
        thread?.interrupt()
        thread?.stop()
        thread = null

    }

    /** VM process thread */
    private fun runThread(arch: String, qemuFlags: Array<String>) {

        // Start the process
        println("[Qemu] Assets: $qemuResourcePath")
        println("[Qemu] Binary: ${getQemuBinaryPath(arch)}")
        println("[Qemu] Starting process...")
        val process = runQemu(arch, qemuFlags)

        // Ensure that if our process dies, then so does Qemu
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                if (!process.isAlive) return
                println("Shutting down Qemu since we are being shut down")
                process.destroyForcibly()
            }
        })

        // Print output
        process.inputStream.bufferedReader().forEachLine {
            println("[Qemu]: $it")
        }

        // Print error
        process.errorStream.bufferedReader().forEachLine {
            println("[Qemu]: $it")
        }

        // Wait for Qemu to exit
        val exitCode = process.waitFor()
        println("Qemu exited with code $exitCode")

    }

}