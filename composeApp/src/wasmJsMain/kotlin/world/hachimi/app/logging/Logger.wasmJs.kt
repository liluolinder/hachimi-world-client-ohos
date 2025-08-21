package world.hachimi.app.logging

actual object Logger {
    actual inline fun e(tag: String, message: String, throwable: Throwable?) {
        println("[ERROR]($tag) $message ${throwable?.stackTraceToString()}")
    }

    actual inline fun w(tag: String, message: String, throwable: Throwable?) {
        println("[WARNING]($tag) $message ${throwable?.stackTraceToString()}")
    }

    actual inline fun d(tag: String, message: String) {
        println("[DEBUG]($tag) $message")
    }

    actual inline fun i(tag: String, message: String) {
        println("[INFO]($tag) $message")
    }

}