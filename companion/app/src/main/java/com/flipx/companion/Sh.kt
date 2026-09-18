package com.flipx.companion

import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

/** Shared desktop layout + thin wrapper around Shizuku shell. */
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

    /** Run a command as Shizuku shell. Returns exit code, -1 if Shizuku unavailable. */
    fun run(vararg args: String): Int {
        if (!granted()) return -1
        return try {
            val proc = Shizuku.newProcess(args, null, null)
            // Drain output so small commands never block.
            val drain: (java.io.InputStream) -> Unit = { ins ->
                try {
                    BufferedReader(InputStreamReader(ins)).forEachLine { }
                } catch (_: Throwable) {
                }
            }
            val t1 = Thread { drain(proc.inputStream) }
            val t2 = Thread { drain(proc.errorStream) }
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

    fun applyDesktop(externalIds: List<Int>): Int {
        var code = run("wm", "size", PHONE_SIZE)
        if (code != 0) return code
        code = run("wm", "density", PHONE_DENSITY)
        if (code != 0) return code
        for (id in externalIds) {
            run("wm", "size", MON_SIZE, "-d", id.toString())
            run("wm", "density", MON_DENSITY, "-d", id.toString())
        }
        return 0
    }

    fun resetAll(displayIds: List<Int>): Int {
        var last = 0
        val targets = (listOf(null) + displayIds).distinct()
        for (id in targets) {
            val sizeArgs = if (id == null) arrayOf("wm", "size", "reset")
            else arrayOf("wm", "size", "reset", "-d", id.toString())
            val denArgs = if (id == null) arrayOf("wm", "density", "reset")
            else arrayOf("wm", "density", "reset", "-d", id.toString())
            last = run(*sizeArgs)
            last = run(*denArgs)
        }
        run("settings", "put", "global", "policy_control", "null")
        return last
    }
}
