package com.krono.platform.effect

import com.krono.core.model.ActionDefinition
import com.krono.core.model.ActionKind
import com.krono.core.ports.EffectPort
import com.krono.core.result.ActionExecutionResult

class AndroidEffectAdapter(
    private val audioManager: AudioEffectManager,
    private val wallpaperManager: WallpaperEffectManager,
    private val ringerModeManager: RingerModeEffectManager
) : EffectPort {

    override suspend fun executeAction(action: ActionDefinition): ActionExecutionResult {
        return when (action.kind) {
            ActionKind.SET_MEDIA_VOLUME -> audioManager.executeSetVolume(action)
            ActionKind.SET_WALLPAPER -> wallpaperManager.executeSetWallpaper(action)
            ActionKind.SET_RINGER_MODE -> ringerModeManager.executeSetRingerMode(action)
        }
    }
}
