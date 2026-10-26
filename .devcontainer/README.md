# Persistent Android build host
Create a 2-core Codespace on the authorized feature branch, without a prebuild.
The official Java 17 devcontainer image supplies the JDK. Run explicitly:
`bash .devcontainer/setup-android.sh`
This probes Maven Central and Google Maven before downloads, pins Gradle 8.10.2 and SDK/build tools 35 archives, and verifies their recorded checksums.
Gradle User Home and SDK live under /workspaces/.spotlight-tools, outside Git, surviving stop/start and container rebuild. They do not survive deleting the Codespace. Review retention settings and stop it after work; stopped storage still consumes Codespaces allowance.
No build/test/Actions is started automatically. Android license/platform-tools setup remains required if not present. This configuration is prepared, not tested inside a Codespace.
Check actual quota before creation; Codespaces compute/storage are independent of Actions minutes/artifacts. No prebuild configuration is added.
No signing key, tokens or sessions are copied. Do not build a distributable Preview until the historical 885838aa certificate is securely available and verified. Never use a generated fallback certificate.
After setup, compile directed tests and explicitly include AdaptiveResultsGeometryTest, AppActionsSectionTest, CompactResultsTest and NextPreferencesTest; the old ui group alone does not cover all ten new cases.
