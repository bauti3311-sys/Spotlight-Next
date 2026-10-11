package dev.bautista.spotlight.control

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlinx.coroutines.*

data class Person(val id:Long,val name:String,val address:String)
object People {
    fun uniqueExact(choices:List<Person>,query:String):Person?=choices.filter{dev.bautista.spotlight.search.normalize(it.name)==dev.bautista.spotlight.search.normalize(query)}.singleOrNull()
    fun allowed(context:Context)=ContextCompat.checkSelfPermission(context,Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED
    suspend fun find(context:Context,query:String,email:Boolean=false):List<Person> = withContext(Dispatchers.IO) {
        if(!allowed(context))return@withContext emptyList()
        val base=if(email)ContactsContract.CommonDataKinds.Email.CONTENT_URI else ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val filter=if(email)ContactsContract.CommonDataKinds.Email.CONTENT_FILTER_URI else ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI
        val uri=if(query.isBlank())base else Uri.withAppendedPath(filter,Uri.encode(query.trim()))
        val address=if(email)ContactsContract.CommonDataKinds.Email.ADDRESS else ContactsContract.CommonDataKinds.Phone.NUMBER
        val out=mutableListOf<Person>()
        context.contentResolver.query(uri,arrayOf(ContactsContract.Data.CONTACT_ID,ContactsContract.Data.DISPLAY_NAME,address),null,null,ContactsContract.Data.DISPLAY_NAME+" COLLATE LOCALIZED ASC")?.use { c ->
            while(c.moveToNext() && out.size<40){ensureActive();out+=Person(c.getLong(0),c.getString(1) ?: "Contacto",c.getString(2) ?: "")}
        }
        out.filter{it.address.isNotBlank()}.distinctBy{it.id to it.address}
    }
    fun international(context:Context,raw:String):String? {
        val digits=raw.filter(Char::isDigit)
        if(raw.trim().startsWith('+') && digits.length in 8..15 && digits.first()!='0')return "+$digits"
        val country=runCatching { context.getSystemService(TelephonyManager::class.java).networkCountryIso }.getOrNull()?.takeIf{it.length==2} ?: Locale.getDefault().country.takeIf{it.length==2} ?: "AR"
        return PhoneNumberUtils.formatNumberToE164(raw,country.uppercase(Locale.ROOT))
    }
    fun messageIntent(context:Context,channel:String,address:String,text:String):Intent? = when(channel) {
        "whatsapp","whatsapp_business"->international(context,address)?.let { Intent(Intent.ACTION_VIEW,Uri.parse("https://wa.me/${it.drop(1)}?text=${Uri.encode(text)}")).setPackage(if(channel=="whatsapp_business")"com.whatsapp.w4b" else "com.whatsapp") }
        "sms"->Intent(Intent.ACTION_SENDTO,Uri.fromParts("smsto",address,null)).putExtra("sms_body",text)
        "email"->Intent(Intent.ACTION_SENDTO,Uri.fromParts("mailto",address,null)).putExtra(Intent.EXTRA_TEXT,text)
        "telegram"->Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,text).setPackage("org.telegram.messenger")
        else->if(channel.startsWith("share:")) Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,text).setPackage(channel.substringAfter(":")) else null
    }
}
