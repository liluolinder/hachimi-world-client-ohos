package world.hachimi.app

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.browser.customtabs.CustomTabsIntent
import java.io.File
import androidx.core.net.toUri


class AndroidPlatform : Platform {
    override val name: String = "Android"
    override val platformVersion: String = Build.VERSION.SDK_INT.toString()
    override fun getCacheDir(): File {
        return applicationContext.cacheDir
    }

    override fun getDataDir(): File {
        return applicationContext.filesDir
    }

    override fun openUrl(url: String) {
        val intent = CustomTabsIntent.Builder().build()
        intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.launchUrl(applicationContext, url.toUri())
    }
}

private lateinit var applicationContext: Context

fun initializeGlobalContext(context: Context) {
    applicationContext = context
}

actual fun getPlatform(): Platform = AndroidPlatform()