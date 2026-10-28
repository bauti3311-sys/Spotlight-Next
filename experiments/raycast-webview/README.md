# Spotlight Next — recovered WebView experiment

Status: integration checkpoint; Android compile, signed APK and real Android overlay validation PENDING.

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

A MessagePort is transferred only to that exact origin. Protocol v1 permits `search`, `execute` (`open` or `info`), `close` and `recover`. Native search reuses SearchEngine's applications scope and accepts only explicit launcher activities. Results receive opaque per-query tokens; the frontend cannot supply arbitrary packages, components, intents or URLs. Queries and message lengths are bounded, previous queries cancel, stale tokens are removed, and result text is rendered through Vue escaping.

The WebView persists across queries/insets; service shutdown destroys it and closes the port. Existing window geometry/insets/preferences are reused. IME, focus, renderer recovery, rotation, memory and cold/warm timing still require Android execution.

## Build and validation

Frontend: `cd experiments/raycast-webview/frontend && npm ci && npm run generate`.
Copy `.output/public/` into the variant assets only when changing frontend; never include fixtures, desktop images or node_modules. Static assets are checked in so an Android build needs no Node runtime.

Browser contract script: `node tests/web-runtime.cjs` (Playwright; optional `SPOTLIGHT_CHROMIUM` executable). This uses a clearly test-only MessagePort adapter, never production fake applications. Browser results cannot certify Android launch, actual packages, IME or overlay behavior.

Android gates to run when execution is restored:

```
bash gradlew :app:compileWebviewPrototypeKotlin :app:testDebugUnitTest --tests '*LocalRaycastWebViewTest' --offline
bash gradlew :app:lintWebviewPrototype :app:assembleWebviewPrototype -Pspotlight.preview.optimize=false --offline
```

The unoptimized profile is the existing explicit experimental profile, not a claim of R8 success. Do not disable tests. Existing historical signing files must be available through the authorized protected mechanism; never generate a key. Verify `.webviewprototype`, version 58, non-debuggable, certificate and alignment before distributing. Freeze a source SHA and create APK/source ZIP from that same SHA.

No Actions runs, merges, changes to main, key generation or earlier Preview updates were requested or performed.

## Verified browser checkpoint

`npm run generate`: successful. Chromium Headless Shell 151 ran nine browser contract checks with zero JavaScript errors; all observed URLs were local. See docs/verification/webview-recovery-20261028. Screenshots are BROWSER TEST FIXTURES, not Android screenshots or evidence of installed apps. Android native tests, assembly, signing and real visual comparison remain pending.
