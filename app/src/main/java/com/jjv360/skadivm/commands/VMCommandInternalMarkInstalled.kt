package com.jjv360.skadivm.commands

import com.jjv360.skadivm.logic.VMCommandRunner

/** Marks the VM as having finished installation */
class VMCommandInternalMarkInstalled : VMCommand() {

    /** ID */
    override val id = "internal:markInstalled"

    /** Return true if command is supported */
    override fun canRun(cmd: String): Boolean {
        return cmd == "internal:markInstalled"
    }

    /** Execute */
    override fun run(runner: VMCommandRunner, cmd: String, args: List<String>) {
        runner.vm.isInstalled = true
    }

}