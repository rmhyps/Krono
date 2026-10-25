package com.krono.app

import android.app.Application
import com.krono.app.hub.KronoRuntimeHub

class KronoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KronoRuntimeHub.getInstance(this)
    }
}
