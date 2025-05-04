package com.berkavc.connectionstatechecker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.berkavc.connectionstatechecker.SHARED_PREF_ENABLED
import com.berkavc.connectionstatechecker.SharedPreference
import com.berkavc.connectionstatechecker.startConnectivityService

class BootCompleteBroadCastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { ctx ->
            intent?.action?.let {
                val sharedPreference = SharedPreference(ctx)
                if (intent.action == Intent.ACTION_BOOT_COMPLETED && sharedPreference.getValueBoolean(
                        SHARED_PREF_ENABLED, false
                    )
                ) {
                    ctx.startConnectivityService()
                }
            }
        }
    }
}
