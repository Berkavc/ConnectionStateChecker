package com.berkavc.connectionstatechecker.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.berkavc.connectionstatechecker.INTENT_REBOOT
import com.berkavc.connectionstatechecker.R
import com.berkavc.connectionstatechecker.SHARED_PREF_ENABLED
import com.berkavc.connectionstatechecker.SharedPreference
import com.berkavc.connectionstatechecker.base.BaseActivity
import com.berkavc.connectionstatechecker.databinding.ActivityMainBinding
import com.berkavc.connectionstatechecker.service.ConnectivityService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {
    override val layoutRes: Int = R.layout.activity_main
    override val viewModel: MainViewModel by viewModels()
    override var viewLifeCycleOwner: LifecycleOwner = this

    private lateinit var sharedPreference: SharedPreference

    private val PERMISSIONS_REQUEST_CODE = 1001

    private val requiredPermissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.INTERNET
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            add(Manifest.permission.BLUETOOTH)
            add(Manifest.permission.BLUETOOTH_ADMIN)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreference = SharedPreference(this)
        super.onCreate(savedInstanceState)
        viewModel.isDeviceRebooted = intent.getBooleanExtra(INTENT_REBOOT, false)
        if (viewModel.isDeviceRebooted) {
            if (sharedPreference.getValueBoolean(SHARED_PREF_ENABLED, false)) {
                startConnectivityService()
            }
        } else {
            stopConnectivityService()
        }
        checkAndRequestPermissions()
    }

    override fun onResume() {
        super.onResume()
        arrangeMainUI()
    }

    private fun arrangeMainUI() {
        binding.switchEnable.setOnCheckedChangeListener(null)
        binding.switchEnable.isChecked = sharedPreference.getValueBoolean(
            SHARED_PREF_ENABLED, false
        )
        binding.switchEnable.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                startConnectivityService()
            } else {
                stopConnectivityService()
            }
            sharedPreference.saveBooleanSynchronized(SHARED_PREF_ENABLED, isChecked)
        }

    }

    private fun checkAndRequestPermissions() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val denied = grantResults.indices.filter { grantResults[it] != PackageManager.PERMISSION_GRANTED }
//            if (denied.isNotEmpty()) {
//                lifecycleScope.launch {
//                    binding.switchEnable.isChecked = false
//                    Toast.makeText(this@MainActivity, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
//                    delay(1250L)
//                    checkAndRequestPermissions()
//                }
//            }
        }
    }

    private fun startConnectivityService() {
        val intent = Intent(this, ConnectivityService::class.java)
        startForegroundService(intent)
    }

    private fun stopConnectivityService() {
        val intent = Intent(this, ConnectivityService::class.java)
        stopService(intent)
    }

    override fun observeViewModel() {}
}