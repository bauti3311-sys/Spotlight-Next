package dev.bautista.spotlight.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.interaction.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.graphics.drawable.toBitmap
import dev.bautista.spotlight.search.*

@Composable
fun ResultIcon(
    result: Result,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    if (result.icon != null) {
        val bitmap =
            remember(result.id, result.icon) { result.icon.toBitmap(80, 80).asImageBitmap() }
        Image(bitmap, result.category, modifier.clip(RoundedCornerShape(9.dp)))
    } else
        Icon(
            when {
                result.mime=="application/pdf"->PhosphorIcons.Pdf
                result.action is Action.WorkflowRun -> when(dev.bautista.spotlight.automation.Workflows.parse(LocalAppearance.current.routines).firstOrNull{it.id==result.action.id}?.icon){"bolt"->CommandIcons.Bolt;"sun"->CommandIcons.Sun;"music"->CommandIcons.MusicNote;else->CommandIcons.Layers}
                result.action is Action.SystemPanel -> when(result.action.page){"saved"->PhosphorIcons.Saved;"calendar"->PhosphorIcons.Calendar;"weather-native"->PhosphorIcons.Weather;"sports-native"->CommandIcons.Sports;"context"->CommandIcons.Description;"context-ai"->CommandIcons.AutoAwesome;"conversation"->PhosphorIcons.Chat;"diagnostics"->CommandIcons.Info;"tasker","catalog","profiles"->CommandIcons.Layers;"interpret"->CommandIcons.ListChecks;"display-review"->CommandIcons.Sun;"app"->CommandIcons.Apps;"device"->CommandIcons.Display;"shizuku"->CommandIcons.Shield;"focus"->CommandIcons.Timer;"agent"->CommandIcons.AutoAwesome;"extensions"->CommandIcons.Layers;"preferences:appearance"->CommandIcons.Display;"preferences:ai"->CommandIcons.AutoAwesome;"preferences:shortcuts"->CommandIcons.Keyboard;"preferences:suggestions"->CommandIcons.Star;"preferences:permissions"->CommandIcons.Shield;else->CommandIcons.Sliders}
                result.action is Action.ReadNotification -> CommandIcons.Forum
                result.action is Action.SelectMode -> when(result.action.mode){"Apps"->CommandIcons.Apps;"Archivos"->CommandIcons.Folder;"IA"->CommandIcons.AutoAwesome;"Portapapeles"->CommandIcons.ContentCopy;else->CommandIcons.Bolt}
                result.action is Action.NewNote -> CommandIcons.EditNote
                result.action is Action.Ask -> CommandIcons.AutoAwesome
                result.id.startsWith("section:") -> CommandIcons.Chevron
                result.action is Action.Access -> CommandIcons.LockOpen
                result.action is Action.ComposeMessage -> if(result.action.replyKey!=null)CommandIcons.Reply else CommandIcons.ChatBubbleOutline
                result.action is Action.ComposeSearch -> CommandIcons.Search
                result.action is Action.AudioPanel -> CommandIcons.Sliders
                result.action is Action.Device -> when(result.action.command) {"volume_down"->CommandIcons.VolumeDown;"mute"->CommandIcons.Mute;"play"->CommandIcons.Play;"pause"->CommandIcons.Pause;"next"->CommandIcons.Next;"previous"->CommandIcons.Previous;else->CommandIcons.VolumeUp}
                result.action is Action.Navigate -> when(result.action.scope.section) {"replies"->CommandIcons.Forum;"calendar"->CommandIcons.Event;"files-menu"->CommandIcons.Folder;else->when(result.action.scope.kind) { "image"->CommandIcons.Image;"video"->CommandIcons.Videocam;"audio"->CommandIcons.MusicNote;"pdf"->CommandIcons.PictureAsPdf;else->if(result.action.scope.section=="files")CommandIcons.Folder else sectionIcon(ResultSections.category(result)) } }
                result.action is Action.Launch && result.action.intent.action==android.provider.Settings.ACTION_BATTERY_SAVER_SETTINGS -> CommandIcons.Battery
                result.action is Action.Launch && result.action.intent.action==android.provider.Settings.ACTION_INTERNAL_STORAGE_SETTINGS -> CommandIcons.Storage
                result.action is Action.Launch && result.action.intent.action==android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS -> CommandIcons.Location
                result.action is Action.Launch && result.action.intent.action==android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS -> CommandIcons.Accessibility
                result.action is Action.Launch && result.action.intent.action==android.provider.Settings.ACTION_WIFI_SETTINGS -> CommandIcons.Wifi
                result.action is Action.Launch && result.action.intent.action==android.provider.Settings.ACTION_BLUETOOTH_SETTINGS -> CommandIcons.Bluetooth
                result.action is Action.Launch && result.action.intent.action==android.provider.Settings.ACTION_DISPLAY_SETTINGS -> CommandIcons.Display
                result.category=="Archivos" -> when {result.mime?.startsWith("image")==true->CommandIcons.Image;result.mime?.startsWith("video")==true->CommandIcons.Videocam;result.mime?.startsWith("audio")==true->CommandIcons.MusicNote;result.mime=="application/pdf"->CommandIcons.PictureAsPdf;result.mime==android.provider.DocumentsContract.Document.MIME_TYPE_DIR->CommandIcons.Folder;else->CommandIcons.Description}
                else -> sectionIcon(ResultSections.category(result))
            },
            result.category,
            modifier,
            tint = tint,
        )
}

