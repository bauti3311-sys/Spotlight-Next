# Gate #54: compilation failure and minimal test correction

- Run: https://github.com/bauti3311-sys/Spotlight-Next/actions/runs/40048431940 (single authorized attempt, `directed-tests`).
- Tested source: `b0252a94225f8a64cd157bee3b9c62399dbd8b64`.
- `compileDebugKotlin` and `compileDebugJavaWithJavac` succeeded. `compileDebugUnitTestKotlin` failed with receiver/type resolution errors on `getUnclippedBoundsInRoot().height` in `ContextSplitHeightTest.kt`, original lines 25, 26, 27, 35 and 37.
- Fix: compute the `DpRect` height as `bottom - top` through `measuredHeight`. All five assertions, their thresholds (100/100/100/500/220 dp), both test cases and scroll checks are preserved. No production code or dependencies changed.
- Static review: all 243 unit/instrumented test source files matched the tested remote tree before editing. The other uses of `getUnclippedBoundsInRoot` use `top`/`bottom` directly or subtract them; no additional occurrence of this error was found.
- Evidence: the original GitHub job log remains available. Its local copy is `/workspace/scratch/8c789f039c8b/gate-40048431940.log`; it has not been replaced. Artifact upload failed because GitHub Actions storage quota was exhausted; no artifact upload was retried. No XML exists because test execution never began.
- Validation limit: this correction is statically reviewed, not compiled. UI, D28 and all remaining directed groups remain unexecuted. No Actions dispatch, job rerun, APK generation or Android tests were performed for this correction. A new gate requires fresh explicit authorization.
- Matrix unchanged: 9 complete / 25 implemented with insufficient tests / 6 partial. PR #18 stays draft; existing Preview artifacts and historical signing remain untouched.
