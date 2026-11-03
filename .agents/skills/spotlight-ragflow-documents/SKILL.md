---
name: spotlight-ragflow-documents
description: Audit and improve native Spotlight Next document retrieval with real citations, reusing FileIndex/OCR/SAF and semantic search. Evaluate an optional remote RAGFlow adapter only if local gaps justify it and the owner explicitly consents.
---

# Spotlight Next — local retrieval first, optional RAGFlow

Recovered from PR #20, adapted for Preview57 native Compose on 2026-11-03.
See [provenance](../../../docs/verification/native57-return-20261103/skills-origin.json).

## Existing architecture is the starting point
Read AGENTS, native direction, autonomy and the functional audit. Inspect FileIndex, DocumentText/ExtractedDocument, OCR budget, DocumentEmbedder, SemanticModelStore, DocumentSelectionText, Quick Look, source permissions and DirectAi. FileIndex already owns semantic_vectors, fragment offsets, fingerprint checks, permission checks and cancellation epochs. Do not build a duplicate index/provider registry or assume semantic search is absent. The local model is optional and must be actually installed and verified; lexical search remains available.

## Workflow
1. Define a real local retrieval flow: query -> indexed permitted document -> exact excerpt/provenance -> native Quick Look. Measure relevance on a small local consented fixture corpus before deciding a server is needed. A source file or model class is not evidence of real-device OCR/embeddings working.
2. Preserve all native SAF/OCR/index/import/cache behavior, private mode and revocation. Unknown page is null, never fabricated. Existing Result.preview is text; audit how to expose structured citations without inventing page mappings or replacing Result wholesale.
3. Reuse existing retrieval entry points. Add an optional adapter/interface only if a concrete seam is needed; [provider guidance](references/provider-contract.md) is illustrative. Maintain local-only operation and native permission authority.
4. Tests use fixture documents and controlled providers only: exact excerpt/source/fingerprint, no match, multiple matches, ranking, query changes, cancellation, deleted document/model, revoked grant and offline fallback. A mock is a test fixture, not a production retrieval service.
5. RAGFlow is a separate ingestion/retrieval server, not an Android library. Do not install Docker/Elasticsearch/models, a server in Termux, or a permanent service. No backend deployment, account, paid key, private query, OCR text or file upload without explicit separate authorization. Installing this skill authorizes none of those actions.
6. Only if local evidence shows a justified gap and the owner consents: research the exact deployed RAGFlow version and API, including POST /api/v1/retrieval. Do not assume fields/version from an example. Define dataset isolation, auth, TLS, bounded payloads/timeouts, deletion/retention, access revocation and safe read-only retry policy. Document infrastructure/costs separately.
7. Show exactly what data would leave the device and why. Opt-in remote retrieval is separate from uploading selected files and from sending excerpts to an LLM. Revocation/disable must prevent subsequent transmissions. Keep credentials in native protected storage or a narrow trusted backend, never source or a UI model.

## Acceptance
Without any server or network, existing apps/files search and Quick Look still function. A local cited hit opens the real permitted source. Page appears only if known. No model/network call per keystroke; no stale results, invented sources or hidden bulk ingestion. Remote failure retains local results. Report inspection, fixture execution and physical proof separately. No new dependencies or server are implied.

References for later version-specific research: https://github.com/infiniflow/ragflow ; https://ragflow.io/docs/http_api_reference
