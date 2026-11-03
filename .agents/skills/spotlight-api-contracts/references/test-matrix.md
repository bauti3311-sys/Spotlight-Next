# Native contract acceptance matrix

These are criteria for future directed changes, not tests executed by installation.

| Scenario | Expected |
| --- | --- |
| Installed exact app -> select -> Enter | Correct native target; inspected selection alone causes no effect |
| Fast edit/delete/change scope | Latest query only; old result/error cannot replace current state |
| App removed/disabled or permission revoked after review | Explicit unavailable/permission state; no hidden alternative |
| Selected destination fails | Offer fallback for review; do not launch a different app automatically |
| Ambiguous contacts, multiple numbers or WhatsApp variants | Explicit recipient/address/channel selection |
| Changed notification/PendingIntent before confirm | Require fresh review; no send |
| Repeated sensitive confirmation/timeout/restoration | No automatic repeat; unknown outcome remains explicit |
| Invalid model/user/provider parameters | Reject at domain/executor boundary without private details |
| PDF scroll/select | Local Quick Look; external open only explicit |
| Close/Escape/IME/rotation | Preserve native focus and draft; no restored execution |
| Optional backend absent/offline | Local apps/files remain available |

Use existing relevant suites; no full matrix or APK build for documentation-only work.
