package world.hachimi.app.logging

expect object Logger {
    inline fun e(tag: String, message: String, throwable: Throwable? = null)
    inline fun w(tag: String, message: String, throwable: Throwable? = null)
    inline fun d(tag: String, message: String)
    inline fun i(tag: String, message: String)
}