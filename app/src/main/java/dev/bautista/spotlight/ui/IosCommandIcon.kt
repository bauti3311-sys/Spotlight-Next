package dev.bautista.spotlight.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import dev.bautista.spotlight.R

/** Icons8 iOS50, attributed in Settings. Application art never passes through this resolver. */
internal object IosIconMetrics {
 val metadata=16.dp
 val command=20.dp
 val navigation=24.dp
 val touchTarget=48.dp
 // PNG artwork is used intact, monochrome-tinted; no synthetic stroke/alpha filters.
}
internal fun iosAssetFor(icon:ImageVector):Int?=when(icon.name) {
    "Search","HugeSearch","FindInPage" -> R.drawable.inspector_ios_search
    "Mic","HugeMic" -> R.drawable.inspector_ios_mic
    "Close","HugeClose" -> R.drawable.inspector_ios_close
    "Settings","HugeSettings" -> R.drawable.inspector_ios_actions
    "SpotlightSaved","PhosphorSaved" -> R.drawable.inspector_ios_saved
    "PhosphorPdf","PictureAsPdf" -> R.drawable.inspector_ios_pdf
    "Event","PhosphorCalendar" -> R.drawable.inspector_ios_calendar
    "Cloud","PhosphorWeather" -> R.drawable.inspector_ios_weather
    "ChatBubbleOutline","Forum","PhosphorChat" -> R.drawable.inspector_ios_chat
    "Star","StarBorder","PhosphorStar" -> R.drawable.inspector_ios_star
    "Display" -> R.drawable.inspector_ios_display
    "Keyboard" -> R.drawable.inspector_ios_keyboard
    "Apps" -> R.drawable.inspector_ios_grid
    "AutoAwesome" -> R.drawable.inspector_ios_sparkle
    "Sliders" -> R.drawable.inspector_ios_sliders
    "Shield" -> R.drawable.inspector_ios_shield
    "MoreHoriz","Layers" -> R.drawable.inspector_ios_more
    "Info" -> R.drawable.inspector_ios_info
    "Arrow" -> R.drawable.inspector_ios_open
    "Pin" -> R.drawable.inspector_ios_pin
    "ContentCopy","Snippet" -> R.drawable.inspector_ios_copy
    "Play" -> R.drawable.inspector_ios_play
    "Pause" -> R.drawable.inspector_ios_pause
    "Next","SkipNext" -> R.drawable.inspector_ios_next
    "Previous","SkipPrevious" -> R.drawable.inspector_ios_previous
    "Chevron" -> R.drawable.inspector_ios_chevron
    "Reply" -> R.drawable.inspector_ios_reply
    "Share" -> R.drawable.inspector_ios_share
    "Image" -> R.drawable.inspector_ios_image
    "Folder" -> R.drawable.inspector_ios_folder
    "Description" -> R.drawable.inspector_ios_document
    "MusicNote" -> R.drawable.inspector_ios_music
    "Videocam" -> R.drawable.inspector_ios_video
    "Delete" -> R.drawable.inspector_ios_delete
    "Timer" -> R.drawable.inspector_ios_timer
    "VolumeUp" -> R.drawable.inspector_ios_volume
    "VolumeDown" -> R.drawable.inspector_ios_volume_low
    "Mute" -> R.drawable.inspector_ios_mute
    "Person" -> R.drawable.inspector_ios_person
    "Wifi" -> R.drawable.inspector_ios_wifi
    "Bluetooth" -> R.drawable.inspector_ios_bluetooth
    "Language","TravelExplore" -> R.drawable.inspector_ios_globe
    "Calculate" -> R.drawable.inspector_ios_calculator
    "EditNote" -> R.drawable.inspector_ios_edit
    "Sun" -> R.drawable.inspector_ios_sun
    "ListChecks" -> R.drawable.inspector_ios_list
    "LockOpen" -> R.drawable.inspector_ios_unlock
    "Location" -> R.drawable.inspector_ios_location
    "Phone" -> R.drawable.inspector_ios_phone
    "Mail" -> R.drawable.inspector_ios_mail
    "Bolt" -> R.drawable.inspector_ios_bolt
    "Battery" -> R.drawable.inspector_ios_battery
    "Storage" -> R.drawable.inspector_ios_storage
    "Accessibility" -> R.drawable.inspector_ios_accessibility
    "Sport","Sports" -> R.drawable.inspector_ios_sport
    "History" -> R.drawable.inspector_ios_history
    else -> null // Preserve an unknown extension's truthful licensed symbol, never substitute an unrelated action.
}
@Composable internal fun IosCommandIcon(icon:ImageVector,description:String?,modifier:Modifier=Modifier.size(IosIconMetrics.command),tint:Color=LocalContentColor.current) {
 val asset=iosAssetFor(icon)
 if(asset!=null)Icon(painterResource(asset),description,modifier,tint=tint)
 else Icon(icon,description,modifier,tint=tint)
}
