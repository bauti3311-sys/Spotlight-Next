package dev.bautista.spotlight
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
/** User initiated, finite recognition through Android's installed speech activity. */
class VoiceActivity:ComponentActivity(){
 private var launched=false
 private val recognition=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->val text=result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim()?.take(3000);val query=if(result.resultCode==RESULT_OK && !text.isNullOrBlank())intent.getStringExtra("prefix").orEmpty().take(500)+text else intent.getStringExtra("fallback").orEmpty().take(4000);startActivity(Intent(this,InvokeActivity::class.java).putExtra("query",query));finish()}
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);launched=savedInstanceState?.getBoolean("launched")==true;if(!launched){val speech=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM).putExtra(RecognizerIntent.EXTRA_LANGUAGE,java.util.Locale.getDefault().toLanguageTag()).putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true).putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1).putExtra(RecognizerIntent.EXTRA_PROMPT,"Dictar a Spotlight · revisá el texto antes de ejecutar");if(packageManager.resolveActivity(speech,0)==null){Toast.makeText(this,"No hay un servicio de dictado compatible instalado.",Toast.LENGTH_LONG).show();startActivity(Intent(this,InvokeActivity::class.java).putExtra("query",intent.getStringExtra("fallback")));finish()}else runCatching{launched=true;recognition.launch(speech)}.onFailure{Toast.makeText(this,"No se pudo iniciar el dictado.",Toast.LENGTH_LONG).show();startActivity(Intent(this,InvokeActivity::class.java).putExtra("query",intent.getStringExtra("fallback")));finish()}}}
 override fun onSaveInstanceState(outState:Bundle){outState.putBoolean("launched",launched);super.onSaveInstanceState(outState)}
}
