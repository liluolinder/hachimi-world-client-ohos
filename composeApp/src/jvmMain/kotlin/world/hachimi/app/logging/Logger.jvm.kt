package world.hachimi.app.logging

import java.util.logging.Level
import java.util.logging.Logger as JavaLogger

actual object Logger {
    val logger: JavaLogger = JavaLogger.getLogger(Logger::class.java.name)

    init {
        logger.level = Level.FINE
    }

    actual inline fun e(
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        if (throwable != null) {
            logger.log(Level.SEVERE, "ERROR: [$tag] $message", throwable)
        } else {
            logger.severe("ERROR: [$tag] $message")
        }
    }

    actual inline fun w(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            logger.log(Level.WARNING, "WARNING: [$tag] $message", throwable)
        } else {
            logger.warning("WARNING: [$tag] $message")
        }
    }

    actual inline  fun d(tag: String, message: String) {
        logger.info("DEBUG: [$tag] $message")
    }

    actual inline fun i(tag: String, message: String) {
        logger.info("INFO: [$tag] $message")
    }
}