---
name: spotlight-raycast-native
description: Implement and review Spotlight Next native search, results, inspector, Quick Look, forms and actions with precise Raycast-inspired behavior, Android overlay safety and comparable evidence.
---
# Raycast behavior, native Android ownership

Read AGENTS.md, current user decisions, spotlight-native-craft and spotlight-visual-review first. The latest approved contract wins over historical DESIGN.md (three idle suggestions, adaptive active results, Compacta optional, current Tab autocomplete rules). This skill is project-owned guidance, not Raycast SDK code.

## Choose one real flow

| Surface | Native contract |
| --- | --- |
| Search/List | Render promptly; preserve query, stable result identity and selection. Initial three useful suggestions. Expand active results to real window/IME space; scroll only when needed. |
| Grid | Use only when images define the items and the existing flow warrants it. No new grid mode to imitate a screenshot. |
| Detail/inspector | Preserve selected object, show actual metadata; side-by-side only when useful width remains. Mobile uses integrated vertical context. No empty column or artificial height. |
| Quick Look | Selecting PDF previews locally; scrolling never opens another app. Opening externally is explicit. Preserve page/URI permissions; cancel stale work and release resources. |
| Form | Visible labels and local validation, unambiguous recipient, editable draft, explicit confirmation. Error belongs near its field and remains readable at large text. Restoration never executes. |
| ActionPanel | Independent from inspector App Actions; preserve Ctrl+K. Real actions only, deduplicate equivalent capabilities, state missing permissions. Enter executes focused action, Escape returns before closing overlay. |
| Spotify/WhatsApp | Selection is preparation. Busy state cannot silently change approved target. Cancellation never retries or resends. Distinguish request accepted, verified state and delivery. |
| Settings/permissions | Reuse native groups, current tokens and actual grants. Explain the capability and recovery, not private implementation details. |

## Execution and evidence

1. Name the state transition and inspect real component, caller constraints and overlay WindowManager flags. No React, WebView, Raycast package, new theme or framework.
2. Fix one evidenced issue in hierarchy, clipping, density, focus or recovery. Reuse Solid Graphite/Obsidian Glass, Inter, app icons and spacing tokens. No decorative badges, stacked cards, gradients or dead controls.
3. Use 48dp Android targets, correct semantics and visible keyboard focus. Do not copy desktop shortcuts that conflict with AGENTS.
4. Use [Compose focus](../compose-focus-navigation/SKILL.md), [state](../compose-state-and-effects/SKILL.md), [components](../compose-component-design/SKILL.md), [tests](../compose-ui-testing-patterns/SKILL.md), [motion](../compose-animations/SKILL.md), and [performance](../compose-performance/SKILL.md) only when relevant.
5. Capture before/after from the real Compose component at the same API, width/height, density, font scale, orientation, theme, text and selected state. Store SHA and fixture metadata. Compare pixels by inspection; semantic pass is not visual acceptance. Never replace a baseline to hide failure.
6. Test observable input: keyboard/focus, scroll without activation, IME/window bounds, errors, cancellation and saved draft restoration. Use controlled providers; do not send real messages or connect accounts.
7. Compile/test the directed change, preserve first failure, publish with HEAD/lock CAS. Freeze production edits during a build/delivery. Physical jank/audio/One UI claims require the device.

See [adoption review](references/adoption-review.md) and [Google performance compatibility](references/google-compose-performance.md). No scripts, hooks, downloads or implicit CI execution.
