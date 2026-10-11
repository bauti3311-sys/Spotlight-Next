package dev.bautista.spotlight.advanced

import org.junit.Assert.*
import org.junit.Test
class AdvancedStateTest {
    @Test fun absentAndStoppedRemainStandard(){assertEquals(ConnectionStatus.ABSENT,AdvancedPolicy.state(false,false,false,false,null,null).status);assertFalse(AdvancedPolicy.state(true,false,false,false,null,null).available)}
    @Test fun authorizationAndDenialAreDistinct(){assertEquals(ConnectionStatus.AUTHORIZATION,AdvancedPolicy.state(true,true,false,false,13,null).status);assertEquals(ConnectionStatus.DENIED,AdvancedPolicy.state(true,true,false,true,13,null).status)}
    @Test fun onlyAuthorizedShellEnablesAdvanced(){assertTrue(AdvancedPolicy.state(true,true,true,false,13,2000).available);assertFalse(AdvancedPolicy.state(true,true,true,false,13,0).available);assertFalse(AdvancedPolicy.state(true,true,true,false,12,2000).available)}
    @Test fun disconnectionDoesNotRetainPrivilege(){assertEquals(ConnectionStatus.DISCONNECTED,AdvancedPolicy.state(true,false,false,false,null,null,true).status);assertFalse(AdvancedPolicy.state(true,false,false,false,null,null,true).available)}
    @Test fun settingsAreBoundedAndAllowlisted(){assertTrue(AdvancedPolicy.validSetting("screen_brightness","120"));assertFalse(AdvancedPolicy.validSetting("screen_brightness","0"));assertFalse(AdvancedPolicy.validSetting("adb_enabled","1"));assertFalse(AdvancedPolicy.validSetting("screen_off_timeout","9999999"))}
    @Test fun administrativeTargetsAreConservative(){assertTrue(AdvancedPolicy.canStop("com.spotify.music",false,emptySet()));assertFalse(AdvancedPolicy.canStop("com.android.systemui",false,emptySet()));assertFalse(AdvancedPolicy.canStop("com.example.ime",false,setOf("com.example.ime")));assertFalse(AdvancedPolicy.canStop("com.example.app;reboot",false,emptySet()));assertFalse(AdvancedPolicy.canStop("com.example.system",true,emptySet()))}
}
