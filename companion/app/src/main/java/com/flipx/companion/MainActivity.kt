package com.flipx.companion

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.Display
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import rikka.shizuku.Shizuku

/** FlipX Control Centre (by DashOps): neon control deck + field manual. */
class MainActivity : Activity() {

    companion object {
        const val REPO = "https://github.com/aussiewaska-coder/FlipX"
        const val NEON = 0xFF00F0FF.toInt()
        const val PINK = 0xFFFF2D78.toInt()
        const val AMBER = 0xFFF5A524.toInt()
        const val BG = 0xFF05070F.toInt()
        const val CARD = 0xFF0C1322.toInt()
        const val TILE = 0xFF111A30.toInt()
        const val TEXT = 0xFFEAEFF7.toInt()
        const val MUTED = 0xFF8A94A6.toInt()
        const val GREEN = 0xFF34D17B.toInt()
        const val RED = 0xFFF0524F.toInt()
    }

    private lateinit var dot: TextView
    private lateinit var statusBody: TextView
    private lateinit var result: TextView

    private val permListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        runOnUiThread {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                say("// control online - welcome back, operator")
                DisplayService.start(this)
            } else {
                say("// access denied - the grid needs your blessing")
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
            setBackgroundColor(BG)
        }

        root.addView(TextView(this).apply {
            text = "\u26A1 FLIPX CONTROL CENTRE"
            textSize = 25f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(TEXT)
            setShadowLayer(18f, 0f, 0f, NEON)
        })
        root.addView(TextView(this).apply {
            text = "// by DashOps - phone-as-desktop division"
            textSize = 14f
            setTextColor(AMBER)
            setShadowLayer(10f, 0f, 0f, AMBER)
            setPadding(0, 0, 0, 24)
        })

