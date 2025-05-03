package com.berkavc.connectionstatechecker.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.core.app.NotificationCompat
import com.berkavc.connectionstatechecker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetAddress

class ConnectivityReceiver : BroadcastReceiver() {
    private val channelId = "connectivity_channel"
    private var wifiState: ConnectionState? = null
    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)

        when (intent.action) {
            WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                val info = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                if (info != null && info.isConnected) {
                    if(wifiState != ConnectionState.CONNECTED) {
                        wifiState = ConnectionState.CONNECTED
                        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        val wifiInfo: WifiInfo = wifiManager.connectionInfo
                        val ssid = wifiInfo.ssid
                        GlobalScope.launch {
                            val ping = getPingMs()
                            val message = context.getString(R.string.wifi_title, ssid, ping)
                            showNotification(
                                context,
                                context.getString(R.string.wifi_connected_title),
                                message,
                                ConnectionRoot.WIFI
                            )
                        }
                    }
                } else {
                    if (wifiState != ConnectionState.DISCONNECTED) {
                        wifiState = ConnectionState.DISCONNECTED
                        showNotification(
                            context,
                            context.getString(R.string.wifi),
                            context.getString(R.string.wifi_disconnected_message),
                            ConnectionRoot.WIFI
                        )
                    }
                }
            }

/*            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val message = when (intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )) {
                    BluetoothAdapter.STATE_ON -> context.getString(R.string.bluetooth_on)
                    BluetoothAdapter.STATE_OFF -> context.getString(R.string.bluetooth_off)
                    else -> return
                }
                showNotification(
                    context,
                    context.getString(R.string.bluetooth_state_changed),
                    message,
                    ConnectionRoot.BLUETOOTH
                )
            }*/

            BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_CONNECTION_STATE,
                    BluetoothAdapter.ERROR
                )
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val name = device?.name ?: context.getString(R.string.unknown)
                val message = when (state) {
                    BluetoothAdapter.STATE_CONNECTED -> context.getString(
                        R.string.bluetooth_connected,
                        name
                    )

                    BluetoothAdapter.STATE_DISCONNECTED -> context.getString(
                        R.string.bluetooth_disconnected,
                        name
                    )

                    else -> return
                }
                showNotification(
                    context,
                    context.getString(R.string.bluetooth_connection_changed),
                    message,
                    ConnectionRoot.BLUETOOTH
                )
            }
        }
    }

    private fun showNotification(
        context: Context,
        title: String,
        message: String,
        connectionRoot: ConnectionRoot
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        val name = "Connectivity Channel"
        val descriptionText = "Notifications for Wi-Fi and Bluetooth changes"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private suspend fun getPingMs(): Int = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("ping", "-c", "1", "8.8.8.8"))
            val reader = process.inputStream.bufferedReader()
            val output = reader.readText()
            process.waitFor()

            // Example ping output line:
            // 64 bytes from 8.8.8.8: icmp_seq=1 ttl=117 time=23.8 ms
            val timeLine = output.lines().find { it.contains("time=") }
            val match = Regex("time=([0-9.]+)").find(timeLine ?: "")
            match?.groupValues?.get(1)?.toFloat()?.toInt() ?: -1
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    enum class ConnectionRoot {
        WIFI,
        BLUETOOTH
    }

    enum class ConnectionState {
        CONNECTED,
        DISCONNECTED
    }
}
