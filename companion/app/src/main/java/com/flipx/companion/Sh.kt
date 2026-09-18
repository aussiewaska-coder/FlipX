package com.flipx.companion

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import rikka.shizuku.Shizuku
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/** Shared desktop layout. Commands execute in a Shizuku user service (shell UID). */
object Sh {
    const val PHONE_SIZE = "1080x1920"
    const val PHONE_DENSITY = "190"
    const val MON_SIZE = "1920x1080"
    const val MON_DENSITY = "190"

    fun granted(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    /** Run a command with shell privileges. Blocking — call off the main thread. */
    fun run(context: Context, vararg args: String): Int {
        if (!granted()) return -1
        val latch = CountDownLatch(1)
        val svc = AtomicReference<IFlipService?>()
        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                try {
                    svc.set(IFlipService.Stub.asInterface(binder))
                } catch (_: Throwable) {
                }
                latch.countDown()
            }

            override fun onServiceDisconnected(name: ComponentName) {
                latch.countDown()
            }

            override fun onBindingDied(name: ComponentName) {
                latch.countDown()
            }
        }
        val uargs = Shizuku.UserServiceArgs(ComponentName(context.packageName, FlipService::class.java.name))
            .daemon(false)
            .processNameSuffix("flipx")
            .debuggable(BuildConfig.DEBUG)
            .versionCode(BuildConfig.VERSION_CODE)
        try {
            Shizuku.bindUserService(uargs, conn)
        } catch (_: Throwable) {
            return -1
        }
        return try {
            if (!latch.await(15, TimeUnit.SECONDS)) return -1
            val s = svc.get() ?: return -1
            try {
                s.runWm(args)
            } catch (_: Throwable) {
                -1
            }
        } catch (_: Throwable) {
            -1
        } finally {
            try {
                Shizuku.unbindUserService(conn)
            } catch (_: Throwable) {
            }
        }
    }

    fun applyDesktop(context: Context, externalIds: List<Int>): Int {
        var code = run(context, "wm", "size", PHONE_SIZE)
        if (code != 0) return code
        code = run(context, "wm", "density", PHONE_DENSITY)
        if (code != 0) return code
        for (id in externalIds) {
            run(context, "wm", "size", MON_SIZE, "-d", id.toString())
            run(context, "wm", "density", MON_DENSITY, "-d", id.toString())
        }
        return 0
    }

    fun resetAll(context: Context, displayIds: List<Int>): Int {
        var last = 0
        val targets = (listOf(null) + displayIds).distinct()
        for (id in targets) {
            val sizeArgs = if (id == null) arrayOf("wm", "size", "reset")
            else arrayOf("wm", "size", "reset", "-d", id.toString())
            val denArgs = if (id == null) arrayOf("wm", "density", "reset")
            else arrayOf("wm", "density", "reset", "-d", id.toString())
            last = run(context, *sizeArgs)
            last = run(context, *denArgs)
        }
        run(context, "settings", "put", "global", "policy_control", "null")
        return last
    }
}
