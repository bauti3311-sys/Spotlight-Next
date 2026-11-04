package dev.bautista.spotlight.ui

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.search.*

/** Presentation boundary only. Ranking, selection, gesture effects and launch review stay with the owner. */
@Composable internal fun SpotlightResultList(
    results:List<Result>,config:Config,query:String,selectedIndex:Int,
    listState:LazyListState,maxHeight:Dp,homeSections:Boolean,homeLayout:Boolean,
    inline:Boolean,motionEnabled:Boolean,playingPackages:Set<String>,
    isFavorite:(Result)->Boolean,onFocusChanged:(Boolean)->Unit,
    onSelect:(Int,Result)->Unit,onDetails:(Int,Result)->Unit,
    onFavorite:(Result)->Unit,onInteraction:()->Unit,
) {
    val colors=MaterialTheme.colorScheme
    LazyColumn(state=listState,modifier=Modifier.fillMaxWidth()
        .onFocusChanged{onFocusChanged(it.hasFocus)}.focusGroup().heightIn(max=maxHeight)
        .padding(horizontal=GlassTokens.listHorizontalInset(config),vertical=(3f*GlassTokens.densityScale(config)).dp)) {
        if(query.isNotBlank() && results.isNotEmpty())item("results-heading") {
            Text(if(results.map{ResultSections.category(it)}.distinct().size==1)ResultSections.category(results.first()) else "Resultados",
                Modifier.padding(start=GlassTokens.rowHorizontalPadding(config),top=(6f*GlassTokens.densityScale(config)).dp,bottom=(4f*GlassTokens.densityScale(config)).dp),
                fontSize=GlassTokens.scaledSp(11.sp,config),lineHeight=GlassTokens.scaledSp(14.sp,config,minSp=12f),
                fontWeight=FontWeight.Medium,color=colors.onSurfaceVariant)
        }
        itemsIndexed(results,key={_,item->item.id}) { i,item->
            val section=ResultSections.category(item)
            if(query.isBlank() && results.map{ResultSections.category(it)}.distinct().size>1 && results.take(i).none{ResultSections.category(it)==section})
                SectionHeading(if(homeSections)ResultSections.homeHeading(item) else section,first=i==0 && homeLayout)
            ResultRow(item,i==selectedIndex,isFavorite(item),
                onOpen={onSelect(i,item)},onDetails={onDetails(i,item)},onFavorite={onFavorite(item)},
                inline=inline,animations=motionEnabled,onInteraction=onInteraction,
                playing=(item.action as? Action.Launch)?.intent?.component?.packageName in playingPackages)
            if(query.isBlank() && homeSections)Spacer(Modifier.height(3.dp))
        }
    }
}

/** Same action identity as the existing dispatcher, never a new executor or completion claim. */
internal fun primaryActionLabel(result:Result?,config:Config):String=when {
    result?.action is Action.Copy->"Copiar"
    result?.action is Action.ComposeSearch->"Preparar búsqueda"
    result?.action is Action.ComposeMessage->"Preparar mensaje"
    result?.action is Action.Ask->result.title.takeIf{it.startsWith("Responder") || it.startsWith("Preguntar") || it.startsWith("Consultar")} ?: UniversalIntentEngine.primaryAiTitle(config)
    result?.action is Action.SystemPanel && result.action.page=="interpret"->"Revisar plan"
    result?.category=="Archivos"->"Abrir documento"
    (result?.action as? Action.Launch)?.intent?.component!=null->"Abrir aplicación"
    result?.action is Action.Launch->"Abrir"
    else->"Ejecutar"
}
