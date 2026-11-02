# Bridge acceptance matrix (adapt to codebase)

| Scenario | Expected |
|---|---|
| Search app actually installed | Native result with stable ID, no fake fixture results in production |
| Rapid type / delete / type | Only latest query displayed; old responses ignored |
| Select app then press Enter | Open exactly selected installed app once |
| Touch scroll on PDF results | Never externally open a file |
| Repeated action.confirm event | Idempotency or explicit conflict; never double-send message |
| Bridge receives malformed or oversized payload | Reject safely; no Java crash / WebView JS injection |
| WebView error / renderer crash | Graceful offline Compose fallback |
| No allowed permission | Action unavailable or permission explanation, no pretend success |
| Escape / close / IME dismiss | Overlay lifetime/focus stable |
| Lost network for optional backend | Search/apps remain fully local and functional |
