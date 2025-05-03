package com.berkavc.connectionstatechecker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ConnectionStateCheckerApp: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}