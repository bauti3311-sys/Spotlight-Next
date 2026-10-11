package dev.bautista.spotlight.overlay

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import dev.bautista.spotlight.InvokeActivity

class SpotlightTile : TileService() {
    override fun onStartListening() {
        super.onStartListening()
        qsTile?.let {
            it.state = Tile.STATE_ACTIVE
            it.updateTile()
        }
    }

    // The Intent overload is the only platform API on Android 8–13; API 34+ uses PendingIntent.
    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        unlockAndRun {
            val intent =
                Intent(this, InvokeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (Build.VERSION.SDK_INT >= 34)
                startActivityAndCollapse(
                    PendingIntent.getActivity(
                        this,
                        10,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                )
            else {
                @Suppress("DEPRECATION") startActivityAndCollapse(intent)
            }
        }
    }
}
