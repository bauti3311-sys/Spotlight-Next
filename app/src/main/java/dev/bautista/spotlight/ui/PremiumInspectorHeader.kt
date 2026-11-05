package dev.bautista.spotlight.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bautista.spotlight.search.Result

/** Pure presentation of existing selection; no invented app/account/media capability. */
@Composable internal fun PremiumInspectorHeader(item:Result,kind:String,subtitle:String,
    compact:Boolean=false,onSearch:(()->Unit)?=null,application:Boolean=true) {
    val colors=MaterialTheme.colorScheme
    val shape=RoundedCornerShape(18.dp)
    Column(Modifier.fillMaxWidth().background(colors.surfaceContainerHigh,shape)
        .border(.75.dp,colors.outlineVariant.copy(alpha=.65f),shape)
        .padding(if(compact)14.dp else 20.dp),verticalArrangement=Arrangement.spacedBy(if(compact)10.dp else 16.dp)) {
        PremiumSectionLabel(kind)
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(if(compact)48.dp else 64.dp).background(colors.primaryContainer,RoundedCornerShape(15.dp)),contentAlignment=Alignment.Center) {
                ResultIcon(item,Modifier.size(if(application)44.dp else 30.dp),colors.primary)
            }
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                Text(item.title,Modifier.then(if(application)Modifier.testTag("inspector-title") else Modifier).semantics{heading()},
                    style=if(compact)MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight=FontWeight.SemiBold,maxLines=2,overflow=TextOverflow.Ellipsis)
                Text(subtitle,style=MaterialTheme.typography.bodySmall,color=colors.onSurfaceVariant,maxLines=2,overflow=TextOverflow.Ellipsis)
            }
            onSearch?.let{IconButton(onClick=it,modifier=Modifier.size(48.dp)){
                IosCommandIcon(CommandIcons.Search,"Buscar dentro de Spotify",Modifier.size(20.dp),tint=colors.primary)
            }}
        }
    }
}

@Composable internal fun PremiumSectionLabel(label:String) {
    Text(label.uppercase(),style=MaterialTheme.typography.labelSmall,
        fontSize=GlassTokens.scaledSp(10.sp,LocalAppearance.current),letterSpacing=1.4.sp,
        fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.primary)
}
