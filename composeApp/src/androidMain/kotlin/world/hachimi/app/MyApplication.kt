package world.hachimi.app

import android.app.Application
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import world.hachimi.app.di.appModule
import world.hachimi.app.model.GlobalStore

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        initializeGlobalContext(this)

        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }

        val global = inject<GlobalStore>()
        global.value.initialize()
    }
}