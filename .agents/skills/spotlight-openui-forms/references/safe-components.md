# Native contextual surface allowlist

Reuse actual Compose components and GlassTokens. Names below describe data roles, not an instruction to create a second component framework.

- Message review: native recipient/address/channel identity, editable bounded body, target resolved/ambiguous/unavailable, confirmation initially absent. Current Replies target must be revalidated at dispatch.
- Document citation: document ID, title, exact excerpt, origin, fingerprint and page only when extraction proves it. Native URI remains owned and grant-checked locally.
- Native setting review: allowlisted volume/brightness, bounded requested/current value, permission/capability and explicit confirmation. Never imply Shizuku is required for every operation.
- Action receipt: operation identity, prepared/requested/verified/failed/unknown, honest recovery. UNKNOWN does not enable automatic retry.

Compose emits typed intent to the existing native executor. No arbitrary HTML, JSON component execution, reflection or scripts. Persist drafts as data, not authorization. Existing components already handle much of this; inspect before extending.
