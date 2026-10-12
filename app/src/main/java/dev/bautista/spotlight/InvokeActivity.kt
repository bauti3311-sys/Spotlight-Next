package dev.bautista.spotlight

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import dev.bautista.spotlight.overlay.OverlayService

/** A transparent trampoline only; the search UI belongs to WindowManager, not this Activity. */
class InvokeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(0,0)
        if (!Settings.canDrawOverlays(this)) Toast.makeText(this,"Permití la superposición desde Ajustes de Spotlight.",Toast.LENGTH_LONG).show()
        else
            runCatching {
                    ContextCompat.startForegroundService(
                        this,
                        Intent(this, OverlayService::class.java)
                            .putExtra("query", intent.getStringExtra("query")?.take(4000)).putExtra("invocation",intent.getStringExtra("invocation") ?: "touch"),
                    )
                }
                .onFailure {
                    Toast.makeText(
                            this,
                            "Android no permitió abrir Spotlight. Intentá desde la app.",
                            Toast.LENGTH_LONG,
                        )
                        .show()
                }
        finish()
        overridePendingTransition(0,0)
    }
}
