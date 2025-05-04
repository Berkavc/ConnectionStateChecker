package com.berkavc.connectionstatechecker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.berkavc.connectionstatechecker.R
import com.berkavc.connectionstatechecker.receiver.ConnectivityReceiver

class ConnectivityService : Service() {
    private val receiver = ConnectivityReceiver()
    private val channelId = "connectivity_foreground"

    override fun onCreate() {
        super.onCreate()
        registerReceiver(receiver, IntentFilter().apply {
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        })
        startForeground(1, createForegroundNotification())
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    private fun createForegroundNotification(): Notification {
        val channel = NotificationChannel(
            channelId,
            "Connectivity Service",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_icon)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
