package com.jjv360.skadivm.logic

import android.content.Context
import com.jjv360.skadivm.utils.extractZip
import java.io.File


class VMRunner(val ctx: Context) {

    /** Get current device architecture */
//    val architecture : String
//        get() {
//
//            // Get system arch
//            val arch = System.getProperty("os.arch") ?: ""
//
//            // Convert to our format
//            if (arch.startsWith("armeabi")) return "arm"
//            if (arch.startsWith("arm64")) return "aarch64"
//            if (arch.startsWith("x86_64")) return "x86_64"
//            return "unknown_arch"
//
//        }

    /** Get Qemu resource path */
    private val qemuResourcePath
        get() = File(ctx.cacheDir, "qemu-assets-v9.1-2")

    /** Get list of supported architectures for emulation */
    fun getEmulationArchitectures(): ArrayList<String> {

        // Got from the list of extracted jni libs
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
    private fun getQemuBinaryPath(arch: String): File {
        return File(ctx.applicationInfo.nativeLibraryDir, "libqemu-system-$arch.so")
    }

    /** Extract Qemu */
    private fun extractQemuAssets() {

        // Check if Qemu has been extracted already
        if (qemuResourcePath.exists())
            return

        // Create folder
        println("Extracting Qemu...")
        if (!qemuResourcePath.mkdirs())
            throw Exception("Unable to create temporary folder for Qemu resources.")

        // Get InputStream to the qemu lib zip file
        val qemuZipInputStream = ctx.assets.open("qemu-assets.zip")

        // Extract it
        extractZip(qemuZipInputStream, qemuResourcePath)

    }

    /** C++ function to run Qemu */
//    private external fun runQemu(workingDir: String, qemuBinary: String, cmdline: String, lineIn: (line: String) -> Unit): Int

    /** Start the VM */
    fun start() {

        // Load the C++ code
//        System.loadLibrary("skadivm")

        // Extract Qemu assets
        extractQemuAssets()

        // Run it
        val qemuBinaryPath = getQemuBinaryPath("x86_64")
        println("Qemu assets: $qemuResourcePath")
        println("Starting Qemu at: $qemuBinaryPath")
//        val exitCode = runQemu(qemuResourcePath.absolutePath, qemuBinaryPath.absolutePath, "--help") {
//            println("Qemu: $it")
//        }
        val builder = ProcessBuilder()
//        builder.command("sh", "-c", "set")
//        builder.command(qemuBinaryPath.absolutePath, "--sandbox", "on,obsolete=deny,elevateprivileges=deny,resourcecontrol=deny", "--help")
        builder.command("${ctx.applicationInfo.nativeLibraryDir}/libtestme.so")
        builder.directory(qemuResourcePath)
        builder.environment()["HOME"] = ctx.cacheDir.absolutePath
        builder.environment()["SHELL"] = "/bin/sh"

        // Start the process
        val process = builder.start()

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
            println("Qemu: $it")
        }

        // Print error
        process.errorStream.bufferedReader().forEachLine {
            println("Qemu error: $it")
        }

        // Wait for Qemu to exit
        val exitCode = process.waitFor()
        println("Qemu exited with code $exitCode")

    }

}