---
name: spotlight-openui-forms
description: Adapt OpenUI principles to native Jetpack Compose contextual forms, recipient selection, confirmations and structured results in Spotlight Next. No React/Vue renderer, JavaScript or visual WebView in the APK.
---

# Spotlight Next — contextual forms in native Compose

Recovered from PR #20; native adaptation authorized 2026-11-03. This is a project workflow inspired by OpenUI's constrained structured interfaces, not an OpenUI Android renderer. Read [provenance](../../../docs/verification/native57-return-20261103/skills-origin.json) and the native direction first.

## First verify
Read AGENTS, autonomy and the functional audit. Inspect existing CommandForms/MessageEditor, People, Replies, CommandTemplatePanel, LocalToolForms, ContextualDraftHost, ContextSplitPanel, ToolPlanEditing and the executor. Forms, deterministic parsing and review already exist. Reuse these components and GlassTokens/Inter/icons; do not rebuild the application or introduce a generic AI chat.

## Workflow
1. Choose one demonstrated functional gap and one native flow. Map states: missing/ambiguous target -> editable draft -> review -> confirming -> requested/verified/error/unknown. Do not infer delivery from successful dispatch.
2. Use a small closed set of typed native presentation models: message review, native setting review, cited excerpt, action receipt. [Allowlist](references/safe-components.md) is conceptual, not new composables required by name. Bound fields and validate again in Kotlin execution.
3. Start with deterministic input and real local data. Generated output is untrusted data, never executable code or an arbitrary component tree. No fabricated contacts, notifications, documents or effects in production.
4. Recipient identity comes from current authorized People/Replies data; require explicit choice for ambiguity and multiple addresses. Channel, recipient and exact body must remain the reviewed values. Switching mechanisms or targets invalidates approval.
5. Reuse the API-contract skill's preparation/review/confirmation and existing durable task policies. Restoring an editable draft must not restore authorization or replay an effect. A possibly completed external action has no automatic retry.
6. Preserve native overlay geometry and existing inspector placement. Visible labels, field errors, focus/Tab/Shift+Tab/Enter/Escape, 48dp targets, phone/tablet, large text, TalkBack semantics and IME behavior are acceptance criteria. No broad redesign without visual approval.
7. AI generation, if separately authorized later, can fill only allowlisted validated data via existing DirectAi policies. No remote request per keystroke or implicit private-context transfer.

## Stop rules
No @openuidev React/Vue packages, Zod, browser, JS engine or new framework in the APK. No Gateway, telemetry, provider, paid key or dependency upgrade implied by this skill. Fixed native forms are the default; upstream OpenUI is conceptual research only. Require a concrete benefit before adding any schema renderer.

## Acceptance
One local command opens an editable review form without effects; ambiguous target remains unresolved; confirmation revalidates current target/permission; cancellation or restoration cannot send. Preserve existing apps, Spotify, WhatsApp, documents, IA and settings. Test actual Compose components and compare matched native screenshots for any visible correction. Do not label fixtures physical validation.

Conceptual references only: https://github.com/thesysdev/openui ; https://github.com/thesysdev/skills/tree/main/skills/openui
