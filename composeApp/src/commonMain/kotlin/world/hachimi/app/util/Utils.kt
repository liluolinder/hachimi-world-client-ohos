package world.hachimi.app.util

import androidx.compose.runtime.Stable
import io.ktor.http.*

@Stable
fun isValidHttpsUrl(content: String): Boolean {
    try {
        val url = Url(content)
        if (url.protocolOrNull == URLProtocol.HTTPS)  {
            return true
        }
    } catch (_: URLParserException) {
        return false
    }
    return false
}

/**
 * TextField with `singleLine = true` could be bypassed by pasting multiline text.
 */
fun String.singleLined() = replace('\n', ' ').replace('\r', ' ')