@Composable
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun ResultRow(
    result: Result,
    selected: Boolean,
    favorite: Boolean,
    onOpen: () -> Unit,
    onDetails: () -> Unit,
    onFavorite: () -> Unit,
    animations: Boolean = true,
    onInteraction:()->Unit={},
) {
    val colors = MaterialTheme.colorScheme
    val motion=animations && LocalMotion.current
    val interaction=remember{MutableInteractionSource()}
    val hovered by interaction.collectIsHoveredAsState()
    val pad=when(LocalAppearance.current.density){"compact"->4;"spacious"->10;else->6}
    val ink by animateColorAsState(colors.onSurface,tween(if(motion)GlassTokens.selectionDuration(LocalAppearance.current) else 0),label="result-ink")
    val fill by animateColorAsState(if(selected)GlassTokens.selection() else if(hovered)GlassTokens.hover() else Color.Transparent,tween(if(motion)GlassTokens.selectionDuration(LocalAppearance.current) else 0),label="result-fill")
    Row(
        Modifier.fillMaxWidth().pointerInput(result.id){awaitEachGesture{awaitFirstDown(requireUnconsumed=false,pass=PointerEventPass.Initial);onInteraction()}}.heightIn(min=if(LocalAppearance.current.density=="compact")52.dp else GlassTokens.rowMinimum).semantics { this.selected=selected; customActions=listOf(CustomAccessibilityAction("Operaciones del resultado"){onDetails();true}) }
            .clip(RoundedCornerShape(GlassTokens.rowRadius))
            .background(fill)

            .hoverable(interaction).combinedClickable(interactionSource=interaction,indication=LocalIndication.current,onClick=onOpen,onLongClick=onDetails)
            .padding(start = 12.dp, end = 8.dp, top = pad.dp, bottom = pad.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Native application artwork keeps its own identity. Utility glyphs have
        // one stroke treatment and no tinted tile or selection stripe.
        ResultIcon(result,Modifier.size(if(result.icon!=null)30.dp else GlassTokens.glyph),colors.onSurfaceVariant)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(
                result.title,
                color = ink,
                fontWeight = FontWeight.Medium,
                fontSize = GlassTokens.resultText*LocalAppearance.current.textScale,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                result.subtitle,
                color = colors.onSurfaceVariant,
                fontSize = GlassTokens.secondaryText*LocalAppearance.current.textScale,
                maxLines = if(result.id.startsWith("smart:") || result.id=="intent:local") 2 else 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if(LocalConfiguration.current.screenWidthDp>=600)Text(
            if(result.id.startsWith("smart:") || result.id=="intent:local")"Intención" else result.category,
            modifier=Modifier.padding(horizontal=12.dp).width(110.dp),
            color=colors.onSurfaceVariant,fontSize=GlassTokens.metadataText,textAlign=TextAlign.End,maxLines=1,overflow=TextOverflow.Ellipsis)
        if (favorite)Icon(CommandIcons.Star,"Favorita",Modifier.padding(horizontal=8.dp).size(14.dp),tint=colors.primary)
        Box(Modifier.size(GlassTokens.hit),contentAlignment=Alignment.Center) {
        if(selected || hovered) IconButton(onClick = onDetails, modifier = Modifier.size(GlassTokens.hit)) {
            Icon(
                CommandIcons.Chevron,
                "Operaciones del resultado",
                Modifier.size(18.dp),
                tint = ink.copy(alpha = .7f),
            )
        }
    }
    }

}

@Composable
fun InfoCardView(card: InfoCard, onCopy: () -> Unit, onLink: () -> Unit,onShare:(()->Unit)?=null) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth()
            .heightIn(max = 340.dp)
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        Text(card.title, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        Text(
            card.subtitle,
            fontSize = (11*LocalAppearance.current.textScale).sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
        )
        SelectionContainer { Text(card.body, fontSize = (16*LocalAppearance.current.textScale).sp, lineHeight = 21.sp) }
        card.rows.forEach { (label, value) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Text(
                    label,
                    Modifier.widthIn(min = 90.dp, max = 150.dp),
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant,
                )
                Spacer(Modifier.width(12.dp))
                Text(value, Modifier.weight(1f), fontSize = 13.sp)
            }
        }
        Row {
            TextButton(onClick = onCopy) { Text("Copiar") }
            if(onShare!=null)TextButton(onClick=onShare){Text("Compartir")}
            if (card.url != null) TextButton(onClick = onLink) { Text("Abrir fuente / mapa") }
        }
    }
}
