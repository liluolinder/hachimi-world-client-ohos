package world.hachimi.app.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import world.hachimi.app.logging.Logger
import world.hachimi.app.util.LrcParser

/**
 * UI states, should attach a player
 */
class PlayerUIState() {
    var isFetching by mutableStateOf(false)
    var hasSong by mutableStateOf(false)
    var songId by mutableStateOf<Long?>(null)
    var songDisplayId by mutableStateOf("")
    var isPlaying by mutableStateOf(false)
    var isBuffering by mutableStateOf(false)
    var songTitle by mutableStateOf("")
    var songAuthor by mutableStateOf("")
    var songCoverUrl by mutableStateOf<String?>(null)
    var songDurationSecs by mutableStateOf(0)

    var currentMillis by mutableStateOf(0L)
        private set
    var currentLyricsLine by mutableStateOf(-1)
        private set
    var timedLyricsEnabled by mutableStateOf(false)
        private set
    var lyricsLines by mutableStateOf<List<String>>(emptyList())
        private set
    private var lrcSegments: List<TimedLyricsSegment> = emptyList()

    var downloadProgress by mutableStateOf(0f)

    data class TimedLyricsSegment(
        val startTimeMs: Long,
        val endTimeMs: Long,
        val spans: List<TimedLyricsSpan>
    )

    data class TimedLyricsSpan(
        val startTimeMs: Long,
        val endTimeMs: Long,
        val text: String
    )

    fun updateCurrentMillis(milliseconds: Long) {
        currentMillis = milliseconds

        if (timedLyricsEnabled) {
            val currentLineIndex = lrcSegments.indexOfFirst {
                it.startTimeMs <= milliseconds && milliseconds <= it.endTimeMs
            }
            currentLyricsLine = currentLineIndex
        }
    }

    fun setLyrics(content: String) {
        try {
            val lrcLines = LrcParser.parse(content)
            val result = mutableListOf<TimedLyricsSegment>()
            for ((index, line) in lrcLines.withIndex()) {
                val startTime = line.timestampMs

                val next = lrcLines.getOrNull(index + 1)
                val endTime = next?.timestampMs ?: Long.MAX_VALUE

                val segment = TimedLyricsSegment(
                    startTimeMs = startTime,
                    endTimeMs = endTime,
                    // TODO: Support enhanced lrc later
                    spans = listOf(TimedLyricsSpan(startTime, endTime, line.content))
                )
                result.add(segment)
            }
            this.lrcSegments = result
            lyricsLines = lrcSegments.map { it.spans.first().text }
            timedLyricsEnabled = true
        } catch (e: Exception) {
            Logger.e("player", "Failed to parse lyrics", e)
            lyricsLines = content.lines()
            timedLyricsEnabled = false
        }
    }
}