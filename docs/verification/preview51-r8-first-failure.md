# Preview 51 — first minified-build failure

Source: 5178d27a55ca15b29d9459d1d3384d712a022211. Build failed after 10m14s in minifyRaycastPreviewWithR8; lint XML has 0 errors. Full log retained at /workspaces/.spotlight-tools/evidence51/build51.log.

R8 reports only missing StdDeserializer, StdSerializer and com.spotify.base.annotations.NotNull. Verified AAR 0.8.0 classes with javap: ConnectionParams defaults to GsonMapper.create(); NotNull is RuntimeInvisibleAnnotations. The AAR already supplies -dontwarn com.fasterxml.jackson.*, which does not cover these nested types. Add only the three exact optional-type rules; no global warning suppression, added Jackson runtime, tests/lint disabled, or changed dependencies. SDK consumer keep rules remain intact.

14 existing directed cases passed before this packaging-only correction. New minified build still required; no physical Spotify playback claim.
