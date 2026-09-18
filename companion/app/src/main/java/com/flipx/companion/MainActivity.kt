package com.flipx.companion

import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.Display
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import rikka.shizuku.Shizuku

/** FlipX Control Center: Shizuku status + one-tap desktop, Linux, OpenCode. */
class MainActivity : Activity() {

    private lateinit var status: TextView
    private val permListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        runOnUiThread {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                toast("Shizuku granted")
                DisplayService.start(this)
            } else {
                toast("Shizuku denied - resolution buttons need it")
            }
            refresh()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(permListener)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        status = TextView(this).apply { textSize = 15f }
        layout.addView(status)
        fun btn(label: String, fn: () -> Unit) {
            layout.addView(Button(this).apply {
                text = label
                textSize = 18f
                setOnClickListener { fn() }
            })
        }

        btn("Grant Shizuku permission") { Shizuku.requestPermission(0) }
        btn("Desktop Mode") { bg { apply() } }
        btn("Reset Screen (native)") { bg { reset() } }
        btn("Linux Desktop") { termux(TermuxCtl.GUISTART, false, "Linux starting - open Termux:X11") }
        btn("Kill Linux (keeps layout)") { termux(TermuxCtl.GUIKILL, true, "Kill sent") }
        btn("OpenCode (single click)") { termux(TermuxCtl.OPENCODE, false, "Opening OpenCode") }

        val scroll = ScrollView(this).apply { addView(layout) }
        setContentView(scroll)
    }

    override fun onResume() {
        super.onResume()
        refresh()
        // Keep the auto-apply service alive while the app is used.
        if (Sh.granted()) DisplayService.start(this)
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permListener)
        super.onDestroy()
    }

    private fun bg(fn: () -> Unit) {
        Thread {
            fn()
            runOnUiThread { refresh() }
        }.start()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun termux(script: String, background: Boolean, msg: String) {
        if (TermuxCtl.run(this, script, background)) toast(msg)
        else toast("Termux not reachable - enable 'Allow external apps' in Termux settings")
    }

    private fun apply() {
        if (!Sh.granted()) {
            runOnUiThread { toast("Grant Shizuku first") }
            return
        }
        val code = Sh.applyDesktop(externalIds())
        runOnUiThread { toast(if (code == 0) "Desktop layout applied" else "Failed ($code)") }
    }

    private fun reset() {
        if (!Sh.granted()) {
            runOnUiThread { toast("Grant Shizuku first") }
            return
        }
        Sh.resetAll(allIds())
        runOnUiThread { toast("Back to native") }
    }

    private fun externalIds(): List<Int> {
        return try {
            getSystemService(DisplayManager::class.java).displays
                .map { it.displayId }
                .filter { it != Display.DEFAULT_DISPLAY }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun allIds(): List<Int> {
        return try {
            getSystemService(DisplayManager::class.java).displays.map { it.displayId }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun refresh() {
        val shizuku = if (Sh.granted()) "granted" else "NOT granted"
        val ids = try {
            getSystemService(DisplayManager::class.java).displays.joinToString { it.displayId.toString() }
        } catch (_: Throwable) {
            "?"
        }
        status.text = "Shizuku: $shizuku\nDisplays: [$ids]\nLayout: phone 1080x1920/190 - monitor 1920x1080/190"
    }
}