        // Glowing status card.
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 28, 32, 28)
            background = glowBox(CARD, NEON, 26f)
            elevation = 10f
        }
        dot = TextView(this).apply { textSize = 18f; setPadding(0, 0, 24, 0); gravity = Gravity.CENTER_VERTICAL }
        statusBody = TextView(this).apply { textSize = 12f; setTextColor(TEXT) }
        card.addView(dot)
        card.addView(statusBody)
        root.addView(card)

        result = TextView(this).apply {
            textSize = 13f
            typeface = Typeface.MONOSPACE
            setTextColor(MUTED)
            setPadding(0, 20, 0, 12)
            text = "// awaiting orders_"
        }
        root.addView(result)

        section(root, "// connection", NEON) {
            add(tile("Grant Shizuku", R.drawable.ic_shield, NEON)) { grant() }
        }
        section(root, "// display", NEON) {
            add(tile("Desktop Mode", R.drawable.ic_desktop, NEON)) { bg { apply() } }
            add(tile("Reset Screen", R.drawable.ic_reset, NEON)) { bg { reset() } }
        }
        section(root, "// linux", GREEN) {
            add(tile("Linux Desktop", R.drawable.ic_terminal, GREEN)) {
                termux(TermuxCtl.GUISTART, false, "// debian inbound - open Termux:X11")
            }
            add(tile("Kill Linux", R.drawable.ic_kill, GREEN)) {
                termux(TermuxCtl.GUIKILL, true, "// linux down - layout holds the line")
            }
        }
        section(root, "// agent", PINK) {
            add(tile("OpenCode", R.drawable.ic_code, PINK)) {
                termux(TermuxCtl.OPENCODE, false, "// agent waking up_")
            }
        }

        // Field manual + repo link.
        root.addView(TextView(this).apply {
            text = "// field manual".uppercase()
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(AMBER)
            setShadowLayer(8f, 0f, 0f, AMBER)
            setPadding(0, 28, 0, 12)
        })
        val manual = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 28, 32, 28)
            background = glowBox(CARD, PINK, 26f)
            elevation = 10f
        }
        manual.addView(TextView(this).apply {
            textSize = 13f
            setTextColor(TEXT)
            setLineSpacing(6f, 1f)
            text = "01 // AFTER REBOOT: join any WiFi \u2192 Shizuku app \u2192 START \u2192 open this deck \u2192 DESKTOP MODE. Two taps. The grid remembers nothing - you are its memory.\n" +
                "02 // DAILY OPS: plug monitor \u2192 DESKTOP MODE \u2192 LINUX DESKTOP. KILL LINUX keeps the layout. Nothing resets unless you say so.\n" +
                "03 // GOING DARK: RESET SCREEN returns the phone to native. Reboot also wipes overrides - see 01.\n" +
                "04 // NO WIFI: Settings \u2192 Developer \u2192 Smallest width 909. No tools, no tears."
        })
        val link = TextView(this).apply {
            textSize = 14f
            setPadding(0, 20, 0, 0)
            text = "Full docs + scripts:\n$REPO"
            setTextColor(NEON)
            autoLinkMask = Linkify.WEB_URLS
            movementMethod = LinkMovementMethod.getInstance()
        }
        manual.addView(link)
        root.addView(manual)
        root.addView(TextView(this).apply {
            textSize = 12f
            setTextColor(MUTED)
            gravity = Gravity.CENTER
            setPadding(0, 28, 0, 8)
            text = "// end of line - stay shiny \u26A1"
        })

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

    private fun glowBox(fill: Int, stroke: Int, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(fill)
            setStroke(3, stroke)
            cornerRadius = radius
        }
    }

    private fun section(root: LinearLayout, title: String, glow: Int, fill: GridLayout.() -> Unit) {
        root.addView(TextView(this).apply {
            text = title.uppercase()
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(AMBER)
            setShadowLayer(8f, 0f, 0f, glow)
            setPadding(0, 28, 0, 12)
        })
        val grid = GridLayout(this).apply {
            columnCount = 2
            useDefaultMargins = true
        }
        grid.fill()
        root.addView(grid)
    }

    private fun GridLayout.tile(label: String, icon: Int, glow: Int): Button {
        return Button(this@MainActivity, null, android.R.attr.borderlessButtonStyle).apply {
            text = label
            textSize = 17f
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
            compoundDrawablePadding = 28
            setPadding(36, 44, 36, 44)
            setTextColor(TEXT)
            background = glowBox(TILE, glow, 20f)
            elevation = 6f
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(0, 0, 16, 16)
            }
        }.also { addView(it) }
    }

    private fun GridLayout.add(b: Button, fn: () -> Unit) {
        b.setOnClickListener { fn() }
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
        else say("// termux unreachable - enable 'Allow external apps' in Termux")
    }

    private fun grant() {
        val ready = try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
        if (!ready) {
            say("// start the Shizuku service first (open Shizuku app)")
            return
        }
        try {
            Shizuku.requestPermission(0)
        } catch (t: Throwable) {
            say("// grant failed to send: ${t.message}")
        }
    }

    private fun apply() {
        if (!Sh.granted()) {
            runOnUiThread { say("// grant Shizuku first, operator") }
            return
        }
        val code = Sh.applyDesktop(externalIds())
        runOnUiThread { say(if (code == 0) "// desktop layout engaged" else "// apply failed ($code)") }
    }

    private fun reset() {
        if (!Sh.granted()) {
            runOnUiThread { say("// grant Shizuku first, operator") }
            return
        }
        Sh.resetAll(allIds())
        runOnUiThread { say("// back to native. the grid forgets.") }
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
        if (!::dot.isInitialized) return
        val ok = Sh.granted()
        dot.text = if (ok) "\u25CF" else "\u25CB"
        val glow = if (ok) GREEN else RED
        dot.setTextColor(glow)
        dot.setShadowLayer(14f, 0f, 0f, glow)
        val ids = try {
            getSystemService(DisplayManager::class.java).displays.joinToString { it.displayId.toString() }
        } catch (_: Throwable) {
            "?"
        }
        statusBody.text = "Shizuku: ${if (ok) "granted" else "NOT granted"}\nDisplays: [$ids]\nPhone 1080x1920/190 - Monitor 1920x1080/190"
    }
}
