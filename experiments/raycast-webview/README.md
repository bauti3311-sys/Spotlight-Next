# Spotlight Next — recovered WebView experiment

Current source checkpoint (2026-11-03): bridge v2, with native contract tests and
browser validation; no new APK built for this change. The delivered Prototype58
below is historical and uses its recorded source8c7128e. Actual Android overlay
validation remains pending. See [v2 contract](../../docs/RAYCAST-BRIDGE-V2.md) and
[current evidence](../../docs/verification/bridge-contracts-20261102/README.md).

The user's 28 October 2026 instruction explicitly replaces the earlier native-only visual restriction for this experiment. Reuse HugoRCD's approved clone, keep OverlayService/WindowManager and existing Kotlin search, and retain Compose recovery. Do not merge or replace earlier Preview packages.

## Recovery facts

- Remote current source: `4fc33f2c918d3fe53a8bad7ac6571f2757630066`, `feat/antigravity-tablet-20261026`.
- Existing Codespace: `ubiquitous-waffle-7vgr7gxxgjrw3x7qv`.
- Local previous worktree: `/workspaces/.spotlight-tools/nuxt-prototype58`, clean at that SHA on `feat/nuxt-webview-prototype-20261028`; branch was not published.
- Downloaded original source: `/workspaces/.spotlight-tools/raycast-nuxt-upstream`. Evidence58 contained the Nuxt UI registry inspection. No Android integration was present in the old worktree.
- Foreign reservation: `067da78b873f81433a97069923d9f26ea68c4761`, owner `work-nuxt-prototype-20261028-1c621d66`, checkpoint `4fc33f2c`, state active. NOT transferred, edited or released.
- This recovery is isolated on `feat/nuxt-webview-recovery-20261028`, as explicitly authorized by the user when ownership blocks the previous area. It does not claim the shared reservation or alter the old worktree.

## Frontend provenance

Original: https://github.com/HugoRCD/raycast-nuxt-ui-clone
Upstream snapshot: `600890f5a0b0f6dda95ca64da599f4c120b1df94`.
Reference: https://raycast-nuxt-ui.hrcd.fr/
Apache-2.0 original LICENSE and attribution preserved in this directory.

The experimental `https://pkg.pr.new/@nuxt/ui@4457` dependency is replaced by official Nuxt UI 4.0.1. Its actual source includes CommandPalette's footer slot. Nuxt 4.1.3 is required by UI 4; keeping Nuxt 3 merely disables the module and fails stylesheet generation. No dependency resolution is forced. `package-lock.json` freezes the successful installation.

CommandConsole, CommandFooter, ActionsDropdown, SettingsPopover and the original palette CSS are retained/adapted. The wallpaper and macOS menu bar are omitted; palette backgrounds, blur, shadows, row structure and footer remain. Demo commands/providers/assets are excluded. Icons are bundled; fonts/API servers are not required. Nuxt app-manifest polling is disabled. Fidelity is NOT certified until equal-size reference and actual Android WebView screenshots exist.

## Native integration

Only package suffix `.webviewprototype` uses LocalRaycastWebView inside the existing OverlayService/WindowManager host. Standard Compose code remains and can be restored from the original settings popover or on a renderer error.

Frontend files are packaged in `app/src/webviewPrototype/assets/raycast-web`. WebView uses synthetic `https://spotlight.local/`, intercepts local asset reads, blocks file/content/network access and all navigation, and supplies a restrictive CSP. No addJavascriptInterface or privileged Android object is exposed.

A MessagePort is transferred only to that exact origin. Protocol v2 uses sessions,
monotonic request IDs and typed envelopes. It permits search.query/cancel,
result.inspect, action.execute (open/info) and overlay.close/recover. Kotlin
revalidates installed launcher activities and consumes tokens before effects;
repeated or stale requests never repeat an effect. The frontend validates bounded
replies and ignores old sessions, query revisions and unknown IDs. Migration
replaces both bundled client and host; v1 is rejected. Earlier verification
sections below describe their historical checkpoints, not new APK validation.

