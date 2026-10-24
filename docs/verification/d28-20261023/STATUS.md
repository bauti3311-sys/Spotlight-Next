# D28 implementation checkpoint (not completion)

Base0c8b0a8d. Code: typed whole-argument parameters (TEXT/NUMBER/PACKAGE/CHOICE), destination validation, reusable templates, graph bindings, lossless coexistence with legacy records, DataStore transactions, revision comparison and name/alias conflicts. Creation/editing from D27; Saved review freezes parameters and loads D27 without executing. Omissions/relative settings must be resolved before saving a template; no relative-baseline persistence claimed.

17 new cases written:13domain/storage/D27 adapter,4Compose. **None executed or compiled**: one recovery attempt failed downloading Gradle8.10.2 with `java.net.SocketException: Network is unreachable`. No retries. Local directed-script preflight exited2: SDK35 absent. `bash -n scripts/test-d28-directed.sh` passed (shell syntax only).

Affordable alternative: run `bash scripts/test-d28-directed.sh` on an already provisioned local host/Codespace with JDK17, Gradle8.10.2, SDK35 and cached dependencies. Offline, one Gradle worker, directed classes only; no APK and no GitHub Actions dispatch. UI regressions remain listed first. No Actions authorized or started. No physical/Gemini/Spotify/Cast verification.

D28 remains PARTIAL and UI08f19055 remains pending. Matrix9 complete/25 insufficient/6partial; Preview44/45 unchanged. Next execution gate: UI regressions, CommandTemplatesTest/CommandTemplateFlowTest, native-command neighbors; fix any compile or runtime failure before promoting D28 or packaging a Preview.
