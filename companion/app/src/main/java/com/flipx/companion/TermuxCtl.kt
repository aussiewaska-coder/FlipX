package com.flipx.companion

import android.content.Context
import android.content.Intent

/** Fires Termux RUN_COMMAND intents so one tap runs home scripts / OpenCode. */
object TermuxCtl {
    const val TERMUX_PKG = "com.termux"
    const val ACTION_RUN = "com.termux.app.RUN_COMMAND"
    const val EXTRA_PATH = "com.termux.RUN_COMMAND_PATH"
    const val EXTRA_ARGS = "com.termux.RUN_COMMAND_ARGUMENTS"
    const val EXTRA_BG = "com.termux.RUN_COMMAND_BACKGROUND"

    const val GUISTART = "/data/data/com.termux/files/home/guistart"
    const val GUIKILL = "/data/data/com.termux/files/home/guikill"
    const val OPENCODE = "/data/data/com.termux/files/home/.shortcuts/OpenCode"

    fun run(context: Context, script: String, background: Boolean): Boolean {
        return try {
            // RUN_COMMAND always goes to RunCommandService: with BACKGROUND=false
            // it opens the session in the Termux UI itself. (Termux setting
            // "Allow external apps" must be ON or this throws SecurityException.)
            val intent = Intent(ACTION_RUN).apply {
                setClassName(TERMUX_PKG, "$TERMUX_PKG.app.RunCommandService")
                putExtra(EXTRA_PATH, script)
                putExtra(EXTRA_BG, background)
            }
            context.startService(intent)
            true
        } catch (_: Throwable) {
            false
        }
    }
}
