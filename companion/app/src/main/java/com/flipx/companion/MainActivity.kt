package com.flipx.companion

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.Display
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import rikka.shizuku.Shizuku

/** FlipX Control Centre (by DashOps): status + one-tap desktop, Linux, OpenCode. */
class MainActivity : Activity() {

    private lateinit var dot: TextView
    private lateinit var statusBody: TextView
    private lateinit var result: TextView

    private val permListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        runOnUiThread {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                say("Shizuku granted - control online")
                DisplayService.start(this)
            } else {
                say("Shizuku denied - resolution buttons need it")
            }
            refresh()
        }
    }
    private val binderListener = Shizuku.OnBinderReceivedListener { runOnUiThread { refresh() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(permListener)
        Shizuku.addBinderReceivedListenerSticky(binderListener)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 40, 48, 40)
            setBackgroundColor(0xFF0B1220.toInt())
        }

        root.addView(TextView(this).apply {
            text = "FlipX Control Centre"
            textSize = 26f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(0xFFEAEFF7.toInt())
        })
        root.addView(TextView(this).apply {
            text = "by DashOps"
            textSize = 14f
            setTextColor(0xFFF5A524.toInt())
            setPadding(0, 0, 0, 24)
        })

        // Status card.
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 28, 32, 28)
            setBackgroundColor(0xFF141D33.toInt())
        }
        dot = TextView(this).apply { textSize = 30f; setPadding(0, 0, 24, 0) }
        statusBody = TextView(this).apply { textSize = 14f; setTextColor(0xFFEAEFF7.toInt()) }
        card.addView(dot)
        card.addView(statusBody)
        root.addView(card)

        result = TextView(this).apply {
            textSize = 13f
            typeface = Typeface.MONOSPACE
            setTextColor(0xFF8A94A6.toInt())
            setPadding(0, 20, 0, 12)
            text = "Ready."
        }
        root.addView(result)

        section(root, "Connection") {
            add(tile("Grant Shizuku", R.drawable.ic_shield, Gravity.CENTER)) { grant() }
        }
        section(root, "Display") {
            add(tile("Desktop Mode", R.drawable.ic_desktop)) { bg { apply() } }
            add(tile("Reset Screen", R.drawable.ic_reset)) { bg { reset() } }
        }
        section(root, "Linux") {
            add(tile("Linux Desktop", R.drawable.ic_terminal)) {
                termux(TermuxCtl.GUISTART, false, "Linux starting - open Termux:X11")
            }
            add(tile("Kill Linux", R.drawable.ic_kill)) {
                termux(TermuxCtl.GUIKILL, true, "Kill sent - layout kept")
            }
        }
        section(root, "Agent") {
            add(tile("OpenCode", R.drawable.ic_code, Gravity.CENTER)) {
                termux(TermuxCtl.OPENCODE, false, "Opening OpenCode")
            }
        }

        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        refresh()
        if (Sh.granted()) DisplayService.start(this)
    }

    override fun onDestroy() {
        try {
            Shizuku.removeRequestPermissionResultListener(permListener)
        } catch (_: Throwable) {
        }
        try {
            Shizuku.removeBinderReceivedListener(binderListener)
        } catch (_: Throwable) {
        }
        super.onDestroy()
    }

    private fun section(root: LinearLayout, title: String, fill: GridLayout.() -> Unit) {
        root.addView(TextView(this).apply {
            text = title.uppercase()
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(0xFFF5A524.toInt())
            setPadding(0, 28, 0, 12)
        })
        val grid = GridLayout(this).apply {
            columnCount = 2
            useDefaultMargins = true
        }
        grid.fill()
        root.addView(grid)
    }

    private fun GridLayout.tile(label: String, icon: Int, grav: Int = Gravity.START or Gravity.CENTER_VERTICAL): Button {
        return Button(this@MainActivity, null, android.R.attr.borderlessButtonStyle).apply {
            text = label
            textSize = 17f
            gravity = grav
            setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
            compoundDrawablePadding = 28
            setPadding(36, 40, 36, 40)
            setTextColor(0xFFEAEFF7.toInt())
            setBackgroundColor(0xFF1B2742.toInt())
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(0, 0, 16, 16)
            }
        }.also { addView(it) }
    }

    private fun bg(fn: () -> Unit) {
        Thread {
            fn()
            runOnUiThread { refresh() }
        }.start()
    }

    private fun say(msg: String) {
        result.text = msg
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun termux(script: String, background: Boolean, msg: String) {
        if (TermuxCtl.run(this, script, background)) say(msg)
        else say("Termux unreachable - enable 'Allow external apps' in Termux")
    }

    private fun grant() {
        val ready = try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
        if (!ready) {
            say("Start the Shizuku service first (open Shizuku app)")
            return
        }
        try {
            Shizuku.requestPermission(0)
        } catch (t: Throwable) {
            say("Grant failed to send: ${t.message}")
        }
    }

    private fun apply() {
        if (!Sh.granted()) {
            runOnUiThread { say("Grant Shizuku first") }
            return
        }
        val code = Sh.applyDesktop(externalIds())
        runOnUiThread { say(if (code == 0) "Desktop layout applied" else "Apply failed ($code)") }
    }

    private fun reset() {
        if (!Sh.granted()) {
            runOnUiThread { say("Grant Shizuku first") }
            return
        }
        Sh.resetAll(allIds())
        runOnUiThread { say("Back to native") }
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
        val ok = Sh.granted()
        dot.text = if (ok) "\u25CF" else "\u25CB"
        dot.setTextColor(if (ok) 0xFF34D17B.toInt() else 0xFFF0524F.toInt())
        val ids = try {
            getSystemService(DisplayManager::class.java).displays.joinToString { it.displayId.toString() }
        } catch (_: Throwable) {
            "?"
        }
        statusBody.text = "Shizuku: ${if (ok) "granted" else "NOT granted"}\nDisplays: [$ids]\nPhone 1080x1920/190 - Monitor 1920x1080/190"
    }
}
