package world.hachimi.app

import java.io.File

interface Platform {
    val name: String
    val platformVersion: String
    fun getCacheDir(): File
    fun getDataDir(): File
    fun openUrl(url: String)
}

expect fun getPlatform(): Platform