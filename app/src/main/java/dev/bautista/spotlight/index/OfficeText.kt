package dev.bautista.spotlight.index
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.*
import java.util.zip.ZipInputStream

data class OfficeContent(val text:String,val author:String="",val created:Long=0)
object OfficeText {
 val types=setOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet","application/vnd.openxmlformats-officedocument.presentationml.presentation")
 /** Preserve explicit provider types; fill generic document MIME from a recognized extension. */
 fun mime(name:String,fallback:String?):String? {
  val generic=fallback.isNullOrBlank() || fallback.lowercase(java.util.Locale.ROOT) in setOf("application/octet-stream","binary/octet-stream")
  return when(name.substringAfterLast('.').lowercase(java.util.Locale.ROOT)) {
   "docx"->"application/vnd.openxmlformats-officedocument.wordprocessingml.document"
   "xlsx"->"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
   "pptx"->"application/vnd.openxmlformats-officedocument.presentationml.presentation"
   "pdf"->if(generic)"application/pdf" else fallback
   "txt","md","markdown"->if(generic)"text/plain" else fallback
   else->fallback
  }
 }
 fun supports(mime:String?,name:String="")=mime in types || name.substringAfterLast('.').lowercase() in setOf("docx","xlsx","pptx")
 private fun parse(bytes:ByteArray,onToken:(XmlPullParser,Int)->Unit){val p=Xml.newPullParser();p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,true);p.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL,false);p.setInput(ByteArrayInputStream(bytes),null);var depth=0;var tokens=0;while(true){val event=p.nextToken();require(++tokens<300000){"XML demasiado complejo."};require(event!=XmlPullParser.DOCDECL){"El documento contiene declaraciones XML no compatibles."};if(event==XmlPullParser.END_DOCUMENT)break;if(event==XmlPullParser.START_TAG){depth++;require(depth<=80)};onToken(p,event);if(event==XmlPullParser.END_TAG)depth--}}
 private fun text(bytes:ByteArray,names:Set<String>):String {val out=StringBuilder();var selected=0;parse(bytes){p,e->if(e==XmlPullParser.START_TAG && p.name in names)selected++;if(e==XmlPullParser.TEXT && selected>0 && out.length<24000)out.append(p.text.take(24000-out.length));if(e==XmlPullParser.END_TAG){if(p.name in names)selected--;if(p.name in setOf("p","row","si"))out.append('\n')}};return out.toString().take(24000).trim()}
 fun extract(bytes:ByteArray):OfficeContent {
  require(bytes.size<=8_000_000);val parts=linkedMapOf<String,ByteArray>();var count=0;var total=0
  ZipInputStream(ByteArrayInputStream(bytes)).use{zip->while(true){val entry=zip.nextEntry ?: break;require(++count<=200){"El documento tiene demasiados componentes."};val output=ByteArrayOutputStream();val buffer=ByteArray(8192);while(true){val n=zip.read(buffer);if(n<0)break;total+=n;require(total<=12_000_000 && output.size()+n<=2_000_000){"El documento descomprimido supera el límite."};output.write(buffer,0,n)};if(entry.name.endsWith(".xml")){require(entry.name !in parts){"Componente duplicado."};parts[entry.name]=output.toByteArray()};zip.closeEntry()}}
  val document=parts["word/document.xml"]
  val content=when{
   document!=null->text(document,setOf("t"))
   parts.keys.any{it.startsWith("ppt/slides/slide")}->parts.keys.filter{Regex("ppt/slides/slide[0-9]+\\.xml").matches(it)}.sortedBy{it.substringAfter("slide").substringAfter("slide").substringBefore('.').toIntOrNull() ?: 0}.joinToString("\n"){text(parts.getValue(it),setOf("t"))}.take(24000)
   parts.keys.any{it.startsWith("xl/worksheets/")}->{val shared=mutableListOf<String>();parts["xl/sharedStrings.xml"]?.let{raw->var current:StringBuilder?=null;var inText=false;parse(raw){p,e->when{e==XmlPullParser.START_TAG && p.name=="si"->current=StringBuilder();e==XmlPullParser.START_TAG && p.name=="t"->inText=true;e==XmlPullParser.TEXT && inText->current?.append(p.text);e==XmlPullParser.END_TAG && p.name=="t"->inText=false;e==XmlPullParser.END_TAG && p.name=="si"->{require(shared.size<10000);shared+=current.toString().take(1000);current=null}}}};buildString{parts.filterKeys{Regex("xl/worksheets/sheet[0-9]+\\.xml").matches(it)}.toSortedMap().forEach{(_,raw)->var cellType="";var reading=false;val value=StringBuilder();parse(raw){p,e->when{e==XmlPullParser.START_TAG && p.name=="c"->{cellType=p.getAttributeValue(null,"t").orEmpty();value.clear()};e==XmlPullParser.START_TAG && p.name in setOf("v","t")->reading=true;e==XmlPullParser.TEXT && reading->value.append(p.text);e==XmlPullParser.END_TAG && p.name in setOf("v","t")->reading=false;e==XmlPullParser.END_TAG && p.name=="c"->{if(length<24000)append(if(cellType=="s")shared.getOrNull(value.toString().toIntOrNull() ?: -1).orEmpty() else value.toString()).append('\t')};e==XmlPullParser.END_TAG && p.name=="row"->if(length<24000)append('\n')}};if(length<24000)append('\n')}}.take(24000)}
   else->error("No se encontró un documento Office compatible.")
  }
  val core=parts["docProps/core.xml"];val author=core?.let{text(it,setOf("creator"))}.orEmpty().take(300);val date=core?.let{text(it,setOf("created"))}.orEmpty();val created=runCatching{java.time.Instant.parse(date).toEpochMilli()}.getOrDefault(0)
  require(content.isNotBlank()){"No se encontró texto extraíble."};return OfficeContent(content,author,created)
 }
}
