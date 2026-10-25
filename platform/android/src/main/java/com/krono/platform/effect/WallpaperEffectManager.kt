package com.krono.platform.effect

import android.app.WallpaperManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import com.krono.core.model.ActionDefinition
import com.krono.core.model.WallpaperTarget
import com.krono.core.result.ActionExecutionResult
import com.krono.core.result.ActionExecutionStatus
import com.krono.platform.diagnostic.KronoDiagLog
import java.io.File

class WallpaperEffectManager(private val context: Context) {

    private val wallpaperManager = WallpaperManager.getInstance(context)

    fun executeSetWallpaper(action: ActionDefinition): ActionExecutionResult {
        if (!wallpaperManager.isWallpaperSupported) {
            return ActionExecutionResult(
                actionId = action.actionId,
                status = ActionExecutionStatus.BLOCKED,
                reason = "Wallpaper not supported on this device"
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !wallpaperManager.isSetWallpaperAllowed) {
            return ActionExecutionResult(
                actionId = action.actionId,
                status = ActionExecutionStatus.BLOCKED,
                reason = "Setting wallpaper not allowed by system policy"
            )
        }

        val assetId = action.assetId ?: return ActionExecutionResult(
            actionId = action.actionId,
            status = ActionExecutionStatus.FAILED,
            reason = "Asset ID missing"
        )

        val assetFile = File(File(context.filesDir, "krono_wallpapers"), "$assetId.bin")
        KronoDiagLog.d("KRONO_DIAG", "[WALLPAPER_EFFECT] executeSetWallpaper actionId=${action.actionId} assetId=$assetId target=${action.wallpaperTarget} fileExists=${assetFile.exists()}")
        if (!assetFile.exists() || !assetFile.canRead()) {
            KronoDiagLog.w("KRONO_DIAG", "[WALLPAPER_EFFECT] Wallpaper asset file missing or not readable: ${assetFile.absolutePath}")
            return ActionExecutionResult(
                actionId = action.actionId,
                status = ActionExecutionStatus.BLOCKED,
                reason = "Wallpaper asset file not available: ${assetFile.name}"
            )
        }

        val which = when (action.wallpaperTarget) {
            WallpaperTarget.SYSTEM -> WallpaperManager.FLAG_SYSTEM
            WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
            WallpaperTarget.BOTH, null -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
        }

        return try {
            assetFile.inputStream().use { stream ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val returnId = wallpaperManager.setStream(stream, null, true, which)
                    KronoDiagLog.d("KRONO_DIAG", "[WALLPAPER_EFFECT] wallpaperManager.setStream returnId=$returnId (which=$which)")
                    if (returnId > 0) {
                        ActionExecutionResult(
                            actionId = action.actionId,
                            status = ActionExecutionStatus.APPLIED,
                            evidenceSummary = "Wallpaper set successfully with return ID $returnId"
                        )
                    } else {
                        ActionExecutionResult(
                            actionId = action.actionId,
                            status = ActionExecutionStatus.FAILED,
                            reason = "WallpaperManager returned 0"
                        )
                    }
                } else {
                    val bitmap = BitmapFactory.decodeStream(stream)
                    wallpaperManager.setBitmap(bitmap)
                    KronoDiagLog.d("KRONO_DIAG", "[WALLPAPER_EFFECT] wallpaperManager.setBitmap legacy executed")
                    ActionExecutionResult(
                        actionId = action.actionId,
                        status = ActionExecutionStatus.APPLIED,
                        evidenceSummary = "Wallpaper set successfully (legacy bitmap API)"
                    )
                }
            }
        } catch (e: SecurityException) {
            KronoDiagLog.e("KRONO_DIAG", "[WALLPAPER_EFFECT] SecurityException: ${e.message}", e)
            ActionExecutionResult(
                actionId = action.actionId,
                status = ActionExecutionStatus.BLOCKED,
                reason = "SecurityException: ${e.message}"
            )
        } catch (e: Exception) {
            KronoDiagLog.e("KRONO_DIAG", "[WALLPAPER_EFFECT] Exception: ${e.message}", e)
            ActionExecutionResult(
                actionId = action.actionId,
                status = ActionExecutionStatus.FAILED,
                reason = "Exception: ${e.message}"
            )
        }
    }
}
