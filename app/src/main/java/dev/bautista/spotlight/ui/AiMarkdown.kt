package dev.bautista.spotlight.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

/** A deliberately small native Markdown renderer. No HTML, remote images or automatic links. */
internal data class AiMarkdownBlock(val text:String,val heading:Int=0,val code:Boolean=false)
internal fun markdownBlocks(raw:String):List<AiMarkdownBlock> {
    val blocks=mutableListOf<AiMarkdownBlock>();val code=StringBuilder();var fenced=false
    raw.lines().forEach { line ->
        when {
            line.trimStart().startsWith("```")-> {if(fenced){blocks+=AiMarkdownBlock(code.toString().trimEnd(),code=true);code.clear()};fenced=!fenced}
            fenced->{code.append(line).append('\n')}
            else->{val heading=Regex("^(#{1,6})\\s+(.*)$").matchEntire(line);blocks+=if(heading!=null)AiMarkdownBlock(heading.groupValues[2],heading.groupValues[1].length) else AiMarkdownBlock(line.replace(Regex("^\\s*[-*]\\s+"),"• "))}
        }
    }
    if(fenced && code.isNotEmpty())blocks+=AiMarkdownBlock(code.toString().trimEnd(),code=true)
    return blocks
}
internal fun markdownInline(text:String):AnnotatedString = buildAnnotatedString {
    val tokens=Regex("\\*\\*([^*]+)\\*\\*|`([^`]+)`")
    var end=0
    tokens.findAll(text).forEach { match ->
        append(text.substring(end,match.range.first))
        if(match.groupValues[1].isNotEmpty())withStyle(SpanStyle(fontWeight=FontWeight.SemiBold)){append(match.groupValues[1])}
        else withStyle(SpanStyle(fontFamily=FontFamily.Monospace)){append(match.groupValues[2])}
        end=match.range.last+1
    }
    append(text.substring(end))
}
@Composable internal fun AiMarkdown(raw:String) {
    val blocks=remember(raw){markdownBlocks(raw)}
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(4.dp)) {
        blocks.forEach { block ->
            if(block.code)Text(block.text,Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.onSurface.copy(alpha=.06f),RoundedCornerShape(7.dp)).padding(10.dp),fontFamily=FontFamily.Monospace,fontSize=12.sp,lineHeight=19.sp)
            else Text(markdownInline(block.text),fontSize=when(block.heading){1->20.sp;2->17.sp;3->15.sp;else->14.sp},fontWeight=if(block.heading>0)FontWeight.SemiBold else FontWeight.Normal,lineHeight=23.sp)
        }
    }
}
