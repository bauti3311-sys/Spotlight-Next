package dev.bautista.spotlight.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.interaction.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.semantics.*
import androidx.compose.foundation.text.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*

@Composable
fun SpotlightBar(
    query: String,
    onQueryChange: (String) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSubmit: () -> Unit,
    focus: FocusRequester,
    opacity: Float,
    compact: Boolean,
    suggestion: String? = null,
    onAcceptSuggestion: () -> Unit = {},
    animations: Boolean = true,
    onDictate:(()->Unit)?=null,
) {
    val colors = MaterialTheme.colorScheme
    val style = LocalAppearance.current.appearance
    val barShape = androidx.compose.foundation.shape.RoundedCornerShape(LocalAppearance.current.corner.dp)
    val modeShape=if(LocalAppearance.current.preset in setOf("raycast","graphite","ios")) androidx.compose.foundation.shape.RoundedCornerShape(15.dp) else CircleShape
    val motion=animations && LocalMotion.current
    val circle = 48.dp
    val gap = if (compact) 6.dp else 10.dp
    val phone=compact && LocalAppearance.current.adaptive
    val searchField:@Composable (Modifier)->Unit = {fieldModifier ->
        Row(
            fieldModifier
                .height(60.dp)
                .shadow(if(LocalAppearance.current.preset=="graphite")1.dp else if(LocalAppearance.current.preset=="macos")8.dp else 4.dp, barShape)
                .clip(barShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            colors.surface.copy(alpha = opacity),
                            if(!LocalAppearance.current.blur) colors.surface.copy(alpha=opacity) else if (colors.background.luminance() > .5f)
                                Color(0xFFF1F7FA).copy(alpha = opacity)
                            else Color(0xFF252A35).copy(alpha = opacity),
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = if(colors.background.luminance()>.5f) .80f else .17f), barShape)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                CommandIcons.Search,
                "Buscar",
                Modifier.size(22.dp),
                tint = colors.onSurface.copy(alpha = .60f),
            )
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                query,
                { onQueryChange(it.take(4000)) },
                Modifier.weight(1f).focusRequester(focus),
                singleLine = true,
                textStyle =
                    TextStyle(color = colors.onSurface, fontFamily=Inter, fontSize = if (compact) 17.sp else 20.sp),
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty())
                            Text(
                                when(category){"IA"->"Preguntá a la IA…";"Archivos"->"Buscar archivos…";"Apps"->"Buscar aplicaciones…";"Acciones"->"Buscar comandos…";else->if(compact)"Buscar…" else "Buscar en Spotlight"},
                                fontSize = if (compact) 17.sp else 20.sp,
                                color = colors.onSurface.copy(alpha = .52f),
                                maxLines = 1,
                            )
                        if (suggestion != null && query.isNotEmpty())
                            Text(
                                buildAnnotatedString {
                                    withStyle(SpanStyle(color=Color.Transparent)){append(query)}
                                    append(suggestion.drop(query.length))
                                },
                                maxLines = 1,
                                fontSize = if (compact) 17.sp else 20.sp,
                                color = colors.onSurface.copy(alpha = .30f),
                                style=TextStyle(fontFamily=Inter,letterSpacing=0.sp),
                                softWrap=false,
                            )
                        inner()
                    }
                },
            )
            if (suggestion != null)
                Text(
                    "Tab",
                    fontSize = 10.sp,
                    color = colors.onSurfaceVariant,
                    modifier =
                        Modifier.clickable(onClick = onAcceptSuggestion).padding(horizontal = 6.dp),
                )
            onDictate?.let{dictate->IconButton(onClick=dictate,modifier=Modifier.size(48.dp)){Icon(CommandIcons.Mic,"Dictar consulta",Modifier.size(18.dp))}}
            if (query.isNotEmpty())
                IconButton(onClick = { onClear() }, modifier = Modifier.size(48.dp)) {
                    Icon(CommandIcons.Close, "Limpiar búsqueda", Modifier.size(18.dp))
                }
        }
    }
    val categoryButtons:@Composable ()->Unit = {
        listOf("Apps", "Archivos", "Acciones", "IA").forEachIndexed { i, item ->
            val interaction=remember(item){MutableInteractionSource()}
            val hovered by interaction.collectIsHoveredAsState()
            val focused by interaction.collectIsFocusedAsState()
            val selectedCategory = category == item
            val label=when(i){0->"Apps";1->"Archivos";2->"Comandos";else->"IA"}
            val categoryBackground by animateColorAsState(if(selectedCategory) colors.primary.copy(alpha=.15f) else if(hovered || focused)colors.primary.copy(alpha=.09f) else colors.surface.copy(alpha=opacity),tween(if(motion)140 else 0),label="category-color")
            val categoryScale by animateFloatAsState(if(selectedCategory)1.025f else 1f,tween(if(motion)140 else 0),label="category-scale")
            val tint =
                if (selectedCategory) colors.primary
                else if (colors.background.luminance() > .5f) Color(0xFF667083)
                else Color(0xFFBCC8E0)
            Box(
                Modifier.size(circle).graphicsLayer {scaleX=categoryScale;scaleY=categoryScale}
                    .shadow(if(LocalAppearance.current.preset=="graphite")0.dp else 3.dp, modeShape)
                    .clip(modeShape)
                    .background(categoryBackground)
                    .border(1.dp, Color.White.copy(alpha = if(colors.background.luminance()>.5f) .80f else .17f), modeShape)
                    .semantics {selected=selectedCategory;stateDescription=if(selectedCategory)"Modo activo" else "Abrir modo"}
                    .hoverable(interaction)
                    .clickable(interactionSource=interaction,indication=LocalIndication.current) { onCategoryChange(if (selectedCategory) "Todo" else item) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    when(i) {0->CommandIcons.Apps;1->CommandIcons.Folder;2->CommandIcons.Bolt;else->CommandIcons.AutoAwesome},
                    (when(i){0->"Aplicaciones";2->"Comandos";else->item})+" · Ctrl/⌘+${i+1}", Modifier.size(if(hovered || focused)21.dp else 23.dp).then(if(hovered || focused)Modifier.offset(y=(-5).dp) else Modifier), tint=tint,
                )
                if(hovered || focused)Text(label,Modifier.align(Alignment.BottomCenter).padding(bottom=5.dp),fontSize=8.sp,color=tint,maxLines=1)
            }
        }
    }
    if(phone) Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        searchField(Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp,Alignment.End),verticalAlignment=Alignment.CenterVertically){categoryButtons()}
    } else Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(gap)){
        searchField(Modifier.weight(1f));categoryButtons()
    }
}
