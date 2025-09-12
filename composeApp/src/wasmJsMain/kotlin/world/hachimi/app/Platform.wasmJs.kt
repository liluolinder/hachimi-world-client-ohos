package world.hachimi.app

import io.github.vinceglb.filekit.PlatformFile

class WasmPlatform : Platform {
    override val name: String = "wasm"
    // Get chrome or wasm virtual machine version
    override val platformVersion: String = "1"
    override val variant: String = "${BuildKonfig.BUILD_TYPE}-wasm"
    override fun getCacheDir(): PlatformFile {
        TODO()
    }

    override fun getDataDir(): PlatformFile {
        TODO("Not yet implemented")
    }

    override fun openUrl(url: String) {
        TODO("Not yet implemented")
    }
}

actual fun getPlatform(): Platform = WasmPlatform()