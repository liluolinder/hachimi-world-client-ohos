package world.hachimi.app.util

import androidx.compose.runtime.Stable
import io.ktor.http.URLParserException
import io.ktor.http.URLProtocol
import io.ktor.http.Url

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