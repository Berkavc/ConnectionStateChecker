package com.berkavc.connectionstatechecker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.berkavc.connectionstatechecker.INTENT_REBOOT
import com.berkavc.connectionstatechecker.SHARED_PREF_ENABLED
import com.berkavc.connectionstatechecker.SharedPreference
import com.berkavc.connectionstatechecker.main.MainActivity

class BootCompleteBroadCastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { ctx ->
            intent?.action?.let {
                val sharedPreference = SharedPreference(ctx)
                if (intent.action == Intent.ACTION_BOOT_COMPLETED && sharedPreference.getValueBoolean(
                        SHARED_PREF_ENABLED, false
                    )
                ) {
                    val mainActivityIntent = Intent()
                    mainActivityIntent.setClassName(
                        context.packageName,
                        MainActivity::class.java.name
                    )
                    mainActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    mainActivityIntent.putExtra(INTENT_REBOOT, true)
                    context.startActivity(mainActivityIntent)
                }
            }
        }
    }
}
