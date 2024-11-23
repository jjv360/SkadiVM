package com.jjv360.skadivm.commands

import com.jjv360.skadivm.logic.VMCommandRunner

/** Represents a command that can be run by a VM template when setting up or running the VM. */
abstract class VMCommand: Cloneable {

    /** Statics */
    companion object {

        /** Registered commands */
        val commands = mutableListOf<VMCommand>()

        /** Register a command */
        fun register(cmd: VMCommand) {

            // Remove any existing ones with a matching ID
            commands.removeIf { it.id == cmd.id }

            // Add this one
            commands.add(cmd)

        }

        /** Fetch the executor for the specified command */
        fun get(cmd: String): VMCommand? {
            return commands.find { it.canRun(cmd) }
        }

    }

    /** Unique command ID */
    open val id = ""

    /** Return true if this command can be processed */
    abstract fun canRun(cmd: String): Boolean

    /** Execute the command */
    abstract fun run(runner: VMCommandRunner, cmd: String, args: List<String>)

    /** Called when the VM exits to clean up */
    open fun finish(runner: VMCommandRunner) {}

    /** Make clone function public */
    public override fun clone(): VMCommand {
        return super.clone() as VMCommand
    }

}