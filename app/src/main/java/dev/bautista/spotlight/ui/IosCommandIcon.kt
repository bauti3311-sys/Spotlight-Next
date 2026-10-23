package dev.bautista.spotlight.ui

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import dev.bautista.spotlight.R

/** Licensed Icons8 iOS artwork for the palette's common commands.
 * Installed application artwork is owned by ResultIcon/PackageManager.
 * Unmigrated specialist commands keep their existing icon rather than a misleading substitute.
 */
@Composable internal fun IosCommandIcon(icon:ImageVector,description:String?,modifier:Modifier=Modifier,tint:Color=LocalContentColor.current) {
    val asset=when(icon) {
        CommandIcons.Search->R.drawable.inspector_ios_search
        CommandIcons.Close->R.drawable.inspector_ios_close
        CommandIcons.Mic->R.drawable.inspector_ios_mic
        CommandIcons.MoreHoriz,CommandIcons.Layers->R.drawable.inspector_ios_more
        CommandIcons.Info->R.drawable.inspector_ios_info
        CommandIcons.Arrow->R.drawable.inspector_ios_open
        CommandIcons.Star,CommandIcons.StarBorder->R.drawable.inspector_ios_star
        CommandIcons.Pin->R.drawable.inspector_ios_pin
        CommandIcons.ContentCopy->R.drawable.inspector_ios_copy
        CommandIcons.Play->R.drawable.inspector_ios_play
        CommandIcons.Pause->R.drawable.inspector_ios_pause
        CommandIcons.Next->R.drawable.inspector_ios_next
        CommandIcons.Previous->R.drawable.inspector_ios_previous
        CommandIcons.Chevron->R.drawable.inspector_ios_chevron
        CommandIcons.Reply->R.drawable.inspector_ios_reply
        CommandIcons.Share->R.drawable.inspector_ios_share
        else->null
    }
    if(asset!=null)Icon(painterResource(asset),description,modifier,tint=tint)
    else Icon(icon,description,modifier,tint=tint)
}
