package android.webkit;

/** Test-only SDK error fixture: WebResourceError has a package-private constructor. */
public final class LocalRaycastTestWebError extends WebResourceError {
    public LocalRaycastTestWebError() { super(); }
    @Override public int getErrorCode() { return -2; }
    @Override public CharSequence getDescription() { return "Test load error"; }
}
