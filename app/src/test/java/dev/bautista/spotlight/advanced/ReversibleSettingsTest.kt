package dev.bautista.spotlight.advanced

import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test
class ReversibleSettingsTest {
    private class Backend:AdvancedBackend {
        var value="120";var writes=0;var denied=false;var available=true
        override suspend fun read(key:String)=value
        override suspend fun canWrite()=available
        override suspend fun write(key:String,value:String,confirmed:Boolean){check(confirmed);check(!denied);writes++;this.value=value}
        override suspend fun stop(packageName:String,confirmed:Boolean){check(confirmed)}
    }
    private class Journal:RecoveryJournal {val values=mutableMapOf<String,String>();override fun original(key:String)=values[key];override fun remember(key:String,value:String){values.putIfAbsent(key,value)};override fun clear(key:String){values.remove(key)};override fun entries()=values.toMap()}
    @Test fun noMutationWithoutConfirmation()=runBlocking {val b=Backend();val j=Journal();val settings=ReversibleSettings(b,j);assertTrue(runCatching{settings.apply("screen_brightness","220",false)}.isFailure);assertEquals(0,b.writes);assertTrue(j.entries().isEmpty())}
    @Test fun originalSurvivesMultipleChangesAndRestores()=runBlocking{val b=Backend();val j=Journal();val settings=ReversibleSettings(b,j);settings.apply("screen_brightness","220",true);settings.apply("screen_brightness","60",true);assertEquals("120",j.original("screen_brightness"));settings.restore("screen_brightness",true);assertEquals("120",b.value);assertTrue(j.entries().isEmpty())}
    @Test fun binderFailurePreservesRecovery()=runBlocking{val b=Backend();b.denied=true;val j=Journal();assertTrue(runCatching{ReversibleSettings(b,j).apply("screen_brightness","220",true)}.isFailure);assertEquals("120",j.original("screen_brightness"));assertEquals(0,b.writes)}
    @Test fun unavailablePermissionAndInvalidInputsDoNothing()=runBlocking{val b=Backend();b.available=false;val j=Journal();val settings=ReversibleSettings(b,j);assertTrue(runCatching{settings.apply("screen_brightness","220",true)}.isFailure);assertTrue(runCatching{settings.apply("adb_enabled","1",true)}.isFailure);assertEquals(0,b.writes);assertTrue(j.entries().isEmpty())}
    @Test fun restoreRequiresExplicitConsent()=runBlocking{val b=Backend();val j=Journal();j.remember("screen_brightness","60");assertTrue(runCatching{ReversibleSettings(b,j).restore("screen_brightness",false)}.isFailure);assertEquals(0,b.writes);assertEquals("60",j.original("screen_brightness"))}
    @Test fun cancelledConnectionDoesNotProceed()=runBlocking{val b=object:AdvancedBackend{override suspend fun canWrite():Boolean=throw CancellationException();override suspend fun read(key:String)="120";override suspend fun write(key:String,value:String,confirmed:Boolean){fail("Must not run")};override suspend fun stop(packageName:String,confirmed:Boolean){fail("Must not run")}};val j=Journal();assertTrue(runCatching{ReversibleSettings(b,j).apply("screen_brightness","220",true)}.exceptionOrNull() is CancellationException);assertTrue(j.entries().isEmpty())}
}
