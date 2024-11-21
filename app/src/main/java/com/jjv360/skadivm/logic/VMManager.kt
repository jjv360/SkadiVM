package com.jjv360.skadivm.logic

import android.content.Context
import java.io.File
import java.nio.charset.Charset
import java.util.UUID

/** Extend the Context to have a virtualMachinesDir param */
val Context.virtualMachinesDir : File
    get() = File(this.filesDir, "virtual-machines")

/** Manages the database of VMs installed by the user */
class VMManager private constructor() {

    /** Statics */
    companion object {

        /** Get shared instance */
        val shared = VMManager()

    }

    /** Cached VMs */
    private val cachedVMs = arrayListOf<VM>()

    /** Get list of VMs that are installed */
    fun getVirtualMachines(ctx: Context) : Array<VM> {

        // Check if cached VMs exist
        if (cachedVMs.isNotEmpty())
            return cachedVMs.toTypedArray()

        // Go through all files in the virtual machines folder
        val vms = arrayListOf<VM>()
        for (folder in ctx.virtualMachinesDir.listFiles() ?: arrayOf()) {

            // Get item
            val vm = vmFromDirectory(ctx, folder)

            // Add it
            if (vm != null)
                cachedVMs.add(vm)

        }

        // Done
        return cachedVMs.toTypedArray()

    }

    /** Loads a VM from a folder and adds it to the cached VMs list */
    private fun vmFromDirectory(ctx: Context, folder: File): VM? {

        // Catch errors
        try {

            // Stop if the yaml subfile doesn't exist, this is not a VM
            val templateFile = File(folder, "template.yaml")
            if (!templateFile.exists())
                return null

            // Get VM ID, which is the name of the folder
            val id = folder.name

            // Load template
            val templates = VMTemplate.fromYaml(templateFile)
            if (templates.isEmpty())
                throw Exception("The template.yaml file had no template information.")

            // Exists! Create and return it
            val vm = VM(manager = this, id = id, path = folder, template = templates.first(), ctx = ctx.applicationContext)
            return vm

        } catch (err: Throwable) {

            // Load failed
            println("[VMManager] Failed to load VM at $folder")
            err.printStackTrace()
            return null

        }

    }

    /** Create a new VM based on a template */
    fun createVM(ctx: Context, template: VMTemplate): VM {

        // Create ID
        val id = UUID.randomUUID().toString()

        // Ensure folder exists
        val folder = File(ctx.virtualMachinesDir, id)
        folder.mkdirs()

        // Save template to the directory
        val yaml = VMTemplate.toYaml(mapOf("template" to template))
        File(folder, "template.yaml").bufferedWriter(Charset.forName("UTF-8")).use {
            it.write(yaml)
        }

        // Create VM
        val vm = VM(manager = this, path = folder, id = id, template = template, ctx = ctx.applicationContext)
        vm.load()

        // Add to cache
        cachedVMs.add(vm)

        // TODO: Send out broadcast intent that a VM was created

        // Done
        return vm

    }

    /** Delete VM */
    fun deleteVM(vm: VM) {

        // First make sure it's stopped
        vm.stop()

        // Delete all files in this path
        vm.path.deleteRecursively()

        // Remove from cached list
        cachedVMs.remove(vm)

    }

}