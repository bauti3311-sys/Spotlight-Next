package dev.bautista.spotlight.index
import android.app.Application
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.util.zip.*
@RunWith(RobolectricTestRunner::class) @Config(sdk=[33],application=Application::class)
class OfficeTextTest {
 private fun zip(vararg entries:Pair<String,String>):ByteArray {val out=ByteArrayOutputStream();ZipOutputStream(out).use{z->entries.forEach{(name,xml)->z.putNextEntry(ZipEntry(name));z.write(xml.toByteArray());z.closeEntry()}};return out.toByteArray()}
 @Test fun docxAndMetadataAreReadWithoutExecutingContent(){val raw=zip("word/document.xml" to "<document><p><t>Rehabilitación </t><t>vestibular</t></p><p><t>Segundo párrafo</t></p></document>","docProps/core.xml" to "<core><creator>Bautista</creator><created>2026-10-11T12:00:00Z</created></core>");val content=OfficeText.extract(raw);assertTrue(content.text.contains("Rehabilitación vestibular\nSegundo párrafo"));assertEquals("Bautista",content.author);assertTrue(content.created>0)}
 @Test fun spreadsheetsResolveSharedStringsAndUseCachedValues(){val content=OfficeText.extract(zip("xl/sharedStrings.xml" to "<sst><si><t>Materia</t></si><si><t>Anatomía</t></si></sst>","xl/worksheets/sheet1.xml" to "<worksheet><row><c t='s'><v>0</v></c><c t='s'><v>1</v></c><c><f>1+1</f><v>2</v></c></row></worksheet>"));assertTrue(content.text.contains("Materia\tAnatomía\t2"));assertFalse(content.text.contains("1+1"))}
 @Test fun presentationReadsSlidesInNumericOrder(){val content=OfficeText.extract(zip("ppt/slides/slide10.xml" to "<slide><p><t>Ten</t></p></slide>","ppt/slides/slide2.xml" to "<slide><p><t>Two</t></p></slide>"));assertTrue(content.text.indexOf("Two")<content.text.indexOf("Ten"))}
 @Test fun xmlEntitiesAndCompressedBombsAreRejected(){val evil="<!DOCTYPE document [<!ENTITY x SYSTEM 'file:///private'>]><document><p><t>&x;</t></p></document>";assertNotNull(runCatching{OfficeText.extract(zip("word/document.xml" to evil))}.exceptionOrNull());assertNotNull(runCatching{OfficeText.extract(zip("word/document.xml" to "x".repeat(2_000_001)))}.exceptionOrNull())}
}
