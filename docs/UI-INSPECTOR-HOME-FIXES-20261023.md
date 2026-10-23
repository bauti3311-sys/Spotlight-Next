# Inspector height and home hint — implementation pending Android validation

Base: 9f7f873e0a225418b229cc406a3a41b68b65cf70. Preview 45 remains the verified delivery from 39430eb2; no APK rebuilt.

## Changes

- Split inspectors use the available viewport budget rather than a second 400 dp cap. The results-list maxHeight preference still applies outside split inspectors.
- Split layout measures the two content panes first and the divider last. Short content no longer inherits the maximum divider height; long inspector content can use its allocated height and scroll if needed.
- Home hint participates in the header column instead of being a separate overlapping Box child, and requires actual home: no system page, navigation history, task/form, inspector, action/search menu, active context or file tray.
- New regressions cover short/long inspectors, reducing the budget (IME equivalent), scrolling to the last row, Spotify bottom actions in a tall viewport, and hint absence in Saved/apps with return to Home.

## Execution evidence and limits

Attempted directed command: `:app:testDebugUnitTest --tests dev.bautista.spotlight.ui.ContextSplitHeightTest --tests dev.bautista.spotlight.ui.ProductEntryPointsTest.homeHintDisappearsInSavedAndReturnsOnlyOnHome` via the existing runtime helper.

The command did not reach Gradle: `python3: can't open file '/workspace/scratch/cd7b8fe1285c/functional-runtime/run-gradle.py': [Errno 2] No such file or directory`.

The prior runtime, SDK and dependency caches are absent in this environment. No repeated downloads, CI jobs or build attempts were started. There are **zero newly executed Android tests**, no before/after screenshots and no physical validation. The code and tests are a pending-validation checkpoint, not an accepted fix or a new delivery.

Run ContextSplitHeightTest, ProductEntryPointsTest, MediaInspectorFlowTest and SpotlightPanelLayoutTest first in the restored Android environment. Confirm tablet/phone, font scale, footer and real IME before accepting geometry. Existing whole-pane scrolling remains; its interaction with the inspector scroll still needs the UI round before any further change.

D27 and its historical 60 unique passing cases are unchanged. Matrix remains 9 complete / 25 implemented with insufficient tests / 6 partial. D28, D26, Spotify and Cast are not advanced past the requested prerequisite: both UI bugs must pass first.
