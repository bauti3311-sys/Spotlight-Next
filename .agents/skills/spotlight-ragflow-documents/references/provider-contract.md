# Suggested provider (illustrative; adapt to the current architecture)

```kotlin
interface DocumentRetrievalProvider {
    suspend fun search(query: String, selectedDocumentIds: Set<String>, limit: Int): List<DocumentHit>
}

data class DocumentHit(
    val stableDocumentId: String,
    val title: String,
    val excerpt: String,
    val page: Int?,
    val nativeUri: String?,
    val origin: String // local or ragflow
)
```

Keep document URIs private to Kotlin; do not serialize `content://` grants into untrusted remote responses or let the WebView open arbitrary URIs. Make origin and availability visible. Add tests for upload consent, retrieval citing one real fixture chunk, permission revocation, deletion, stale search and local fallback. Exact interface naming above is only a sketch, not a command to override existing project interfaces.
