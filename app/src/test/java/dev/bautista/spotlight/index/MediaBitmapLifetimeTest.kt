package dev.bautista.spotlight.index

import android.app.Application
import android.graphics.Bitmap
import com.google.android.gms.tasks.TaskCompletionSource
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Actual shared production owner and Google Task callbacks; recognizer output is a fixture. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
class MediaBitmapLifetimeTest {
 @Test fun invalidImageReturnsNoTextWithoutAllocatingARecognizer()=runBlocking {
  val context=org.robolectric.RuntimeEnvironment.getApplication()
  val file=java.io.File(context.cacheDir,"invalid-ocr-image.png").apply{writeText("not an image")}
  try{MediaAnalysis(context).use{assertEquals("",it.image(android.net.Uri.fromFile(file),false))}}finally{file.delete()}
 }

 @Test fun cancellationWhileLabelsRunDoesNotRecycleInputUntilLabelsComplete()=runBlocking {
  val image=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888)
  val text=TaskCompletionSource<String>();val labels=TaskCompletionSource<String>();val labeling=CompletableDeferred<Unit>()
  val job=launch{withOwnedBitmap(image){owner->
   assertEquals("recognized",awaitOwnedTask({text.task},owner.retain()))
   labeling.complete(Unit);awaitOwnedTask<String>({labels.task},owner.retain());Unit
  }}
  yield();assertFalse(image.isRecycled);text.setResult("recognized");labeling.await()
  job.cancelAndJoin();assertFalse("Labels still own their input",image.isRecycled)
  labels.setResult("fixture labels");assertTrue(image.isRecycled)
 }
 @Test fun successfulTextAndLabelSequenceReleasesBitmapOnlyAfterTheWholeOperation()=runBlocking {
  val image=Bitmap.createBitmap(32,32,Bitmap.Config.ARGB_8888)
  val text=TaskCompletionSource<String>();val labels=TaskCompletionSource<String>()
  text.setResult("recognized");labels.setResult("fixture labels")
  val result=withOwnedBitmap(image){owner->
   val a=awaitOwnedTask({text.task},owner.retain());assertFalse(image.isRecycled)
   val b=awaitOwnedTask({labels.task},owner.retain());assertFalse(image.isRecycled)
   "$a:$b"
  }
  assertEquals("recognized:fixture labels",result);assertTrue(image.isRecycled)
 }
}
