package com.flipx.companion

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Runs inside a Shizuku user service process (shell UID when Shizuku was
 * started via ADB), so Runtime.exec here has shell privileges and can run wm.
 */
class FlipService : Service() {

    private val stub = object : IFlipService.Stub() {
        override fun runWm(args: Array<String>): Int {
            return try {
                val proc = Runtime.getRuntime().exec(args)
                // Drain to avoid blocking on full pipes.
                val t1 = Thread { try { proc.inputStream.copyTo(java.io.OutputStream.nullOutputStream()) } catch (_: Throwable) {} }
                val t2 = Thread { try { proc.errorStream.copyTo(java.io.OutputStream.nullOutputStream()) } catch (_: Throwable) {} }
                t1.start()
                t2.start()
                val code = proc.waitFor()
                t1.join(2000)
                t2.join(2000)
                code
            } catch (_: Throwable) {
                -1
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder = stub.asBinder()
}
