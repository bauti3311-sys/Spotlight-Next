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
    onDismiss:(()->Unit)?=null,
    onOptionsChange:(Boolean)->Unit={},
    optionsMaxHeight:Dp=320.dp,
    optionsOpen:Boolean?=null,
    onSearchFocus:()->Unit={},
    quickMenu:(@Composable ()->Unit)?=null,
) {
    val colors = MaterialTheme.colorScheme
    val searchField:@Composable (Modifier)->Unit = {fieldModifier ->
        Row(
            fieldModifier
                .height(60.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                HugeIcons.Search,
                "Buscar",
                Modifier.size(22.dp),
                tint = colors.onSurface.copy(alpha = .60f),
            )
            Spacer(Modifier.width(12.dp))
            BasicTextField(
                query,
                { onQueryChange(it.take(4000)) },
                Modifier.weight(1f).focusRequester(focus).onFocusChanged{if(it.isFocused)onSearchFocus()},
                singleLine = true,
                textStyle =
                    TextStyle(color = colors.onSurface, fontFamily=when(LocalAppearance.current.typeface){"system"->androidx.compose.ui.text.font.FontFamily.Default;"mono"->androidx.compose.ui.text.font.FontFamily.Monospace;else->Inter}, fontSize = if (compact) 17.sp else 20.sp),
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
                                style=TextStyle(fontFamily=when(LocalAppearance.current.typeface){"system"->androidx.compose.ui.text.font.FontFamily.Default;"mono"->androidx.compose.ui.text.font.FontFamily.Monospace;else->Inter},letterSpacing=0.sp),
                                softWrap=false,
                            )
                        inner()
                    }
                },
            )
            if (suggestion != null)
                Text(
                    "→",
                    fontSize = 10.sp,
                    color = colors.onSurfaceVariant,
                    modifier =
                        Modifier.clickable(onClick = onAcceptSuggestion).padding(horizontal = 6.dp),
                )
            onDictate?.let{dictate->IconButton(onClick=dictate,modifier=Modifier.size(48.dp)){Icon(HugeIcons.Mic,"Dictar consulta",Modifier.size(18.dp))}}
            if (query.isNotEmpty())
                IconButton(onClick = { onClear() }, modifier = Modifier.size(48.dp)) {
                    Icon(HugeIcons.Close, "Limpiar búsqueda", Modifier.size(18.dp))
                }
        }
    }
    // A service overlay cannot safely host an Activity-style popup window.
    // Keep options in the same window/token as the editor.
    var localOptions by remember { mutableStateOf(false) }
    val options=optionsOpen ?: localOptions
    fun setOptions(value:Boolean){localOptions=value;onOptionsChange(value)}
    Column(Modifier.fillMaxWidth().semantics { testTag="spotlight-bar" }) {
        Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
            searchField(Modifier.weight(1f))
            IconButton(onClick={setOptions(!options)},modifier=Modifier.size(48.dp)) {
                Icon(CommandIcons.Sliders,"Opciones de búsqueda",Modifier.size(20.dp))
            }
        }
        if(options) {
            HorizontalDivider()
            Column(Modifier.fillMaxWidth().heightIn(max=optionsMaxHeight.coerceAtLeast(0.dp)).verticalScroll(rememberScrollState())) {
                if(quickMenu!=null)quickMenu()
                else listOf("Todo","Apps","Archivos","Acciones","IA").forEachIndexed { i,item ->
                    val label=when(item){"Apps"->"Aplicaciones";"Acciones"->"Comandos";else->item}
                    DropdownMenuItem(text={Text(label)},modifier=Modifier.semantics {
                        contentDescription=if(i==0)"Búsqueda universal" else "$label · Ctrl/⌘+$i"
                        selected=category==item
                    },onClick={setOptions(false);onCategoryChange(if(category==item)"Todo" else item)})
                }
                onDismiss?.let { dismiss ->
                    HorizontalDivider()
                    DropdownMenuItem(text={Text("Cerrar Spotlight")},modifier=Modifier.semantics{contentDescription="Cerrar Spotlight"},onClick={setOptions(false);dismiss()})
                }
            }
        }
    }
}
