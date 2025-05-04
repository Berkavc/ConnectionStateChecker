package com.berkavc.connectionstatechecker.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.core.app.NotificationCompat
import com.berkavc.connectionstatechecker.R
import com.berkavc.connectionstatechecker.getPingMs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConnectivityReceiver : BroadcastReceiver() {
    private val channelId = "connectivity_channel"
    private var wifiState: ConnectionState? = null
    private var cellularState: ConnectionState? = null
    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)
        when (intent.action) {
            WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                val info = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                if (info != null && info.isConnected) {
                    if (wifiState != ConnectionState.CONNECTED) {
                        wifiState = ConnectionState.CONNECTED
                        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                        val wifiInfo: WifiInfo = wifiManager.connectionInfo
                        val ssid = wifiInfo.ssid
                        CoroutineScope(Dispatchers.Main).launch {
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

            WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                val message = when (intent.getIntExtra(
                    WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN
                )) {
                    WifiManager.WIFI_STATE_ENABLED -> context.getString(R.string.wifi_on)
                    WifiManager.WIFI_STATE_DISABLED -> context.getString(R.string.wifi_off)
                    else -> return
                }
                showNotification(
                    context,
                    context.getString(R.string.wifi),
                    message,
                    ConnectionRoot.WIFI
                )
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

            ConnectivityManager.CONNECTIVITY_ACTION -> {
                val cm =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = cm.activeNetworkInfo

                if (activeNetwork != null && activeNetwork.isConnected) {
                    if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                        if (cellularState != ConnectionState.CONNECTED) {
                            cellularState = ConnectionState.CONNECTED
                            val wifiManager =
                                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                            val wifiInfo: WifiInfo = wifiManager.connectionInfo
                            val ssid = wifiInfo.ssid
                            CoroutineScope(Dispatchers.Main).launch {
                                val ping = getPingMs()
                                val message = context.getString(R.string.cellular_title, ssid, ping)
                                showNotification(
                                    context,
                                    context.getString(R.string.cellular_connected),
                                    message,
                                    ConnectionRoot.CELLULAR
                                )
                            }
                        } else if (cellularState != ConnectionState.DISCONNECTED) {
                            cellularState = ConnectionState.DISCONNECTED
                            showNotification(
                                context,
                                context.getString(R.string.cellular),
                                context.getString(R.string.cellular_disconnected),
                                ConnectionRoot.CELLULAR
                            )
                        }
                    } else if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                        if (wifiState != ConnectionState.CONNECTED) {
                            wifiState = ConnectionState.CONNECTED
                            val wifiManager =
                                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                            val wifiInfo: WifiInfo = wifiManager.connectionInfo
                            val ssid = wifiInfo.ssid
                            CoroutineScope(Dispatchers.Main).launch {
                                val ping = getPingMs()
                                val message = context.getString(R.string.wifi_title, ssid, ping)
                                showNotification(
                                    context,
                                    context.getString(R.string.wifi_connected_title),
                                    message,
                                    ConnectionRoot.WIFI
                                )
                            }
                        } else if (wifiState != ConnectionState.DISCONNECTED) {
                            wifiState = ConnectionState.DISCONNECTED
                            showNotification(
                                context,
                                context.getString(R.string.wifi),
                                context.getString(R.string.wifi_disconnected_message),
                                ConnectionRoot.WIFI
                            )
                        }
                    }
                } else {
                    showNotification(
                        context,
                        context.getString(R.string.network_disconnected),
                        context.getString(R.string.no_active_network),
                        ConnectionRoot.GENERIC
                    )
                }
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
        val descriptionText = "Notifications for Connection changes"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    enum class ConnectionRoot {
        WIFI,
        BLUETOOTH,
        CELLULAR,
        GENERIC
    }

    enum class ConnectionState {
        CONNECTED,
        DISCONNECTED
    }
}
