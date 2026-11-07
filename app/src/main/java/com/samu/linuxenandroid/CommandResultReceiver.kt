package com.samu.linuxenandroid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.concurrent.atomic.AtomicInteger

data class CommandResult(
    val label: String,
    val stdout: String?,
    val stderr: String?,
    val exitCode: Int,
    val err: Int,
    val errmsg: String?,
)

class CommandResultReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.getBundleExtra(TermuxBridge.EXTRA_RESULT_BUNDLE)
        val result = CommandResult(
            label = intent.getStringExtra(EXTRA_LABEL).orEmpty(),
            stdout = bundle?.getString(TermuxBridge.RESULT_STDOUT),
            stderr = bundle?.getString(TermuxBridge.RESULT_STDERR),
            exitCode = bundle?.getInt(TermuxBridge.RESULT_EXIT_CODE, -1) ?: -1,
            err = bundle?.getInt(TermuxBridge.RESULT_ERR, -1) ?: -1,
            errmsg = bundle?.getString(TermuxBridge.RESULT_ERRMSG),
        )
        listener?.invoke(result)
    }

    companion object {
        const val ACTION_RESULT = "com.samu.linuxenandroid.action.COMMAND_RESULT"
        const val EXTRA_LABEL = "com.samu.linuxenandroid.extra.LABEL"

        private val requestCodeCounter = AtomicInteger(1)

        @Volatile
        var listener: ((CommandResult) -> Unit)? = null

        fun nextRequestCode(): Int = requestCodeCounter.getAndIncrement()
    }
}
