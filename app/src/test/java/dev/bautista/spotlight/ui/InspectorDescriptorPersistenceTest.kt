package dev.bautista.spotlight.ui
import android.app.Application
import android.content.*
import android.content.pm.*
import android.net.Uri
import androidx.compose.runtime.saveable.SaverScope
import dev.bautista.spotlight.data.FileBuffer
import dev.bautista.spotlight.search.*
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class InspectorDescriptorPersistenceTest {
 @Test fun selectedDocumentsPersistAsDescriptorsWithoutExtractedTextOrGrants() {
  val buffer=FileBuffer()
  buffer.add(Result("file:one","Anatomía.pdf","","Archivos",0,Action.Launch(Intent(Intent.ACTION_VIEW,Uri.parse("content://fixture/one")).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)),preview="PRIVATE_EXTRACT",mime="application/pdf"))
  val raw=with(FileBufferSaver){SaverScope{true}.save(buffer)}!!
  assertFalse(raw.contains("PRIVATE_EXTRACT"))
  val restored=FileBufferSaver.restore(raw)!!
  assertEquals(1,restored.items.size);assertEquals("file:one",restored.items.single().id)
  assertEquals(0,(restored.items.single().action as Action.Launch).intent.flags)
 }
 @Test fun localNoteRecoveryContainsOnlyItsIdentityForRevalidation() {
  val item=Result("note:one","Título","","Notas",0,Action.EditNote("one","PRIVATE_NOTE"),preview="PRIVATE_NOTE")
  val raw=with(DocumentPreviewSaver){SaverScope{true}.save(item)}!!
  assertFalse(raw.contains("PRIVATE_NOTE"));val restored=DocumentPreviewSaver.restore(raw)!!
  val action=restored.action as Action.EditNote;assertEquals("one",action.id);assertEquals("",action.text);assertNull(restored.preview)
 }
}
