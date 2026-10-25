package com.krono.platform.effect

import android.app.WallpaperManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import com.krono.core.model.ActionDefinition
import com.krono.core.model.ActionKind
import com.krono.core.ports.CapabilityPort
import com.krono.core.ports.CapabilityStatus
import java.io.File

class AndroidCapabilityManager(
    private val context: Context,
    var isAudioSessionActive: Boolean = false
) : CapabilityPort {

    private val wallpaperManager = WallpaperManager.getInstance(context)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    override fun checkCapability(action: ActionDefinition): CapabilityStatus {
        return when (action.kind) {
            ActionKind.SET_WALLPAPER -> {
                if (!wallpaperManager.isWallpaperSupported) {
                    return CapabilityStatus.Blocked("Wallpaper not supported")
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !wallpaperManager.isSetWallpaperAllowed) {
                    return CapabilityStatus.Blocked("Wallpaper change not permitted by device policy")
                }
                val assetId = action.assetId
                if (assetId != null) {
                    val file = File(File(context.filesDir, "krono_wallpapers"), "$assetId.bin")
                    if (!file.exists()) {
                        return CapabilityStatus.Blocked("Wallpaper asset file missing: $assetId")
                    }
                }
                CapabilityStatus.Ready
            }
            ActionKind.SET_MEDIA_VOLUME -> {
                if (audioManager == null) {
                    return CapabilityStatus.Blocked("AudioManager not available")
                }
                CapabilityStatus.Ready
            }
            ActionKind.SET_RINGER_MODE -> {
                if (audioManager == null) {
                    return CapabilityStatus.Blocked("AudioManager not available")
                }
                CapabilityStatus.Ready
            }
        }
    }
}
