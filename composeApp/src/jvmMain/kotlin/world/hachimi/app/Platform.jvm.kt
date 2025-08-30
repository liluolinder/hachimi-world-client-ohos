package world.hachimi.app

import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import java.awt.Desktop
import java.io.File
import java.net.URI

object JVMPlatform : Platform {
    override val name: String = "Java"
    override val platformVersion: String = System.getProperty("java.version")

    private val appName = BuildKonfig.APP_PACKAGE_NAME

    override fun getCacheDir(): File {
        return when (hostOs) {
            OS.Windows -> File(System.getenv("LOCALAPPDATA"), "/Cache")
            OS.MacOS -> File(System.getProperty("user.home"), "/Library/Caches")
            OS.Linux -> System.getProperty("XDG_DATA_HOME")?.let { File(it) }
                ?: File(System.getProperty("user.home"), "/.local/cache")
            else -> error("Unsupported os: $hostOs")
        }.resolve(appName).also { if (!it.exists()) it.mkdirs() }
    }

    override fun getDataDir(): File {
        return when (hostOs) {
            OS.Windows -> File(System.getenv("APPDATA"))
            OS.MacOS -> File(System.getProperty("user.home"), "/Library/Application Support")
            OS.Linux -> System.getProperty("XDG_DATA_HOME")?.let { File(it) }
                ?: File(System.getProperty("user.home"), "/.local/share")
            else -> error("Unsupported os: $hostOs")
        }.resolve(appName).also { if (!it.exists()) it.mkdirs() }
    }

    override fun openUrl(url: String) {
        val desktop = Desktop.getDesktop()
        desktop.browse(URI(url))
    }
}

actual fun getPlatform(): Platform = JVMPlatform