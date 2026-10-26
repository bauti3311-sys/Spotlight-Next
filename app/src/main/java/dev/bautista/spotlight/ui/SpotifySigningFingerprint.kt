package dev.bautista.spotlight.ui

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

/** Public certificate fingerprint for the user's own Spotify Dashboard registration. */
@Suppress("DEPRECATION")
internal fun spotifySigningFingerprint(context:Context):String?=runCatching {
 val pm=context.packageManager
 val signatures=if(Build.VERSION.SDK_INT>=28)
  pm.getPackageInfo(context.packageName,PackageManager.GET_SIGNING_CERTIFICATES).signingInfo?.apkContentsSigners
 else pm.getPackageInfo(context.packageName,PackageManager.GET_SIGNATURES).signatures
 val certificate=signatures?.singleOrNull()?.toByteArray() ?: return@runCatching null
 MessageDigest.getInstance("SHA-1").digest(certificate).joinToString(":"){"%02X".format(it.toInt() and 255)}
}.getOrNull()
