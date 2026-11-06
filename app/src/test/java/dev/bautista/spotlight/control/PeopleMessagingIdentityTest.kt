package dev.bautista.spotlight.control

import android.app.Application
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33],application=Application::class)
class PeopleMessagingIdentityTest {
    @Test fun oneContactWithTwoNumbersAndHomonymsRequireExplicitSelection() {
        assertNull(People.uniqueExact(listOf(Person(1,"Alex","+541100000001"),Person(1,"Alex","+541100000002")),"Alex"))
        assertNull(People.uniqueExact(listOf(Person(1,"Alex","+541100000001"),Person(2,"Alex","+541100000002")),"Alex"))
    }
    @Test fun internationalNumberNeverSilentlyDropsLettersOrSpecialCodes() {
        val context=RuntimeEnvironment.getApplication()
        for(raw in listOf("+541100000001abc", "+541100000001#", "+54+1100000001", "+٥٤١١٠٠٠٠٠٠٠١", "+000000000000")) {
            assertNull("Invalid phone input was silently accepted",People.international(context,raw))
        }
        assertEquals("+541100000001",People.international(context,"+54 (11) 0000-0001"))
    }
    @Test fun personalAndBusinessIntentsFixThePackageNumberAndExactText() {
        val context=RuntimeEnvironment.getApplication()
        for((channel,pkg) in listOf("whatsapp" to "com.whatsapp","whatsapp_business" to "com.whatsapp.w4b")) {
            val intent=People.messageIntent(context,channel,"+541100000001","¿Llegaste? & café")!!
            assertEquals(pkg,intent.`package`)
            assertEquals("/541100000001",intent.data!!.path)
            assertEquals("¿Llegaste? & café",intent.data!!.getQueryParameter("text"))
        }
        assertNull(People.messageIntent(context,"whatsapp","+541100000001abc","Texto"))
    }
}
