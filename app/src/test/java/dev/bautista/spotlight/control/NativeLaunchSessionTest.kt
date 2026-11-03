package dev.bautista.spotlight.control

import android.content.*
import android.content.pm.*
import android.net.Uri
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[33])
class NativeLaunchSessionTest {
    private val app=RuntimeEnvironment.getApplication()
    private val manager=Shadows.shadowOf(app.packageManager)
    private val target=ComponentName("test.target","test.target.Main")
    private val browser=ComponentName("test.browser","test.browser.Main")
    private val web=Intent(Intent.ACTION_VIEW,Uri.parse("https://example.org/review?q=original"))
    private val primary get()=Intent(Intent.ACTION_MAIN).setComponent(target)
    private val calls=mutableListOf<Intent>()
    private var failure:Exception?=null
    private var attempts=0
    private val context=object:ContextWrapper(app) {
        override fun startActivity(intent:Intent) {
            attempts++
            failure?.let{throw it}
            calls+=Intent(intent)
            super.startActivity(intent)
        }
    }
    private fun install(component:ComponentName=target,enabled:Boolean=true,exported:Boolean=true,permission:String?=null) {
        val info=ApplicationInfo().apply{packageName=component.packageName;this.enabled=true;nonLocalizedLabel="Destino"}
        val activity=ActivityInfo().apply{packageName=component.packageName;name=component.className;applicationInfo=info
            this.enabled=enabled;this.exported=exported;this.permission=permission}
        manager.installPackage(PackageInfo().apply{packageName=component.packageName;applicationInfo=info;activities=arrayOf(activity)})
        if(component==browser)manager.addResolveInfoForIntent(web,ResolveInfo().apply{activityInfo=activity})
    }
    private fun proposal(session:NativeLaunchSession):LaunchAlternative {
        install(browser)
        assertFalse(session.launch(primary,web,"origin",true))
        assertTrue(calls.isEmpty())
        return requireNotNull(session.issue?.alternative)
    }
    @Test fun exactInstalledActivityIsPinnedAndRequestedOnce() {
        install();val session=NativeLaunchSession(context)
        assertTrue(session.launch(primary,null,"origin",true))
        assertEquals(target,calls.single().component)
        assertFalse(session.launch(primary,null,"origin",true));assertEquals(1,calls.size)
    }
    @Test fun disabledPrivateMissingAndRestrictedActivitiesNeverDispatch() {
        val session=NativeLaunchSession(context)
        assertFalse(session.launch(primary,null,"missing",true));assertEquals(LaunchProblem.UNAVAILABLE,session.issue?.problem)
        install(enabled=false)
        assertFalse(session.launch(primary,null,"disabled",true));assertEquals(LaunchProblem.UNAVAILABLE,session.issue?.problem)
        install(exported=false)
        assertFalse(session.launch(primary,null,"private",true));assertEquals(LaunchProblem.PERMISSION_REQUIRED,session.issue?.problem)
        install(permission="test.permission.RESTRICTED")
        assertFalse(session.launch(primary,null,"permission",true));assertEquals(LaunchProblem.PERMISSION_REQUIRED,session.issue?.problem)
        assertTrue(calls.isEmpty())
    }
    @Test fun removedPackageIsRevalidatedBeforeDispatch() {
        install();val session=NativeLaunchSession(context)
        assertNull(IntegrationRegistry(context).launchAccess(primary).problem)
        manager.removePackage(target.packageName)
        assertFalse(session.launch(primary,null,"origin",true));assertTrue(calls.isEmpty())
    }
    @Test fun securityExceptionIsSanitizedAndNeverRetried() {
        install();failure=SecurityException("private-token-and-path")
        val session=NativeLaunchSession(context)
        assertFalse(session.launch(primary,null,"origin",true))
        assertEquals(LaunchProblem.PERMISSION_REQUIRED,session.issue?.problem)
        assertFalse(session.issue.toString().contains("private-token"))
        failure=null
        assertFalse(session.launch(primary,null,"origin",true));assertTrue(calls.isEmpty());assertEquals(1,attempts)
    }
    @Test fun unknownOutcomeOffersNoAutomaticOrManualFallback() {
        install();install(browser);failure=IllegalStateException("possibly-dispatched")
        val session=NativeLaunchSession(context)
        assertFalse(session.launch(primary,web,"origin",true))
        assertEquals(LaunchProblem.UNKNOWN_OUTCOME,session.issue?.problem)
        assertNull(session.issue?.alternative)
        failure=null;assertFalse(session.launch(primary,web,"origin",true));assertTrue(calls.isEmpty())
    }
    @Test fun activityNotFoundAfterValidationOnlyOffersAlternative() {
        install();install(browser);failure=ActivityNotFoundException("fixture")
        val session=NativeLaunchSession(context)
        assertFalse(session.launch(primary,web,"origin",true));assertEquals(1,attempts)
        val proposal=requireNotNull(session.issue?.alternative)
        assertTrue(calls.isEmpty());failure=null
        assertTrue(session.confirm(proposal.id,"origin"));assertEquals(browser,calls.single().component)
        assertEquals(2,attempts)
    }
    @Test fun fallbackRequiresCurrentProposalAndDispatchesOnce() {
        val session=NativeLaunchSession(context);val proposal=proposal(session)
        assertFalse(session.confirm("unknown-id","origin"));assertTrue(calls.isEmpty())
        assertTrue(session.confirm(proposal.id,"origin"));assertEquals(browser,calls.single().component)
        assertFalse(session.confirm(proposal.id,"origin"));assertEquals(1,calls.size)
    }
    @Test fun cancelledAndDisposedProposalsNeverDispatch() {
        val session=NativeLaunchSession(context);val proposal=proposal(session)
        session.cancel();assertFalse(session.confirm(proposal.id,"origin"))
        val next=proposal(session)
        session.dispose();assertFalse(session.confirm(next.id,"origin"))
        assertFalse(session.launch(primary,web,"origin",true));assertTrue(calls.isEmpty())
    }
    @Test fun changedContextMakesOldApprovalStale() {
        val session=NativeLaunchSession(context);val proposal=proposal(session)
        assertFalse(session.confirm(proposal.id,"changed-query-target-or-payload"))
        assertEquals(LaunchProblem.STALE_TARGET,session.issue?.problem);assertTrue(calls.isEmpty())
    }
    @Test fun changedDefaultHandlerRequiresFreshReview() {
        val session=NativeLaunchSession(context);val proposal=proposal(session)
        val next=ComponentName("test.other","test.other.Main");install(next)
        val activity=app.packageManager.getActivityInfo(next,0)
        manager.removeResolveInfosForIntent(web,browser.packageName)
        manager.addResolveInfoForIntent(web,ResolveInfo().apply{activityInfo=activity})
        assertFalse(session.confirm(proposal.id,"origin"))
        assertEquals(LaunchProblem.STALE_TARGET,session.issue?.problem);assertTrue(calls.isEmpty())
    }
    @Test fun revokedAlternativePermissionBlocksConfirmation() {
        val session=NativeLaunchSession(context);val proposal=proposal(session)
        install(browser,permission="test.permission.REVOKED")
        assertFalse(session.confirm(proposal.id,"origin"));assertEquals(LaunchProblem.PERMISSION_REQUIRED,session.issue?.problem)
        assertTrue(calls.isEmpty())
    }
    @Test fun reviewedPayloadIsAnIndependentSnapshot() {
        web.putExtra("reviewed-text","original")
        val session=NativeLaunchSession(context);val proposal=proposal(session)
        web.putExtra("reviewed-text","changed")
        assertTrue(session.confirm(proposal.id,"origin"))
        assertEquals("https://example.org/review?q=original",calls.single().data.toString())
        assertEquals("original",calls.single().getStringExtra("reviewed-text"))
    }
    @Test fun modifiedProposalCannotExecuteDifferentPayload() {
        val session=NativeLaunchSession(context);val proposal=proposal(session)
        proposal.intent.putExtra("unreviewed","changed")
        assertFalse(session.confirm(proposal.id,"origin"))
        assertEquals(LaunchProblem.STALE_TARGET,session.issue?.problem);assertTrue(calls.isEmpty())
    }
    @Test fun revokedReadGrantBlocksDocumentLaunch() {
        install();val uri=Uri.parse("content://test.documents/document/1")
        val intent=primary.setData(uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        var granted=true
        val permissions=object:ContextWrapper(context) {
            override fun checkUriPermission(uri:Uri,pid:Int,uid:Int,flags:Int)=
                if(granted)PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
        }
        assertNull(IntegrationRegistry(permissions).launchAccess(intent).problem)
        granted=false
        val session=NativeLaunchSession(permissions)
        assertFalse(session.launch(intent,null,"origin",false))
        assertEquals(LaunchProblem.PERMISSION_REQUIRED,session.issue?.problem);assertTrue(calls.isEmpty())
    }
    @Test fun revokedWriteGrantBlocksDispatchBeforeEffect() {
        install()
        val denied=object:ContextWrapper(context) {
            override fun checkUriPermission(uri:Uri,pid:Int,uid:Int,flags:Int)=PackageManager.PERMISSION_DENIED
        }
        val intent=primary.setData(Uri.parse("content://test.documents/write/1")).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        val session=NativeLaunchSession(denied)
        assertFalse(session.launch(intent,null,"origin",false))
        assertEquals(LaunchProblem.PERMISSION_REQUIRED,session.issue?.problem);assertTrue(calls.isEmpty())
    }
    @Test fun boundedIntentRejectsOversizedPayloadWithoutDispatch() {
        install();val session=NativeLaunchSession(context)
        assertFalse(session.launch(primary.putExtra("text","x".repeat(300000)),null,"origin",true))
        assertEquals(LaunchProblem.INVALID_INPUT,session.issue?.problem);assertTrue(calls.isEmpty())
    }
}
