package world.hachimi.app.util

object LrcParser {
    data class LrcLine(
        val timestampMs: Long,
        val content: String,
    )

    fun parse(lrc: String): List<LrcLine> {
        val regex = Regex("""\[(\d+):(\d+)\.(\d+)]\s*(.*)""")
        val result = mutableListOf<LrcLine>()
        val lines = lrc.lines()
        for (line in lines) {
            // Matches
            val matches = regex.find(line)
            if (matches != null) {
                val minutes = matches.groups[1]!!.value.toInt()
                val seconds = matches.groups[2]!!.value.toInt()
                val millisStr = matches.groups[3]!!.value
                val millis = if (millisStr.length == 2) { // h secs
                    millisStr.toInt() * 10
                } else {
                    millisStr.toInt()
                }
                val timestamp = (minutes * 60 + seconds) * 1000L + millis
                val content = matches.groups[4]!!.value.trim()
                result.add(LrcLine(timestamp, content))
            } else {
                error("Invalid time tag: $line")
            }
        }
        return result
    }
}