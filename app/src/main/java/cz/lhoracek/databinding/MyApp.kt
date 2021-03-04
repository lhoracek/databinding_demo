package cz.lhoracek.databinding

import android.app.Application
import cz.lhoracek.databinding.logging.ErrorReportingTree
import mainActivityModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import timber.log.Timber
import timber.log.Timber.DebugTree

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin()

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(ErrorReportingTree())
        }
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@MyApp)
            modules(
                listOf(
                    mainActivityModule
                )
            )
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKoin()
    }
}