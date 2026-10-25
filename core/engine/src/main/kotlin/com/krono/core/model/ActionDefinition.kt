package com.krono.core.model

enum class ActionKind {
    SET_WALLPAPER,
    SET_MEDIA_VOLUME
}

enum class WallpaperTarget {
    SYSTEM,
    LOCK,
    BOTH
}

object TargetResource {
    const val AUDIO_MEDIA = "audio.media"
    const val WALLPAPER_SYSTEM = "wallpaper.system"
    const val WALLPAPER_LOCK = "wallpaper.lock"

    fun targetsForWallpaper(target: WallpaperTarget): Set<String> {
        return when (target) {
            WallpaperTarget.SYSTEM -> setOf(WALLPAPER_SYSTEM)
            WallpaperTarget.LOCK -> setOf(WALLPAPER_LOCK)
            WallpaperTarget.BOTH -> setOf(WALLPAPER_SYSTEM, WALLPAPER_LOCK)
        }
    }
}

data class ActionDefinition(
    val actionId: String,
    val kind: ActionKind,
    val assetId: String? = null,
    val wallpaperTarget: WallpaperTarget? = null,
    val volumePercent: Int? = null
) {
    fun requiredTargetResources(): Set<String> {
        return when (kind) {
            ActionKind.SET_MEDIA_VOLUME -> setOf(TargetResource.AUDIO_MEDIA)
            ActionKind.SET_WALLPAPER -> {
                wallpaperTarget?.let { TargetResource.targetsForWallpaper(it) } ?: emptySet()
            }
        }
    }
}
