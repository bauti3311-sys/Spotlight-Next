package dev.bautista.spotlight.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.*

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
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
    onSearchFocusChanged:(Boolean)->Unit={},
    onAutocompleteAvailabilityChanged:(Boolean)->Unit={},
    quickMenu:(@Composable ()->Unit)?=null,
    onBack:(()->Unit)?=null,
    onSaved:(()->Unit)?=null,
    onSettings:(()->Unit)?=null,
) {
    val colors = MaterialTheme.colorScheme
    var editorValue by remember { mutableStateOf(TextFieldValue(query,TextRange(query.length))) }
    // Selection belongs to the editor. External completion/restoration places the caret at the end.
    val visibleValue=if(editorValue.text==query)editorValue else TextFieldValue(query,TextRange(query.length))
    SideEffect {
        if(editorValue!=visibleValue)editorValue=visibleValue
        onAutocompleteAvailabilityChanged(suggestion!=null && visibleValue.selection.collapsed &&
            visibleValue.selection.end==visibleValue.text.length && visibleValue.composition==null)
    }
    var localOptions by remember { mutableStateOf(false) }
    val options=optionsOpen ?: localOptions
    fun setOptions(value:Boolean){localOptions=value;onOptionsChange(value)}

    val searchField:@Composable (Modifier)->Unit = {fieldModifier ->
        Row(
            fieldModifier
                .height(GlassTokens.searchHeight)
                .padding(start = 4.dp, end = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if(onBack!=null) IconButton(onClick=onBack,modifier=Modifier.size(48.dp)) {
                Icon(CommandIcons.Reply,"Volver a resultados",Modifier.size(20.dp),tint=colors.onSurfaceVariant)
            } else IconButton(onClick={setOptions(!options)},modifier=Modifier.size(48.dp)) {
                Icon(HugeIcons.Search,"Fuentes de búsqueda",Modifier.size(21.dp),tint=colors.onSurfaceVariant)
            }
            Spacer(Modifier.width(4.dp))
            BasicTextField(
                visibleValue,
                { next ->
                    val previousText=editorValue.text
                    editorValue=next.copy(text=next.text.take(4000))
                    // Compare consecutive input events, not a query captured before this frame.
                    if(editorValue.text!=previousText)onQueryChange(editorValue.text)
                },
                Modifier.weight(1f).semantics {
                    // Accessibility replacement can arrive twice before recomposition. Forward
                    // both events using live state; the controlled field otherwise drops a
                    // replacement equal to its last rendered value.
                    setText { replacement ->
                        val text=replacement.text.take(4000)
                        val previous=editorValue.text
                        editorValue=TextFieldValue(text,TextRange(text.length))
                        if(text!=previous)onQueryChange(text)
                        true
                    }
                }.focusProperties { left=FocusRequester.Cancel; right=FocusRequester.Cancel }.focusRequester(focus).onFocusChanged{onSearchFocusChanged(it.isFocused);if(it.isFocused)onSearchFocus()},
                singleLine = true,
                textStyle =
                    TextStyle(color = colors.onSurface, fontFamily=when(LocalAppearance.current.typeface){"system"->androidx.compose.ui.text.font.FontFamily.Default;"mono"->androidx.compose.ui.text.font.FontFamily.Monospace;else->Inter}, fontSize = if (compact) GlassTokens.compactSearchText else GlassTokens.searchText),
                cursorBrush = SolidColor(colors.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmit() }),
                decorationBox = { inner ->
                    Box {
                        if (query.isEmpty())
                            Text(
                                when(category){"IA"->"Preguntá a la IA…";"Archivos"->"Buscar archivos…";"Apps"->"Buscar aplicaciones…";"Acciones"->"Buscar comandos…";else->if(compact)"Buscar…" else "Buscar en Spotlight"},
                                fontSize = if (compact) GlassTokens.compactSearchText else GlassTokens.searchText,
                                color = colors.onSurface.copy(alpha = .52f),letterSpacing=0.sp,
                                maxLines = 1,
                            )
                        if (suggestion != null && query.isNotEmpty())
                            Text(
                                buildAnnotatedString {
                                    withStyle(SpanStyle(color=Color.Transparent)){append(query)}
                                    append(suggestion.drop(query.length))
                                },
                                modifier = Modifier.clearAndSetSemantics { },
                                maxLines = 1,
                                fontSize = if (compact) GlassTokens.compactSearchText else GlassTokens.searchText,
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
                    "Ctrl+Space",
                    fontSize = 10.sp,
                    color = colors.onSurfaceVariant,
                    modifier =
                        Modifier.clickable(onClick = onAcceptSuggestion).padding(horizontal = 6.dp),
                )

            if (query.isNotEmpty())
                IconButton(onClick = { onClear() }, modifier = Modifier.size(48.dp)) {
                    Icon(HugeIcons.Close, "Limpiar búsqueda", Modifier.size(18.dp))
                }
        }
    }
    // A service overlay cannot safely host an Activity-style popup window.
    // Keep options in the same window/token as the editor.
    BoxWithConstraints(Modifier.fillMaxWidth()) {
    val showSaved=maxWidth>=480.dp
    Column(Modifier.fillMaxWidth().semantics { testTag="spotlight-bar" }) {
        Row(Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=4.dp), verticalAlignment=Alignment.CenterVertically) {
            searchField(Modifier.weight(1f))
            Row(Modifier.padding(end=8.dp),verticalAlignment=Alignment.CenterVertically) {
                onSaved?.takeIf{showSaved}?.let { IconButton(onClick=it,modifier=Modifier.size(48.dp)) { Icon(HugeIcons.Saved,"Abrir Guardados",Modifier.size(18.dp),tint=colors.onSurfaceVariant) } }
                onDictate?.let { IconButton(onClick=it,modifier=Modifier.size(48.dp)) { Icon(HugeIcons.Mic,"Dictar consulta",Modifier.size(18.dp),tint=colors.onSurfaceVariant) } }
                onSettings?.let { IconButton(onClick=it,modifier=Modifier.size(48.dp)) { Icon(HugeIcons.Settings,"Abrir Ajustes",Modifier.size(18.dp),tint=colors.onSurfaceVariant) } }
            }
        }
        if(options) {
            HorizontalDivider()
            Column(Modifier.fillMaxWidth().heightIn(max=(optionsMaxHeight-16.dp).coerceAtLeast(0.dp)).verticalScroll(rememberScrollState())) {
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
}
