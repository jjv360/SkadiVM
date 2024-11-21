package com.jjv360.skadivm.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.jjv360.skadivm.logic.VMManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MonitorService : Service() {

    // Coroutine support
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    /** Called when the service is started */
    override fun onCreate() {
        super.onCreate()

        // Create notification channel
        val notificationChannel = NotificationChannel("service", "Background Service", NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)

        // Create notification
        val notification = NotificationCompat.Builder(this, "service")
            .setContentTitle("Virtual machine active")
            .setOngoing(true)
            .build()

        // Get service type
        val svcType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }

        // Show it
        ServiceCompat.startForeground(this, 999, notification, svcType)

        // Start the monitor coroutine
        scope.launch {

            // Run in a loop with a delay
            while (onLoop()) {
                delay(5000)
            }

            // Done
            stopSelf()

        }

    }

    /** Called when a new intent comes in via startService() */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /** Called every few seconds, return true to keep looping or false to quit the service */
    private fun onLoop(): Boolean {

        // Keep going while VMs are active
        for (vm in VMManager.shared.getVirtualMachines(this))
            if (vm.isRunning)
                return true

        // All VMs exited
        return false

    }

    /** Called when the service is destroyed */
    override fun onDestroy() {
        super.onDestroy()

        // Stop the coroutine just in case
        job.cancel()

    }

}