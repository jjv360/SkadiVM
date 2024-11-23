package com.jjv360.skadivm.commands

import com.jjv360.skadivm.qemu.Qemu
import com.jjv360.skadivm.logic.VMCommandRunner
import java.io.File

/** Run qemu-img to create or modify a disk image */
class VMCommandQemuImg : VMCommand() {

    /** ID */
    override val id = "qemu-img"

    /** Return true if command is supported */
    override fun canRun(cmd: String): Boolean {
        return cmd.lowercase() == "qemu-img"
    }

    /** Execute */
    override fun run(runner: VMCommandRunner, cmd: String, args: List<String>) {

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

}