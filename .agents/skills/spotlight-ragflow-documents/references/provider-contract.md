# Native retrieval seam — illustrative, not an implementation

Start with FileIndex.search and FileIndex.semanticSearch. They already own local retrieval, freshness and permissions. Do not add a second registry simply to match a suggested name.

A future structured hit may need: stable document ID, title, exact excerpt, native source handle, source fingerprint, extraction/crop/coverage provenance, page: Int? (unknown=null), origin local|remote and optional meaningful score. An offset in indexed text does not prove a PDF page number; a similarity score does not prove an answer's truth.

Keep source handles/grants and URI resolution native. Remote IDs must never become arbitrary Intent/URI execution. Re-check the current local source/grant before opening a citation. Preserve local-only UI and native Quick Look, without WebView or JavaScript.

Before a remote adapter: explicit server/query/upload/retention consent and tested cancellation/deletion/revocation. Use version-specific provider fixtures and bounded responses; no guessing the RAGFlow wire schema. This document installs nothing and creates no interface.
