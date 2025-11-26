package org.pracainzynierska.sportbooking

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform