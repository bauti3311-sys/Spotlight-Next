# UI component allowlist proposal

All components use the established Raycast Nuxt typography, colors, corners and spacing. Use actual current tokens, not speculative hard-coded redesigns.

- `ReviewMessage`: `targetDisplayName`, `editableBody`, `targetStatus` (`resolved|ambiguous|unavailable`), `confirmed=false`, disabled submit until reviewed; target identities must come from native side.
- `DocumentCitation`: `documentId`, `title`, `pageNumber?`, `snippet`, `sourceUri?` (native-owned), `confidenceLabel?` (if grounded); do not fabricate pages.
- `NativeSettingReview`: `name` (`brightness|volume`), `requestedValue`, `currentValue?`, `permissionStatus`, `confirm`.
- `ActionStatus`: `requestId`, `status` (`ready|running|succeeded|failed|unknown`), `recoveryHint`.

UI events do NOT call external apps directly. They emit typed, bounded messages to Kotlin through a single restricted native bridge. Keep web scripts same-origin/local.
