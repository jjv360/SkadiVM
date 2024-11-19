package com.jjv360.skadivm.utils

import android.content.Context
import java.io.File

/** Manages the database of VMs installed by the user */
class VMManager(private val ctx : Context) {

    /** Cached list of VMs */
    private val _cachedVMs = arrayListOf<VM>()
    private var _fetchedCachedVMs = false

    /** Get list of VMs that are installed */
    val virtualMachines : ArrayList<VM>
        get() {

            // If we've already fetched the list, stop here
            if (_fetchedCachedVMs)
                return _cachedVMs

            // Get all setting keys
            val prefs = ctx.getSharedPreferences("skadivm", Context.MODE_PRIVATE)
            val keys = prefs.all.filter { it.key.startsWith("vm:") }

            // Create VM for each entry
            for (it in keys) {

                // Get values
                val id = it.key
                val path = File(it.value as String)

                // Check if file exists
                if (!path.exists()) {
                    println("[VMManager] VM not found: $path")
                    continue
                }

                // Check if config file exists
                val configFile = File(path, "vm.json")
                if (!configFile.exists() || !configFile.canRead()) {
                    println("[VMManager] Unable to access config object: $path/vm.json")
                    continue
                }

                // Exists! Create and add it
                val vm = VM(ctx, id, path)
                _cachedVMs.add(vm)

            }

            // Done
            _fetchedCachedVMs = true
            return _cachedVMs

        }

}