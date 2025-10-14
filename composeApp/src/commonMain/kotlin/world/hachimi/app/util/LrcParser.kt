package world.hachimi.app.util

object LrcParser {
    data class LrcLine(
        val minute: Int,
        val second: Int,
        val millisecond: Int,
        val timestampMs: Long,
        val content: String,
    )

    fun parse(lrc: String): List<LrcLine> {
        val regex = Regex("""\[(\d+):(\d+)\.(\d+)]\s*(.*)""")
        val tagRegex = Regex("""\[(.*):(.*)]""")
        val result = mutableListOf<LrcLine>()
        val lines = lrc.lines()
        for (line in lines) {
            if (line.isBlank()) continue

            // Matches
            val matches = regex.find(line)
            if (matches != null) {
                val minutes = matches.groups[1]!!.value.toInt()
                val seconds = matches.groups[2]!!.value.toInt()
                val millisStr = matches.groups[3]!!.value
                val millis = when (millisStr.length) {
                    2 -> millisStr.toInt() * 10
                    3 -> millisStr.toInt()
                    else -> error("Invalid time tag: $line")
                }
                val timestamp = (minutes * 60 + seconds) * 1000L + millis
                val content = matches.groups[4]!!.value.trim()
                result.add(LrcLine(minutes, seconds, millis, timestamp, content))
            } else if (tagRegex.matchEntire(line) != null) {
                // Is tag, just ignore
                continue
            } else {
                error("Invalid time tag: $line")
            }
        }
        return result
    }
}