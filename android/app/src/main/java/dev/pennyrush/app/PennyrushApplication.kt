package dev.pennyrush.app

import android.app.Application
import timber.log.Timber

class PennyrushApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
