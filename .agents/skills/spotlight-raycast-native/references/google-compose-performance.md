# Google Compose performance — adapted compatibility guide

Source: android/skills at 4ebabf2b0fd6c04b7150023c8b612f80592a6226, jetpack-compose/jetpack-compose-performance/SKILL.md; Google LLC, Apache-2.0. Original license is retained in the [existing spotlight-app-integration license](../../spotlight-app-integration/references/LICENSE); this file is an original project adaptation, not a full installation.

Adopt a measured transition, release/R8 variant, trace and before/after metric. Inspect state reads, layout, lazy keys/content types, effect lifetime, graphics and modifiers within that flow. Read the relevant pinned upstream reference before a change attributed to it. Scope remains one concrete flow, not a project-wide scan during APK delivery.

Compatibility exclusions: Compose BOM 2024.12.01 cannot assume computedStateOf (1.13+), keyed SideEffect (1.12+), onVisibilityChanged, onLayoutRectChanged or new shared-transition APIs. Do not update Compose to follow a skill. Keep supported derivedStateOf only when its invalidation actually reduces work; ordinary boolean derivation needs no cache. Do not blindly replace snapshot collections, lifecycle collectors or stable annotations. Banes and Google's stability guidance differ: preserve truth of mutability/equality and measure the actual dataset before choosing either.

No physical performance improvement is claimed without a non-debuggable build and real-device measurement. No profiler/agent installer, binary, script, dependency or baseline profile module is installed by this adaptation.
