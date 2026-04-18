package com.elsoft.symlogic.utils


enum class OS {
    WINDOWS, LINUX, MACOS, UNKNOWN
}

actual object PlatformUtils {

    val current: OS = System.getProperty("os.name").lowercase().let {
        when {
            it.contains("win") -> OS.WINDOWS
            it.contains("linux") -> OS.LINUX
            it.contains("mac") -> OS.MACOS
            else -> OS.UNKNOWN
        }
    }

    actual fun getProblemSetDir(): String {

        return if (current == OS.WINDOWS) {
            // Windows: AppData/Local/EBookLibrary/Cache
            val appData = System.getenv("LOCALAPPDATA")
            "$appData/SymLogic/problems"
        } else { // Linux or MacOS
            val userHome = System.getProperty("user.home")
            "$userHome/.symlogic/problems"
        }
    }
}