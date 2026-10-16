package dev.bautista.spotlight.search

/** Provenance is explicit; result identifiers and newline heuristics never grant consent. */
object AiRequestPolicy {
    const val MAX_PROMPT_CHARS = 24000
    fun prepare(ask: Action.Ask): Action.Ask = ask.copy(prompt = ask.prompt.take(MAX_PROMPT_CHARS))
    fun requiresReview(ask: Action.Ask): Boolean =
        !ask.reviewed && (ask.sensitive || ask.origin == AiInputOrigin.PRIVATE_CONTEXT)
    fun question(prompt: String, continuation: Boolean = false): Action.Ask =
        Action.Ask(prompt, origin = AiInputOrigin.USER_QUERY, continuation = continuation)
    fun authorize(ask: Action.Ask): Action.Ask = prepare(ask).copy(sensitive = false, reviewed = true)
}
