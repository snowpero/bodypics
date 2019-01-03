package com.ninis.camera_sample

import android.app.Application
import io.realm.Realm

class ApplicationImpl: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}