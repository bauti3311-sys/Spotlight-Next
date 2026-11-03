---
name: spotlight-api-contracts
description: Design and review Spotlight Next native Kotlin contracts, typed actions, validation, application integration, consent, cancellation and honest errors. Use between Compose state, search, domain actions and Android executors; no visual WebView bridge.
---

# Spotlight Next — API & Interface Design, native edition

Recovered from PR #20 and adapted by the owner's explicit 2026-11-03 decision.
See [provenance](../../../docs/verification/native57-return-20261103/skills-origin.json).
Project-authored workflow inspired by API design principles, not an upstream SDK.

## Authority and scope
Read AGENTS, docs/NATIVE-PREVIEW57-DIRECTION-20261103.md, the autonomy protocol and current PLANS. Verify remote branches/checkpoints, own reservation and dirty worktrees before writing. New development descends from Preview57 `4fc33f2c918d3fe53a8bad7ac6571f2757630066`; do not merge PR20 implementation or use Preview58/59 as a base. Compose is the visual layer; Kotlin owns effects. Do not change main, signing or Actions.

## Contract-first procedure
1. Trace the actual flow: Compose event -> state -> SearchEngine/Action/ToolCall -> registry -> executor -> Android/provider. Read Models.kt, IntegrationRegistry, AppCapabilities, ToolRegistry, ToolExecutor, PlanExecution and the selected caller. Extend existing contracts; do not add a parallel action bus or registry.
2. Prefer Kotlin sealed types/enums and bounded validated values at trust boundaries. Action and ToolCall already exist. Replace stringly typed branches incrementally only with compatibility and restoration tests. JSON/versioning is needed for persisted or remote data, not for every in-process UI callback.
3. Inventory operation, source of target identity, permission/configuration, risk, precondition, cancellation and observable receipt. Separate inspect/prepare from confirm/execute. Selection and scroll cannot execute a sensitive action. Missing integration stays unavailable.
4. Validate user/model/provider inputs at the executor boundary: lengths, ranges, field allowlist, source grants, exact package/activity/channel/recipient and current permission. Construct Intents from reviewed parameters. Never execute a model-provided Intent/script or trust a displayed label as target identity.
5. Preserve current latest-query-wins, stable selection and cancellation. Revalidate before effects after suspension or user review. A fallback that changes reviewed target or effect must be offered for fresh explicit selection, not silently executed.
6. Reuse existing durable task/fence and Replies protections. Prevent repeat confirmation for the same sensitive operation; do not retry after a possibly executed effect, timeout, recreation or unknown outcome. Do not claim the whole app has exactly-once guarantees.
7. Keep PREPARED, REQUESTED, COMPLETED and VERIFIED distinct, extending existing states only when necessary. An accepted Intent/RemoteInput/Spotify request does not prove delivery or audio. Errors should expose a stable recoverable category, not raw provider text/secrets. Proposed categories: invalid input, unavailable, permission required, stale target, cancelled, timeout, unknown outcome, internal.
8. Test the real native path with controlled Android/provider fixtures: absent app, disabled/private activity, changed recipient, revoked grant, malformed arguments, fast edits, repeated confirm, late response, dispose and restored drafts. Never send real messages or spend remote API quota to validate a fixture.

## Acceptance and limits
Use [test matrix](references/test-matrix.md). [Native example](references/native-contract.json) is illustrative documentation, not a new runtime schema or required migration. Preserve preview package/firma, data, keyboard/touch rules and existing search. No dependencies, new permissions, server, UI redesign or CI implied by installing a skill. Source inspection, automated tests and physical One UI proof must be reported separately.

Original conceptual reference: https://github.com/addyosmani/agent-skills/tree/main/skills/api-and-interface-design
