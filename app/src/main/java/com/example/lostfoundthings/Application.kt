package com.example.lostfoundthings

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("3cd54956-dc64-434f-a951-1f88eb468877")
        MapKitFactory.initialize(this)
    }
}
