package com.jjv360.skadivm.commands

import com.jjv360.skadivm.logic.VMCommandRunner

/** Display overlay on the screen to indicate progress */
class VMCommandEcho : VMCommand() {

    /** ID */
    override val id = "echo"

    /** Return true if command is supported */
    override fun canRun(cmd: String): Boolean {
        return cmd.lowercase() == "echo"
    }

    /** Execute */
    override fun run(runner: VMCommandRunner, cmd: String, args: List<String>) {

        // Update UI
        val text = args.joinToString(" ")
        runner.vm.overlayStatus = text
        runner.vm.overlaySubStatus = ""

    }

}