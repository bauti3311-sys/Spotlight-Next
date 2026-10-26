package dev.bautista.spotlight.sharing

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33], application=android.app.Application::class)
class SharedInputTest {
    private val first = Uri.parse("content://fixture.documents/first")
    private val second = Uri.parse("content://fixture.documents/second")
    private fun intent() = Intent(Intent.ACTION_SEND).setType("application/pdf")
    private fun clip(uri: Uri) = ClipData("document", arrayOf("application/pdf"), ClipData.Item(uri))
    private fun rejected(intent: Intent) {
        try { SharedInput.parse(intent); fail("Input must be rejected before dereferencing or replacing context") }
        catch (_: IllegalArgumentException) { }
        catch (_: IllegalStateException) { }
    }
    @Test fun acceptsClipDataOnlyWithoutReadingProvider() {
        val input = SharedInput.parse(intent().apply { clipData = clip(first) })
        assertEquals(listOf(first), input.documents)
        assertEquals("", input.text)
    }
    @Test fun deduplicatesStreamAndClipAndKeepsDocumentOrder() {
        val input = SharedInput.parse(intent().apply {
            action=Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(first, second))
            clipData=clip(first).apply { addItem(ClipData.Item(second)) }
        })
        assertEquals(listOf(first, second), input.documents)
    }
    @Test fun multipleStreamIsNeverCastAsSingleUri() {
        assertEquals(listOf(first, second), SharedInput.parse(intent().apply {
            action=Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(first, second))
        }).documents)
    }
    @Test fun rejectsFileOrHttpUriEvenWithValidText() {
        listOf("file:///sdcard/private.pdf", "https://example.org/doc", "content:///missing-authority").forEach { bad ->
            rejected(intent().putExtra(Intent.EXTRA_TEXT, "Texto válido").apply { clipData=clip(Uri.parse(bad)) })
        }
    }
    @Test fun rejectsMalformedExtrasAndUnsupportedActions() {
        rejected(intent().putExtra(Intent.EXTRA_STREAM, "content://pretend"))
        rejected(intent().apply { action=Intent.ACTION_SEND_MULTIPLE; putStringArrayListExtra(Intent.EXTRA_STREAM, arrayListOf("bad")) })
        rejected(intent().putExtra(Intent.EXTRA_TEXT, 42))
        rejected(Intent(Intent.ACTION_VIEW).setData(first))
        rejected(intent())
    }
    @Test fun boundsBothClipDataAndStreamLists() {
        val uris=(0..12).map { Uri.parse("content://fixture.documents/$it") }
        rejected(intent().apply { action=Intent.ACTION_SEND_MULTIPLE; putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris)) })
        rejected(intent().apply { clipData=clip(uris.first()).apply { uris.drop(1).forEach { addItem(ClipData.Item(it)) } } })
        assertEquals(12,SharedInput.parse(intent().apply { action=Intent.ACTION_SEND_MULTIPLE; putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris.take(12))) }).documents.size)
    }
    @Test fun boundsMergedDocumentsWhenEachInputAloneIsValid() {
        rejected(intent().apply {
            action=Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList((0..6).map { Uri.parse("content://fixture.documents/$it") }))
            clipData=clip(Uri.parse("content://fixture.documents/7")).apply { (8..13).forEach { addItem(ClipData.Item(Uri.parse("content://fixture.documents/$it"))) } }
        })
    }
    @Test fun usesBoundedExplicitTextBeforeClipTextWithoutResolvingUris() {
        val text="a".repeat(17000)
        assertEquals(16000,SharedInput.parse(intent().putExtra(Intent.EXTRA_TEXT,text)).text.length)
        val fromClip=SharedInput.parse(intent().apply { clipData=ClipData.newPlainText("text","Texto explícito") })
        assertEquals("Texto explícito",fromClip.text)
        assertEquals(emptyList<Uri>(),fromClip.documents)
        assertEquals("Preferido", SharedInput.parse(intent().putExtra(Intent.EXTRA_TEXT,"Preferido").apply { clipData=ClipData.newPlainText("text","Alternativo") }).text)
    }
}
