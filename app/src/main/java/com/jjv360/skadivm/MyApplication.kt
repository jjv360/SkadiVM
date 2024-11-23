package com.jjv360.skadivm

import android.app.Application
import com.jjv360.skadivm.commands.VMCommand
import com.jjv360.skadivm.commands.VMCommandDownload
import com.jjv360.skadivm.commands.VMCommandEcho
import com.jjv360.skadivm.commands.VMCommandInternalMarkInstalled
import com.jjv360.skadivm.commands.VMCommandQemuImg
import com.jjv360.skadivm.commands.VMCommandQemuSystem

class MyApplication: Application() {

    /** Called when the application starts up */
    override fun onCreate() {
        super.onCreate()

        // Register VM commands
        VMCommand.register(VMCommandEcho())
        VMCommand.register(VMCommandDownload())
        VMCommand.register(VMCommandQemuImg())
        VMCommand.register(VMCommandInternalMarkInstalled())
        VMCommand.register(VMCommandQemuSystem())

    }

}