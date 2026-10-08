package com.beg.shuttlespot.navigation

/** Route untuk shell utama dan 4 tab di dalamnya. Tab memakai NavHost sendiri (tabNav), terpisah dari NavHost root. */
object MainRoute {
    const val SHELL = "main"

    const val HOME = "home"
    const val ROUTES = "routes"
    const val ANNOUNCEMENTS = "announcements"
    const val PROFILE = "profile"
}
