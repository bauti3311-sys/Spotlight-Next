---
name: spotlight-openui-forms
description: Add safe, contextual, generative forms and result cards to Spotlight Next's existing offline Nuxt/Vue WebView using OpenUI's Vue renderer, without changing its Raycast appearance, privileged Kotlin executor or user-confirmation rules.
---

# Spotlight Next — OpenUI native-looking contextual surfaces

## Intent
Implement **one optional contextual component surface** inside existing Raycast Nuxt/WebView: for commands like 'mandá WhatsApp a Juan', 'explicame este PDF', 'ajustá brillo al 35%' show a compact, valid, editable form/result, never an entire generic AI chat or full-screen page. Do not replace the main search surface.

## First verify
- Read `AGENTS.md`, repo autonomy protocol and current Nuxt source at the active worktree; verify the existing WebView prototype branch, installed package versions and local asset build method.
- Read the actual upstream OpenUI Vue docs and pinned package types before installing anything. Current Vue line uses `@openuidev/vue-lang` + `zod` and provides `Renderer`; at the time of research its Vue renderer is on 0.4.x, not feature-equivalent to React's OpenUI Lang 1.0, so do **not** blindly copy React examples.
- Prefer the official OpenUI skill as optional *reference*, not a substitute for these Spotlight-specific guardrails: https://github.com/thesysdev/skills/tree/main/skills/openui . **Do not auto-install it or run third-party hooks** without review.

## Workflow
1. Write an explicit compatibility matrix: existing Nuxt version, Vue, package manager, static asset build, `@openuidev/vue-lang`, Zod versions, WebView Chromium behavior, output APK size and browser support.
2. Make a small allowlisted Vue component library matching Raycast UI tokens: `ReviewMessage`, `DocumentCitation`, `SearchResult`, `ActionStatus` (optional), `NativeSettingReview`. Each schema has bounded strings, expected enums and disabled-state semantics. No arbitrary HTML/JS execution from model output.
3. Start **without model calls**: one deterministic command parser or recorded fixture produces structured data and renders an editable, accessible confirmation form. This tests UX and bridge independently of inference.
4. Connect form submission to `spotlight-api-contracts` **prepare -> review -> confirm** flow. Form data remains untrusted; native Kotlin validates target, permissions, session freshness and effects. No auto-send, no permission escalation, no background accessibility commands.
5. If AI-generated OpenUI Lang is later approved, scope to allowlisted components and validate; never treat generated output as executable code or privileged actions. Prefer existing Gemini DirectAi backend; no new paid gateway or API keys without explicit approval. Never put credentials in WebView.
6. Bundle all runtime UI assets in APK; ensure no CDN dependency, no `@latest` runtime fetch, and no separate online Activity. Preserve keyboard selection/Enter/Escape and the Compose fallback.
7. Verify layout in narrow phone width and tablet split inspector: contrast, focus order, 85–115% text scaling, keyboard occlusion, screen reader labels, empty/loading/errors, cancellation and recovery.

## Stop / fallback rules
- If Vue runtime is incompatible or too heavy, describe blockers with dependency and byte-size evidence; use a **fixed Vue form** matching the same UI, not a wholesale framework migration.
- If user rejects visual changes, keep opt-in and behind a reversible feature flag.
- Do not automatically deploy OpenUI Gateway or introduce telemetry.

## Acceptance
- 'responderle a Juan diciendo: ...' creates a review-only contextual form; no message sent until explicit approval and a valid native target exists.
- 'buscá este PDF' can display a cited excerpt only when real document data available; never pretend to have read it.
- No regression to overlay start, app search, ActionPanel, touch selection, local offline operation or settings.
- Evidence: before/after matched screenshots (or clearly labeled simulator captures), tests, SHA, exact deps and caveats.

Upstream: https://github.com/thesysdev/openui ; https://www.openui.com/integrations/vue ; https://www.openui.com/docs/api-reference/vue-lang ; https://github.com/thesysdev/skills/tree/main/skills/openui
