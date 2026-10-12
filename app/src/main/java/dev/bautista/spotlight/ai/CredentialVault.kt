package dev.bautista.spotlight.ai

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface AiCredentials { fun read(provider:String):String?; fun contains(provider:String):Boolean }
/** Keys never enter DataStore, backups, URLs or logging. Call read/save on IO. */
class CredentialVault(context:Context):AiCredentials {
    private val directory=File(context.noBackupFilesDir,"credentials")
    private fun file(provider:String):File { require(provider in setOf("openai","gemini","thesportsdb"));return File(directory,"$provider.enc") }
    @Synchronized private fun key():SecretKey {
        val store=KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey("spotlight.ai.v1",null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder("spotlight.ai.v1",KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    override fun contains(provider:String)=runCatching {file(provider).exists()}.getOrDefault(false)
    override fun read(provider:String):String? {
        val f=file(provider);if(!f.exists())return null
        val bytes=AtomicFile(f).readFully();require(bytes.size>12)
        return Cipher.getInstance("AES/GCM/NoPadding").run { init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,bytes.copyOfRange(0,12)));String(doFinal(bytes.copyOfRange(12,bytes.size)),Charsets.UTF_8) }
    }
    fun save(provider:String,value:String) {
        val clean=value.trim();require(clean.length in 1..4096 && !clean.contains('\n') && !clean.contains('\r')) {"Clave inválida"}
        directory.mkdirs();val atomic=AtomicFile(file(provider))
        val cipher=Cipher.getInstance("AES/GCM/NoPadding").apply {init(Cipher.ENCRYPT_MODE,key())}
        val output=atomic.startWrite()
        try {output.write(cipher.iv+cipher.doFinal(clean.toByteArray()));atomic.finishWrite(output)} catch(e:Exception){atomic.failWrite(output);throw e}
    }
    fun delete(provider:String) {AtomicFile(file(provider)).delete()}
}
