package com.flipx.companion

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.IBinder
import android.view.Display

/** Applies the desktop layout whenever a monitor appears. Never resets. */
class DisplayService : Service() {

    private lateinit var displays: DisplayManager
    private val listener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
            if (displayId != Display.DEFAULT_DISPLAY) apply()
        }

        override fun onDisplayRemoved(displayId: Int) {
            // Leaving the layout alone is the rule; Reset is manual.
        }

        override fun onDisplayChanged(displayId: Int) {}
    }

    override fun onCreate() {
        super.onCreate()
        displays = getSystemService(DisplayManager::class.java)
        displays.registerDisplayListener(listener, null)
        startForegroundNotif()
        // Apply immediately in case a monitor is already connected.
        Thread { apply() }.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            displays.unregisterDisplayListener(listener)
        } catch (_: Throwable) {
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun externals(): List<Int> {
        return try {
            displays.displays
                .map { it.displayId }
                .filter { it != Display.DEFAULT_DISPLAY }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun apply() {
        if (!Sh.granted()) return
        Sh.applyDesktop(externals())
    }

    private fun startForegroundNotif() {
        val channelId = "flipx"
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(channelId, "FlipX", NotificationManager.IMPORTANCE_MIN)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
        val notif = Notification.Builder(this, channelId)
            .setContentTitle("FlipX watching displays")
            .setContentText("Auto desktop layout on monitor connect")
            .setSmallIcon(android.R.drawable.stat_sys_data_usb)
            .build()
        startForeground(1, notif)
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, DisplayService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= 26) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Throwable) {
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            DisplayService.start(context)
        }
    }
}
