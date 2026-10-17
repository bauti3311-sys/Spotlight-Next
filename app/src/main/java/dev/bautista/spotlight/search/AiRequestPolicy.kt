package dev.bautista.spotlight.search

/** Provenance is explicit; result identifiers and newline heuristics never grant consent. */
object AiRequestPolicy {
    const val MAX_PROMPT_CHARS = 24000
    fun prepare(ask: Action.Ask): Action.Ask = ask.copy(prompt = ask.prompt.take(MAX_PROMPT_CHARS),
        priorMessages=ask.priorMessages.takeLast(10).map{(role,text)->(if(role=="assistant")"assistant" else "user") to text.take(MAX_PROMPT_CHARS)})
    fun requiresReview(ask: Action.Ask): Boolean =
        !ask.reviewed && (ask.sensitive || ask.origin == AiInputOrigin.PRIVATE_CONTEXT || ask.priorMessages.isNotEmpty())
    fun reviewText(ask:Action.Ask):String = if(ask.priorMessages.isEmpty())ask.prompt else
        ask.priorMessages.joinToString("\n\n"){(role,text)->"${if(role=="assistant")"Spotlight" else "Usuario"}: $text"}+"\n\nNueva pregunta:\n"+ask.prompt
    fun question(prompt: String, continuation: Boolean = false): Action.Ask =
        Action.Ask(prompt, origin = AiInputOrigin.USER_QUERY, continuation = continuation)
    fun authorize(ask: Action.Ask): Action.Ask = prepare(ask).copy(sensitive = false, reviewed = true)
}
