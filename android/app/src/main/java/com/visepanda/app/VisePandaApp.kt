package com.visepanda.app

import android.app.Application
import com.visepanda.core.network.ApiClient

class VisePandaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Network client initialized via lazy singleton
    }
}