The WebView persists across queries/insets; service shutdown destroys it and closes the port. Existing window geometry/insets/preferences are reused. IME, focus, renderer recovery, rotation, memory and cold/warm timing still require Android execution.

## Build and validation

Frontend: `cd experiments/raycast-webview/frontend && npm ci && npm run generate`.
Copy `.output/public/` into the variant assets only when changing frontend; never include fixtures, desktop images or node_modules. Static assets are checked in so an Android build needs no Node runtime.

Browser contract script: `node tests/web-runtime.cjs` (Playwright; optional `SPOTLIGHT_CHROMIUM` executable). This uses a clearly test-only MessagePort adapter, never production fake applications. Browser results cannot certify Android launch, actual packages, IME or overlay behavior.

Android gates to run when execution is restored:

```
bash gradlew :app:compileWebviewPrototypeKotlin :app:testWebviewPrototypeUnitTest --tests '*LocalRaycastWebViewTest' --offline
bash gradlew :app:lintWebviewPrototype :app:assembleWebviewPrototype -Pspotlight.preview.optimize=false --offline
```

The unoptimized profile is the existing explicit experimental profile, not a claim of R8 success. Do not disable tests. Existing historical signing files must be available through the authorized protected mechanism; never generate a key. Verify `.webviewprototype`, version 58, non-debuggable, certificate and alignment before distributing. Freeze a source SHA and create APK/source ZIP from that same SHA.

No Actions runs, merges, changes to main, key generation or earlier Preview updates were requested or performed.

## Verified recovery checkpoint

- Frontend static generation successful with Nuxt4.1.3/UI4.0.1 and no experimental dependency.
- Nine browser contract checks pass, zero JavaScript errors, all observed resource URLs local. Browser tests use an explicit test adapter and are not Android/device evidence.
- Reference demo captured in dark mode at720x464. Reference dialog measured672x400. Original shadow,24px backdrop blur,8px radius,48px search input,40px result rows and font stack were checked. Stable UI's changed font stack and#171717 surface were corrected to the reference stack and#161616/70%.
- Native integration checkpoint4de95d5 was fetched into `/workspaces/.spotlight-tools/recovery58-4de95d5`. Offline `:app:compileWebviewPrototypeKotlin` was launched with JDK17,worker1,2GiB,SerialGC and the existing SDK/Gradle caches. Last observed task:compileWebviewPrototypeKotlin. Its result is UNCONFIRMED because Browser/Codespace tool calls repeatedly time out. No APK or signed artifact verified.
- Detached launcher PID8835; log `/workspaces/.spotlight-tools/evidence-recovery58/compile.log`; exit status `compile.exit`. Inspect those before launching any other build. Never kill another session's process or reclaim its lock.
- Two new Robolectric security/asset tests are written but NOT executed. They need `testWebviewPrototypeUnitTest` so variant assets are present. Full lint/assembly/signing, Android WebView rendering, actual installed apps, IME/focus/insets/rotation/recovery, timings/memory and actual overlay screenshots remain PENDING.
- No main/merge/force/Actions/signing changes. Foreign reservation unchanged.

Next: recover the existing compile result; resolve a demonstrated failure if present; run the directed native tests and lint; only after success, build/sign with existing historical credentials through the authorized protected mechanism. Freeze that source commit and package its exact source ZIP. Obtain actual Android overlay screenshots before certifying visual fidelity. Browser screenshots must remain labeled as browser evidence.

## Verified browser checkpoint

`npm run generate`: successful. Chromium Headless Shell 151 ran nine browser contract checks with zero JavaScript errors; all observed URLs were local. See docs/verification/webview-recovery-20261028. Screenshots are BROWSER TEST FIXTURES, not Android screenshots or evidence of installed apps. Android native tests, assembly, signing and real visual comparison remain pending.
