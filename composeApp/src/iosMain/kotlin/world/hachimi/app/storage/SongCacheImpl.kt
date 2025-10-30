package world.hachimi.app.storage

import io.github.vinceglb.filekit.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import platform.Foundation.NSFileManager
import world.hachimi.app.getPlatform
import world.hachimi.app.logging.Logger
import world.hachimi.app.model.SongDetailInfo

@OptIn(ExperimentalForeignApi::class)
class SongCacheImpl : SongCache {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    val cacheDir = getPlatform().getCacheDir().resolve("song_caches").also {
        NSFileManager.defaultManager.createDirectoryAtURL(it.nsUrl, true, null, null)
    }

    override suspend fun get(key: String): SongCache.Item? = withContext(Dispatchers.IO) {
        val audioFile = cacheDir.resolve(key).takeIf { it.exists() } ?: return@withContext null
        val coverFile = cacheDir.resolve("${key}_cover").takeIf { it.exists() } ?: return@withContext null
        val metadataFile = cacheDir.resolve("${key}_metadata").takeIf { it.exists() } ?: return@withContext null
        val metadata = try {
            val text = metadataFile.readBytes().decodeToString()
            json.decodeFromString<SongDetailInfo>(text)
        } catch (e: Throwable) {
            Logger.w("SongCache", "Failed to decode metadata file, just skip cache", e)
            return@withContext null
        }
        SongCache.Item(
            key = key,
            metadata = metadata,
            audio = Buffer().apply { write(audioFile.readBytes()) },
            cover = Buffer().apply { write(coverFile.readBytes()) }
        )
    }

    override suspend fun save(item: SongCache.Item) = withContext(Dispatchers.IO) {
        val audioFile = cacheDir.resolve(item.key)
        val coverFile = cacheDir.resolve("${item.key}_cover")
        val metadataFile = cacheDir.resolve("${item.key}_metadata")

        audioFile.write(item.audio.readByteArray())
        coverFile.write(item.cover.readByteArray())
        metadataFile.writeString(json.encodeToString(item.metadata))
    }

    override suspend fun getMetadata(key: String): SongDetailInfo? = withContext(Dispatchers.IO) {
        val metadataFile = cacheDir.resolve("${key}_metadata").takeIf { it.exists() } ?: return@withContext null
        try {
            val text = metadataFile.readBytes().decodeToString()
            json.decodeFromString<SongDetailInfo>(text)
        } catch (e: Throwable) {
            Logger.w("SongCache", "Failed to decode metadata file, just skip cache", e)
            null
        }
    }

    override suspend fun saveMetadata(item: SongDetailInfo) = withContext(Dispatchers.IO) {
        val metadataFile = cacheDir.resolve("${item.displayId}_metadata")
        metadataFile.writeString(json.encodeToString(item))
    }
}