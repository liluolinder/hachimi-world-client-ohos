package world.hachimi.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import world.hachimi.app.di.appModule

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        initializeGlobalContext(this)

        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }
    }
}