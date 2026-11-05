package dev.bautista.spotlight.ui

import androidx.compose.ui.platform.testTag

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
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
        Image(bitmap, result.category, modifier.clip(RoundedCornerShape(7.dp)))
    } else
        IosCommandIcon(
            when {
                result.mime=="application/pdf"->PhosphorIcons.Pdf
                result.action is Action.WorkflowRun -> when(dev.bautista.spotlight.automation.Workflows.parse(LocalAppearance.current.routines).firstOrNull{it.id==result.action.id}?.icon){"bolt"->CommandIcons.Bolt;"sun"->CommandIcons.Sun;"music"->CommandIcons.MusicNote;else->CommandIcons.Layers}
                result.action is Action.SystemPanel -> when(result.action.page){"saved"->PhosphorIcons.Saved;"calendar"->PhosphorIcons.Calendar;"weather-native"->PhosphorIcons.Weather;"sports-native"->CommandIcons.Sports;"context"->CommandIcons.Description;"context-ai"->CommandIcons.AutoAwesome;"conversation"->PhosphorIcons.Chat;"diagnostics"->CommandIcons.Info;"tasker","catalog","profiles","saved-command"->CommandIcons.Layers;"interpret"->CommandIcons.ListChecks;"display-review"->CommandIcons.Sun;"app"->CommandIcons.Apps;"device"->CommandIcons.Display;"shizuku"->CommandIcons.Shield;"focus"->CommandIcons.Timer;"agent"->CommandIcons.AutoAwesome;"extensions"->CommandIcons.Layers;"preferences:appearance"->CommandIcons.Display;"preferences:ai"->CommandIcons.AutoAwesome;"preferences:shortcuts"->CommandIcons.Keyboard;"preferences:suggestions"->CommandIcons.Star;"preferences:permissions"->CommandIcons.Shield;else->CommandIcons.Sliders}
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
    playing:Boolean=false,
    inline:Boolean=true,
) {
    val colors = MaterialTheme.colorScheme
    val metadata = if(playing)"En reproducción" else if(PreviewPresentation.isApplication(result))"Aplicación" else result.mime?.substringAfterLast('/')?.uppercase()?.take(12) ?: ResultSections.category(result).let{if(it=="Acciones y comandos")"Comando" else it}
    val motion=animations && LocalMotion.current
    val interaction=remember{MutableInteractionSource()}
    val hovered by interaction.collectIsHoveredAsState()
    val focused by interaction.collectIsFocusedAsState()
    val appearance=LocalAppearance.current
    val d=GlassTokens.densityScale(appearance)
    val pad=GlassTokens.rowVerticalPadding(appearance,inline)
    val hPad=GlassTokens.rowHorizontalPadding(appearance)
    val amount by animateFloatAsState(if(selected)1f else 0f,tween(if(motion)GlassTokens.selectionDuration(appearance) else 0),label="result-selection")
    val dark=colors.surface.luminance()<.5f
    val premium=LocalPremiumReview.current
    val ink by animateColorAsState(colors.onSurface,tween(if(motion)GlassTokens.selectionDuration(LocalAppearance.current) else 0),label="result-ink")
    val fill by animateColorAsState(if(hovered)GlassTokens.hover() else Color.Transparent,tween(if(motion)GlassTokens.selectionDuration(LocalAppearance.current) else 0),label="result-fill")
    Row(
        Modifier.fillMaxWidth().testTag("result:${result.id}").heightIn(min=if(inline)GlassTokens.resultRowHeight(appearance) else (60f*d).coerceAtLeast(48f).dp).semantics { this.selected=selected; customActions=listOf(CustomAccessibilityAction("Operaciones del resultado"){onInteraction();onDetails();true}) }
            .clip(RoundedCornerShape(GlassTokens.rowRadius))
            .background(fill)
            .background(Brush.horizontalGradient(if(premium)listOf(
                colors.primary.copy(alpha=(if(dark).19f else .11f)*amount),
                colors.onSurface.copy(alpha=(if(dark).065f else .025f)*amount)) else GlassTokens.selectedColors(dark,amount)))
            .then(if(focused)Modifier.border(GlassTokens.resultFocusStroke,GlassTokens.resultFocusColor(dark),RoundedCornerShape(GlassTokens.rowRadius)) else Modifier)

            // Let Compose arbitrate taps versus list dragging. Selecting on pointer-down
            // opens automatic Quick Look before LazyColumn can claim the scroll gesture.
            .hoverable(interaction).combinedClickable(interactionSource=interaction,indication=LocalIndication.current,
                onClick={onInteraction();onOpen()},onLongClick={onInteraction();onDetails()})
            .padding(start = hPad, end = (8f*d).dp, top = pad, bottom = pad),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Native application artwork keeps its own identity. Utility glyphs share
        // the same centered slot width so all row titles align to a single vertical axis.
        Box(Modifier.size(GlassTokens.iconSlotWidth(appearance)), contentAlignment = Alignment.Center) {
            ResultIcon(result,Modifier.size(if(result.icon!=null)GlassTokens.appIconSize(appearance) else GlassTokens.glyphSize(appearance)),colors.onSurfaceVariant)
        }
        Spacer(Modifier.width((10f*d).dp))
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)) {
          Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy((10f*d).dp)) {
            Text(result.title,color=ink,fontWeight=if(selected)FontWeight.SemiBold else FontWeight.Medium,
                modifier=Modifier.weight(1f,fill=false),
                fontSize=GlassTokens.scaledSp(GlassTokens.rowText(appearance),appearance),
                lineHeight=GlassTokens.scaledSp(18.sp,appearance),letterSpacing=0.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
            if(inline && !PreviewPresentation.isApplication(result) && result.subtitle.isNotBlank())Text(result.subtitle,
                Modifier.weight(1f),color=colors.onSurfaceVariant,fontSize=GlassTokens.scaledSp(GlassTokens.secondaryText,appearance),
                maxLines=1,overflow=TextOverflow.Ellipsis)
          }
            val subtitle=if(PreviewPresentation.isApplication(result))"Aplicación" else result.subtitle.takeIf{it.isNotBlank()} ?: metadata
            if(!inline)Text(subtitle,color=colors.onSurfaceVariant,fontSize=GlassTokens.scaledSp(GlassTokens.secondaryText,appearance),
                maxLines=1,overflow=TextOverflow.Ellipsis)
        }
        if(inline && !playing)Text(metadata,
            Modifier.padding(start=(10f*d).dp).widthIn(max=104.dp),color=colors.onSurfaceVariant,
            fontSize=GlassTokens.scaledSp(GlassTokens.metadataText,appearance),maxLines=1,overflow=TextOverflow.Ellipsis)
        if(playing)Text("En reproducción",Modifier.padding(start=(10f*d).dp).widthIn(max=124.dp),color=colors.onSurfaceVariant,
            fontSize=GlassTokens.scaledSp(GlassTokens.metadataText,appearance),maxLines=1,overflow=TextOverflow.Ellipsis)
        if (favorite)IosCommandIcon(CommandIcons.Star,"Favorita",Modifier.padding(horizontal=6.dp).size(14.dp),tint=colors.primary)
        if(!inline) Box(Modifier.size(GlassTokens.hit),contentAlignment=Alignment.Center) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides (if(inline)0.dp else GlassTokens.hit)) {
        if(selected || hovered) IconButton(onClick = {onInteraction();onDetails()}, modifier = Modifier.size(if(inline)32.dp else GlassTokens.hit)) {
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
}

@OptIn(ExperimentalLayoutApi::class)
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
        FlowRow {
            TextButton(modifier=Modifier.heightIn(min=48.dp),onClick = onCopy) { Text("Copiar") }
            if(onShare!=null)TextButton(modifier=Modifier.heightIn(min=48.dp),onClick=onShare){Text("Compartir")}
            if (card.url != null) TextButton(modifier=Modifier.heightIn(min=48.dp),onClick = onLink) { Text("Abrir fuente / mapa") }
        }
    }
}

