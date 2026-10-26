# Selective adoption — 2026-10-25

| Source / pinned revision | Status | Decision |
| --- | --- | --- |
| Raycast UI docs (read 2026-10-25) | ADAPTADA | Behavioral mapping for List, Grid, Detail, Form, ActionPanel and shortcuts. Documentation reference only; no licensed SDK, assets or React copied. |
| Impeccable existing pin 9fa16a06d8957fde5687984629fd028c9f2abf10 | ADAPTADA | Extend existing native-craft procedures. Current upstream observed 3671d50f0ac9a662ce0615e85a256110c9cd720b; no automatic replacement or duplicate collection. Existing Apache-2.0 provenance retained. |
| spotlight-raycast-native (project-owned) | ADOPTADA | Original native component/interaction contract; no Raycast SDK code copied. |
| compose-focus-navigation — Banes 6c30c500 | ADAPTADA | Focus lifecycle, real key input and semantic restoration. No replacement of Spotlight shortcuts. |
| compose-state-and-effects — Banes 6c30c500 | ADAPTADA | Single state owner and scoped cancellation; preserve durable overlay codec. |
| compose-ui-testing-patterns — Banes 6c30c500 | ADAPTADA | Plain UI tests, actual input and separate capture recording evidence. |
| compose-animations — Banes 6c30c500 | ADAPTADA | Small API and stable transition identity; no new motion without an observed need. |
| compose-performance — Banes 6c30c500 | ADAPTADA | Measured change only; persistent build runbook replaces generic upstream build skill. |
| compose-component-design — Banes 6c30c500 | ADAPTADA (existing) | Keep current modifier/slot guidance; no reinstall. |
| Google android/skills 4ebabf2b0fd6c04b7150023c8b612f80592a6226 | ADAPTADA | Performance compatibility reference, not full collection. Apache-2.0. Exclude newer Compose APIs and unsupported universal optimization rules. |
| UI UX Pro Max d14efff8ed1faa4187f004f79926f428c8074ec7 | POSPUESTA | MIT; main skill reviewed. Python search/design-system generator and large style/font catalogs are unnecessary for an existing native theme. No scripts executed or claimed fully audited. Generic 44px web targets do not override Android 48dp. |
| Mobile Android Design, wshobson/agents 0874a27b17a22e2b7c2e2bcd725768e0803cad06 | POSPUESTA | MIT; main skill reviewed. Useful touch/semantics/state principles already covered. Its card/dynamic-color/navigation examples conflict with or add no value to the approved overlay; no collection/plugins/hooks installed. |

Sources:
- https://developers.raycast.com/api-reference/user-interface (and List/Grid/Detail/Form/ActionPanel subpages)
- https://github.com/pbakaus/impeccable
- https://github.com/chrisbanes/skills/tree/6c30c500c36a1827c8fde75415dd7a31394bc02c
- https://github.com/android/skills/tree/4ebabf2b0fd6c04b7150023c8b612f80592a6226
- https://github.com/nextlevelbuilder/ui-ux-pro-max-skill/tree/d14efff8ed1faa4187f004f79926f428c8074ec7
- https://github.com/wshobson/agents/tree/0874a27b17a22e2b7c2e2bcd725768e0803cad06/plugins/ui-design/skills/mobile-android-design

ADOPTADA is reserved for unchanged compatible guidance; none of these new candidates is installed wholesale. No third-party commands, hooks, credentials, network policy changes, SDK/Gradle/Kotlin upgrades or APK frameworks were introduced. Markdown links are references, not authorization.

All selected Banes Markdown files were read with their directly referenced local files. Shared Apache-2.0 license is retained in compose-component-design/LICENSE. Current Impeccable 4.7.1 main/native-audit/polish were examined as data; its binary-downloading launcher and hooks are excluded. Existing project pin is not silently advanced. No script in UI UX Pro Max was run or certified safe.
