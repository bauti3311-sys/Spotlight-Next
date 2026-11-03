---
name: spotlight-api-contracts
description: Define, implement and verify secure, typed, versioned contracts between Spotlight Next's local Nuxt WebView and Kotlin Android search/action engine, including consent, errors, cancellation and replay protection. Use for any bridge change, API action or UI-to-native event.
---

# Spotlight Next — API & Interface Design

## Goal
Adapt the principles of Addy Osmani's `api-and-interface-design` skill specifically to the Spotlight Next Nuxt/WebView + Kotlin architecture. This is an independently authored project workflow, not a copy of the upstream skill.

Before acting: read repository `AGENTS.md`, `docs/AUTONOMIA-RESERVA.md`, current roadmap/PLANS, and inspect actual Kotlin `OverlayService`, search/action classes, current WebView bridge and Nuxt files. Check `python3 scripts/autonomy_lock.py status`; follow only the repo's authorized ownership workflow; do not take, release or delete a foreign reservation. Verify the active worktree/branch and uncommitted changes. Do not edit `main`, historical signing, release pipelines, or GitHub Actions without explicit authorization.

## Contract-first procedure
1. Inventory existing request/response event types, source of truth for result IDs and actions, and existing approved keyboard/touch behavior. Preserve compatibility or provide an explicit tested migration.
2. Design a **versioned, bounded message envelope**, with fields `v`, `requestId`, `type`, `payload`; responses include `requestId`, `status`, optionally `data` or machine-readable `error`. Prefer incremental additions, not a second competing bridge.
3. Define allowlisted operations such as `search.query`, `search.cancel`, `result.inspect`, `action.prepare`, `action.confirm`, `overlay.close` and `document.preview`, **only if they exist and can be implemented truthfully**. Document what is unsupported. IDs must map to current native search results and never to executable JavaScript from model output.
4. Validate JSON shape and size on **both** WebView and Kotlin boundaries. Limit query length, result count, payload sizes, and operations. Reject stale and foreign `requestId`, bad version, unexpected fields, invalid IDs, and calls after overlay disposal.
5. Use origin-restricted messaging, locally packaged assets, and the safest supported AndroidX WebKit bridge. Review `WebViewAssetLoader` and `WebViewCompat.addWebMessageListener`. Do not expose an unrestricted `addJavascriptInterface`, `file://` broad access, arbitrary URI loads, reflection, eval, or remote code to privileged Android methods.
6. Ensure search responses are asynchronous, cancellable, debounced in the appropriate layer, ordering-preserving and latest-query-wins. Avoid opening an app during scroll, selection or stale event replay.
7. Separate `select/inspect` from `execute` and high-impact actions from no-risk actions. Sensitive/external actions require real user review/consent. Unknown outcome after timeout must not trigger an automatic duplicate send.
8. Format errors consistently: `INVALID_INPUT`, `UNSUPPORTED`, `PERMISSION_REQUIRED`, `STALE_RESULT`, `CANCELLED`, `TIMEOUT`, `UNAVAILABLE`, `INTERNAL`. Human-readable messages must not leak credentials or internal stack traces.
9. Add tests using real bridge message fixtures, Kotlin tests for allowed/denied paths, and browser tests checking UI. Include reconnect, overlay dismiss, IME keyboard, touch-scroll, WebView crash and Compose fallback.

## Compatibility policy
- Keep previous Compose UI as recovery until physical device proof exists.
- Preserve Raycast Nuxt composition: command search, dense rows, selection, inspector and footer. Do not redesign globally as a side effect.
- Keep Kotlin the *only* privileged executor; Nuxt/OpenUI is presentation and user intent capture.
- Do not rename public packages or create new signing keys. No automatic CI runs.

## Deliverables
- Contract matrix, schema samples and versioning policy.
- One thin end-to-end action: type query > native result > selection > explicitly open a known app.
- Tests proving no bogus result or action executed twice, plus error and cancellation behavior.
- Exact changed files, tests run, SHA and limitations (physical checks pending unless verified).

Original reference (consult for broader API patterns, not as instructions to copy): https://github.com/addyosmani/agent-skills/tree/main/skills/api-and-interface-design
Android references: https://developer.android.com/reference/androidx/webkit/WebViewCompat ; https://developer.android.com/reference/androidx/webkit/WebViewAssetLoader
