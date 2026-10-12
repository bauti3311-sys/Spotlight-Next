package dev.bautista.spotlight

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/** System SAF picker: persisted read grant, no broad storage permission. */
class ContextPickerActivity:ComponentActivity() {
    private val picker=registerForActivityResult(ActivityResultContracts.OpenDocument()){uri->
        if(uri==null){returnToSpotlight();finish();return@registerForActivityResult}
        lifecycleScope.launch {
            try{contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);(application as SpotlightApp).contexts.setDocument(uri)
                returnToSpotlight()
            }catch(e:Exception){android.widget.Toast.makeText(this@ContextPickerActivity,"No se pudo autorizar ese archivo. Probá otra ubicación.",android.widget.Toast.LENGTH_LONG).show()}
            finally{finish()}
        }
    }
    private fun returnToSpotlight(){startActivity(Intent(this,InvokeActivity::class.java).putExtra("query",intent.getStringExtra("query").orEmpty().ifBlank{"contexto"}))}
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);if(savedInstanceState==null)picker.launch(arrayOf("application/pdf","text/*","image/*")+dev.bautista.spotlight.index.OfficeText.types.toTypedArray())}
}
