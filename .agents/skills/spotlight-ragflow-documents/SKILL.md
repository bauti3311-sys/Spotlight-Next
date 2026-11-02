---
name: spotlight-ragflow-documents
description: Plan and implement optional consent-based RAGFlow retrieval for Spotlight Next document questions via a separate backend/API, preserving offline Quick Look and private local search. Use for cited PDF/document Q&A, ingestion, indexing, retrieval or RAGFlow integration.
---

# Spotlight Next — RAGFlow-assisted document intelligence

## Constraint
RAGFlow is a *separate retrieval/ingestion server*, not an Android SDK and not a valid replacement for local FileIndex, OCR, SAF or Quick Look. Never bundle Docker, Elasticsearch, models, or the full RAGFlow service in the APK, Termux on tablet, or as an implicit always-on requirement. Current documented deployment targets Linux AMD64 with substantial memory/disk. Research a remote/private server or managed service only if explicitly authorized by user; do not create paid accounts.

## Workflow
1. Inspect existing local document stack (file permissions, FileIndex, OCR, Quick Look, content buffer, DirectAi). Preserve these for offline and revoked-permission use. First verify whether a small local indexed approach could satisfy the user's intent without RAGFlow; report tradeoffs neutrally.
2. Write a data-flow/privacy matrix for PDFs: source URI, permission scope, extract on device vs remote upload, retention, deletion, access revocation, dataset ID/user separation, encryption in transit, and user-visible consent. No default bulk upload of private files.
3. Implement an `OptionalDocumentRetrievalProvider` or codebase-appropriate interface (do not create duplicate registries). Native result with: `documentId`, `title`, `source`, `pageNumber?`, `snippet`, `score?`, `origin=local|ragflow`. UI citations must always be grounded in a real source.
4. First validate a **mock retrieval provider**, using small consented non-sensitive fixture PDFs. Test exact query -> snippet -> source link, no match -> transparent no-result, multiple matches -> ranking and cancellation.
5. If user separately authorizes a backend and endpoint, implement an **explicit opt-in remote adapter** using the official RAGFlow REST API. For direct retrieval, investigate `POST /api/v1/retrieval`; verify endpoint semantics, dataset IDs, API version and response fields against the *deployed server version*, not a guessed schema. Use timeouts, backoff only for safe read-only queries, bounded payloads and no credential exposure.
6. API keys stay in native secure storage/backend, not in Nuxt JavaScript, localStorage or APK source, and ideally RAGFlow is proxied through a narrowly scoped trusted backend instead of revealing master server credentials to the Android client.
7. Include opt-in upload UI for a user-selected file; indicate what leaves the tablet, remote retention/deletion, and that RAGFlow needs server infrastructure and possibly an embedding/LLM provider. No silent ingestion or network sync.
8. Provide cancellation, deletion and revocation tests. Do not send OCR text of sensitive docs to a model unless the user explicitly approves the external transfer.
9. Document operational setup separately (not in the app runtime). Make all backend-offline tests pass and ensure the UI retains instant app/file search with no server.

## Acceptance
- 'buscar PDF sobre rehabilitación vestibular' returns local matches without internet and opens local Quick Look, no external PDF launch on scroll.
- With server *not configured*, UI explains optional retrieval availability, and app search works normally.
- With a test provider, '¿qué dice el PDF sobre ...?' produces a snippet with real document title and page when known; no invented source.
- Disabled/permission revoked = no external transmission. No remote push without consent.
- Controlled tests verify no query per keystroke, no stale results after query change, no infinite retry and proper source citations.

Official project: https://github.com/infiniflow/ragflow
API reference: https://ragflow.io/docs/http_api_reference
Operational limits: https://github.com/infiniflow/ragflow/blob/main/docs/faq.mdx
