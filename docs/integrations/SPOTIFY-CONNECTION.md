# Spotify connection — Preview 48 checkpoint

Use only the **Client ID** in Settings → Integrations → Spotify. Never enter the Client Secret in Spotlight or commit it. Authorization Code PKCE uses a random ephemeral verifier/state; tokens are encrypted by Android Keystore in no-backup storage. Changing the Client ID or disconnecting clears local tokens. Revocation at Spotify remains available from the user's account settings.

In https://developer.spotify.com/dashboard select your app, open Settings and add this exact Redirect URI:

```
http://127.0.0.1/spotify/callback
```

Save it without a fixed port. Spotify explicitly permits dynamic ports for loopback IP literals. Spotlight binds a temporary random port on **127.0.0.1 only** during the explicit connection operation (five-minute timeout). It does not bind to the LAN, expose a service, use localhost, add a public endpoint or relax Android's cleartext policy. Cancelling/finishing closes the listener; verifier/state are not restored after process death. If Android kills the app during login, reconnect.

Add the intended Spotify account in Dashboard User Management if required. Current Development Mode requires the owner to maintain Premium; the new-user cap is five. Playback Web API operations require a Premium user and an active Spotify/Connect device. Search stays at the current maximum of ten results per selected type. Permission scopes are only `user-read-playback-state` and `user-modify-playback-state`; library/private playlist scopes are not requested in this checkpoint.

Open Spotify's app inspector and use Search explicitly. No remote requests are made per keystroke. A selection is reviewed before Confirm playback. A successful command is displayed as a request accepted by Spotify; Comprobar reproducción reads the real current state and compares the selected track/context URI. There is no automatic retry of commands with external effects. Existing authorized Android MediaSession metadata/progress/controls remain available independently of OAuth. Natural Spotify search forms use the native provider when authorized; the existing public-intent path remains when disconnected.

## Scope and limitations

This checkpoint uses official Web API and the existing MediaSession adapter. App Remote SDK integration is **not yet included**; its official SDK requires the Spotify client, fingerprint/client registration and its own lifecycle/authorization handling. No account has been connected in development, so OAuth/browser round trip, Premium entitlements and real playback remain physical/account acceptance tests. No library, device picker, Web API volume or search-result artwork implementation is claimed here.

Spotui, Square and Outify were inspected as references: all three expose GPL-3.0 licenses; Square/Outify rely on unofficial librespot/Rust playback and Spotui describes additional resolver layers. No source or unofficial playback engine was copied. The native inspector and official endpoints are retained instead of embedding another app.

Whatsmo's repository describes an experimental MIT-licensed Tauri/SvelteKit/Bun shell with unofficial whatsapp-rust, not a small drop-in Kotlin/Go provider. This checkpoint does not adopt its runtime. Existing whatsmeow/WuzAPI research remains in `experiments/whatsapp`; no duplicate audit or unchanged Python PoC suite was run.

WhatsApp experimental preferences record a specific, reversible consent only. Account linking remains unavailable because a validated engine is not shipped. No transport is initialized by those preferences. The provider prototype is compiled by the test source set only and must not appear in the production APK. Normal RemoteInput revalidates the reviewed conversation/PendingIntent; public intents remain the fallback for new conversations.

Sources checked 2026-10-24:

- https://developer.spotify.com/documentation/web-api/concepts/redirect_uri
- https://developer.spotify.com/documentation/web-api/tutorials/code-pkce-flow
- https://developer.spotify.com/documentation/web-api/tutorials/february-2026-migration-guide
- https://developer.spotify.com/documentation/web-api/reference/start-a-users-playback
- https://developer.spotify.com/documentation/android
- https://github.com/spotify/android-sdk
- https://github.com/Spotui/Spotui
- https://github.com/Lelonio/Square
- https://github.com/iTomKo/Outify
- https://github.com/Whatsmo/whatsmo
