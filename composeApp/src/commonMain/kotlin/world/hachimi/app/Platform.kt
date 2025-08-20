package world.hachimi.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform