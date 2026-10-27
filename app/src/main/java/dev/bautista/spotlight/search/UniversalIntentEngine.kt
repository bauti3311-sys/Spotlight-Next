package dev.bautista.spotlight.search

import dev.bautista.spotlight.control.ActionCatalog
import dev.bautista.spotlight.control.CalendarLanguage
import dev.bautista.spotlight.control.NaturalIntents
import dev.bautista.spotlight.data.Config
import dev.bautista.spotlight.next.IntentRouter
import java.time.LocalDate
import java.time.ZoneId

enum class PrimaryIntentKind {
    OPEN_APP,
    KNOWLEDGE_AI,
    FILE_SEARCH,
    CONTEXT_AI,
    COMPOUND_ACTION,
    COMMUNICATION,
    SPORTS,
    WEATHER,
    FOCUS_STUDY,
    DEVICE_CONTROL,
    CALENDAR,
    LOCAL_TOOL,
    GENERAL_SEARCH,
}

enum class EntityType {
    APP_NAME,
    FILE_KIND,
    FILE_TOPIC,
    FILE_SECTION,
    TIME_RANGE,
    CONTACT_NAME,
    MESSAGE_CHANNEL,
    MESSAGE_BODY,
    SPORT_ENTITY,
    LOCATION,
    DEVICE_PARAMETER,
    PERCENTAGE,
    DURATION_MINUTES,
    TOPIC_QUERY,
}

data class ExtractedEntity(
    val type: EntityType,
    val value: String,
    val normalized: String = normalize(value),
)

enum class RequiredCapability {
    PACKAGE_RESOLVE,
    STORAGE_INDEX,
    DOCUMENT_TEXT_EXTRACTION,
    AUDIO_MANAGER,
    MEDIA_SESSION,
    SHIZUKU_SETTINGS,
    CONTACTS_PROVIDER,
    CALENDAR_PROVIDER,
    NOTIFICATION_LISTENER,
    NETWORK_PROVIDER,
    AI_PROVIDER,
}

enum class RequiredPermission(val accessKey: String) {
    READ_EXTERNAL_STORAGE_OR_SAF("files"),
    READ_CONTACTS("contacts"),
    READ_CALENDAR("calendar"),
    NOTIFICATION_LISTENER("notifications"),
    SHIZUKU_ADB("shizuku"),
    AI_CONTEXT_CONSENT("ai_context"),
}

enum class RemoteAiEscalation {
    NONE,
    EXPLICIT_PRIMARY,
    EXPLICIT_WITH_CONTEXT_CONFIRMATION,
    EXPLICIT_TOOL_PLAN,
    OPTIONAL_SECONDARY,
}

data class BarPresentationSpec(
    val primaryTitle: String,
    val primarySubtitle: String,
    val badgeLabel: String,
    val footerLabel: String,
    val hints: List<String> = emptyList(),
)

data class DocumentAiCompoundPlan(
    val originalQuery: String,
    val fileKind: String,
    val searchTerms: String,
    val dateConstraint: FileConstraint? = null,
    val aiInstruction: String,
    val sectionTarget: String = "",
) {
    val fileQuery: String
        get() = dateConstraint?.query ?: listOf(
            if (fileKind.isNotBlank()) "kind:$fileKind" else "",
            searchTerms.trim(),
        ).filter { it.isNotBlank() }.joinToString(" ")
}

data class SectionExcerpt(
    val matchedSection: Boolean,
    val heading: String,
    val excerpt: String,
)

data class IntentResolution(
    val originalQuery: String,
    val canonicalQuery: String,
    val primaryIntent: PrimaryIntentKind,
    val entities: List<ExtractedEntity>,
    val capabilities: Set<RequiredCapability>,
    val tools: List<String>,
    val permissions: Set<RequiredPermission>,
    val aiEscalation: RemoteAiEscalation,
    val requiresConfirmation: Boolean,
    val confirmationReason: String? = null,
    val presentation: BarPresentationSpec,
    val localPlan: LocalPlan? = null,
    val documentAiPlan: DocumentAiCompoundPlan? = null,
    val fileConstraint: FileConstraint? = null,
)

object DocumentSectionLocator {
    fun locate(fullText: String, targetSection: String, maxChars: Int = 10000): SectionExcerpt {
        val lines = fullText.trim().lines()
        if (lines.all { it.isBlank() }) return SectionExcerpt(false, "", "")
        val target = normalize(targetSection).trim()
        if (target.isBlank()) return SectionExcerpt(false, "Extracto inicial del documento", fullText.trim().take(maxChars))
        val number = Regex("\\b(\\d{1,3}|[ivxlcdm]+)\\b").find(target)?.value
        val roman = mapOf("1" to "i", "2" to "ii", "3" to "iii", "4" to "iv", "5" to "v", "6" to "vi", "7" to "vii", "8" to "viii", "9" to "ix", "10" to "x")
        val family = target.substringBeforeLast(' ', "")
        val variants = if (number == null) listOf(target) else {
            val numbers = listOfNotNull(number, roman[number], roman.entries.firstOrNull { it.value == number }?.key).distinct()
            val families = if (family in setOf("capitulo", "cap", "cap.")) listOf("capitulo", "cap", "cap.") else listOf(family)
            families.flatMap { label -> numbers.map { "$label $it" } }
        }
        val heading = Regex("^(?:" + variants.joinToString("|") { Regex.escape(it) } + ")(?:$|[ .:—–-])")
        val start = lines.indexOfFirst { heading.containsMatchIn(normalize(it.trim())) }
        if (start < 0) return SectionExcerpt(false, "Sección no localizada · extracto inicial", fullText.trim().take(maxChars))
        // Do not label every following chapter as the requested chapter.
        val sibling = Regex("^(?:capitulo|cap\\.?|unidad|tema|seccion|parte|apartado) +(?:\\d{1,3}|[ivxlcdm]+)(?:$|[ .:—–-])")
        val end = (start + 1 until lines.size).firstOrNull { sibling.containsMatchIn(normalize(lines[it].trim())) } ?: lines.size
        return SectionExcerpt(true, lines[start].trim(), lines.subList(start, end).joinToString("\n").trim().take(maxChars))
    }
}

/**
 * Deterministic, side-effect-free Universal Intent Engine.
 * Never performs network requests or mutates state while the user types.
 */
object UniversalIntentEngine {
    fun aiProviderLabel(c: Config): String = when (c.aiProvider) {
        "gemini" -> "Gemini"
        "openai" -> "OpenAI"
        else -> "IA"
    }

    fun primaryAiTitle(c: Config): String =
        if (c.aiProvider == "gemini") "Responder con Gemini" else "Responder con IA"

    fun parseDocumentAiCompound(
        raw: String,
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): DocumentAiCompoundPlan? {
        if (raw.length > 4000 || raw.contains('"')) return null
        val trimmed = raw.trim().replace(Regex("\\s+"), " ")
        val splitRegex = Regex(
            "^(.+?)\\s+(?:y|luego|despu[eé]s)\\s+((?:explic(?:a|ame|[aá])|resum(?:i|ime|ir|[ií])|analiz(?:a|ame|[aá])|traduc(?:i|ime|ir|[ií])|correg(?:i|ime|ir|[ií])|extra(?:e|eme|er|[eé])|compar(?:a|ame|ar|[aá])|sintetiz(?:a|ame|ar|[aá])|contame|decime|respond(?:e|eme|[eé])|busc(?:a|ame|[aá])\\s+en\\s+el\\s+documento)\\b.+)$",
            RegexOption.IGNORE_CASE,
        )
        val match = splitRegex.matchEntire(trimmed) ?: return null
        val firstPart = match.groupValues[1].trim()
        val secondPart = match.groupValues[2].trim()
        val fileConstraint = QueryInterpreter.files(firstPart, today, zone)
            ?: parseFlexibleFileClause(firstPart, today, zone)
            ?: return null
        val sectionRegex = Regex(
            "\\b((?:el\\s+|la\\s+)?(?:cap[ií]tulo|cap|tema|unidad|secci[oó]n|parte|apartado|punto)\\s+(?:\\d{1,3}|[ivxlcdm]{1,6})|introducci[oó]n|conclusi[oó]n|resumen)\\b",
            RegexOption.IGNORE_CASE,
        )
        val section = sectionRegex.find(secondPart)?.groupValues?.get(1)
            ?.replace(Regex("^(?:el|la)\\s+", RegexOption.IGNORE_CASE), "")
            ?.trim()
            .orEmpty()
        return DocumentAiCompoundPlan(
            originalQuery = raw.trim(),
            fileKind = fileConstraint.kind,
            searchTerms = fileConstraint.terms,
            dateConstraint = fileConstraint,
            aiInstruction = secondPart,
            sectionTarget = section,
        )
    }

    private fun parseFlexibleFileClause(
        clause: String,
        today: LocalDate,
        zone: ZoneId,
    ): FileConstraint? {
        val norm = normalize(clause.trim()).replace(Regex("\\s+"), " ")
        val stripped = norm.replace(
            Regex("^(?:buscar|busca|buscame|mostrame|mostrar|abrir|abri|abre|encontrar|encontra)(?:me)?(?: todos| todas)?(?: mis| los| las| un| una| el| la)? "),
            "",
        )
        val kindMatch = Regex("^(pdf|pdfs|documento|documentos|archivo|archivos|apunte|apuntes)(?: de| sobre)? (.+)$")
            .matchEntire(stripped) ?: return null
        val head = kindMatch.groupValues[1]
        val kind = if (head.startsWith("pdf")) "pdf" else "document"
        val rawTerms = kindMatch.groupValues[2]
            .replace(Regex("\\bque (?:abri|abriste|descargue|vi|guarde|modifique|le[ií])\\b"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
        val syntheticQuery = "buscar $head de $rawTerms"
        return QueryInterpreter.files(syntheticQuery, today, zone)
            ?: FileConstraint(kind = kind, terms = rawTerms)
    }

    fun resolve(
        raw: String,
        config: Config = Config(),
        today: LocalDate = LocalDate.now(),
        zone: ZoneId = ZoneId.systemDefault(),
    ): IntentResolution {
        val clean = raw.trim()
        val canonical = LocalLanguage.canonical(clean)
        val norm = normalize(clean).removePrefix("¿").removeSuffix("?").trim()
        val providerName = aiProviderLabel(config)

        // 1. Compound Document + AI Action ("Buscá el PDF de rehabilitación vestibular y explicame el capítulo 3")
        parseDocumentAiCompound(clean, today, zone)?.let { docPlan ->
            val entities = buildList {
                add(ExtractedEntity(EntityType.FILE_KIND, docPlan.fileKind))
                if (docPlan.searchTerms.isNotBlank()) add(ExtractedEntity(EntityType.FILE_TOPIC, docPlan.searchTerms))
                if (docPlan.sectionTarget.isNotBlank()) add(ExtractedEntity(EntityType.FILE_SECTION, docPlan.sectionTarget))
                if (docPlan.dateConstraint?.dateLabel?.isNotBlank() == true) {
                    add(ExtractedEntity(EntityType.TIME_RANGE, docPlan.dateConstraint.dateLabel))
                }
                add(ExtractedEntity(EntityType.TOPIC_QUERY, docPlan.aiInstruction))
            }
            val hints = listOfNotNull(
                docPlan.fileKind.uppercase(),
                docPlan.searchTerms.takeIf { it.isNotBlank() },
                docPlan.sectionTarget.takeIf { it.isNotBlank() },
                "Confirmar antes de enviar a $providerName",
            )
            return IntentResolution(
                originalQuery = clean,
                canonicalQuery = canonical,
                primaryIntent = PrimaryIntentKind.COMPOUND_ACTION,
                entities = entities,
                capabilities = setOf(
                    RequiredCapability.STORAGE_INDEX,
                    RequiredCapability.DOCUMENT_TEXT_EXTRACTION,
                    RequiredCapability.AI_PROVIDER,
                ),
                tools = listOf("search_files"),
                permissions = setOf(
                    RequiredPermission.READ_EXTERNAL_STORAGE_OR_SAF,
                    RequiredPermission.AI_CONTEXT_CONSENT,
                ),
                aiEscalation = RemoteAiEscalation.EXPLICIT_WITH_CONTEXT_CONFIRMATION,
                requiresConfirmation = true,
                confirmationReason = "Extrae contenido del documento local y solicita autorización explícita antes de enviarlo a $providerName.",
                presentation = BarPresentationSpec(
                    primaryTitle = "Preparar consulta sobre PDF con $providerName",
                    primarySubtitle = "1. Buscar «${docPlan.searchTerms.ifBlank { docPlan.fileKind }}» · 2. ${if (docPlan.sectionTarget.isNotBlank()) "Localizar ${docPlan.sectionTarget}" else "Extraer texto"} · 3. Autorizar envío",
                    badgeLabel = "Intención",
                    footerLabel = "Revisar plan",
                    hints = hints,
                ),
                documentAiPlan = docPlan,
                fileConstraint = docPlan.dateConstraint,
            )
        }

        // 2. Compound Device + App Action ("Abrí Spotify y bajá el volumen al 40%")
        LocalPlan.parse(clean)?.let { plan ->
            val entities = plan.steps.mapNotNull { step ->
                when (step) {
                    is PlanStep.Open -> ExtractedEntity(EntityType.APP_NAME, step.name)
                    is PlanStep.Volume -> ExtractedEntity(EntityType.PERCENTAGE, step.percent.toString())
                    is PlanStep.Brightness -> ExtractedEntity(EntityType.PERCENTAGE, step.percent.toString())
                    is PlanStep.RelativeVolume -> ExtractedEntity(EntityType.PERCENTAGE, step.delta.toString())
                    is PlanStep.RelativeBrightness -> ExtractedEntity(EntityType.PERCENTAGE, step.delta.toString())
                    is PlanStep.Media -> ExtractedEntity(EntityType.DEVICE_PARAMETER, step.command)
                }
            }
            val needsShizuku = plan.steps.any { it is PlanStep.Brightness || it is PlanStep.RelativeBrightness }
            val needsMedia = plan.steps.any { it is PlanStep.Media }
            val tools = plan.steps.map { step ->
                when (step) {
                    is PlanStep.Open -> "open_app"
                    is PlanStep.Volume, is PlanStep.RelativeVolume -> "set_volume"
                    is PlanStep.Brightness, is PlanStep.RelativeBrightness -> "write_display_setting"
                    is PlanStep.Media -> "control_media"
                }
            }.distinct()
            val caps = buildSet {
                if (plan.steps.any { it is PlanStep.Open }) add(RequiredCapability.PACKAGE_RESOLVE)
                if (plan.steps.any { it is PlanStep.Volume || it is PlanStep.RelativeVolume }) add(RequiredCapability.AUDIO_MANAGER)
                if (needsShizuku) add(RequiredCapability.SHIZUKU_SETTINGS)
                if (needsMedia) add(RequiredCapability.MEDIA_SESSION)
            }
            val perms = buildSet {
                if (needsShizuku) add(RequiredPermission.SHIZUKU_ADB)
                if (needsMedia) add(RequiredPermission.NOTIFICATION_LISTENER)
            }
            return IntentResolution(
                originalQuery = clean,
                canonicalQuery = canonical,
                primaryIntent = PrimaryIntentKind.COMPOUND_ACTION,
                entities = entities,
                capabilities = caps,
                tools = tools,
                permissions = perms,
                aiEscalation = RemoteAiEscalation.NONE,
                requiresConfirmation = true,
                confirmationReason = "Secuencia de ${plan.steps.size} acciones locales; requiere revisar y confirmar pasos.",
                presentation = BarPresentationSpec(
                    primaryTitle = "Revisar interpretación",
                    primarySubtitle = "Plan local · comprobar permisos y confirmar cada paso",
                    badgeLabel = "Intención",
                    footerLabel = "Revisar plan",
                    hints = listOf("${plan.steps.size} pasos", "Confirmación requerida"),
                ),
                localPlan = plan,
            )
        }

        // 3. Focus / Study without distractions ("Quiero estudiar sin distracciones")
        if (norm in setOf("quiero estudiar sin distracciones", "estudiar sin distracciones", "modo estudio", "concentracion", "sesion de concentracion", "pomodoro")) {
            return IntentResolution(
                originalQuery = clean,
                canonicalQuery = canonical,
                primaryIntent = PrimaryIntentKind.FOCUS_STUDY,
                entities = listOf(ExtractedEntity(EntityType.DURATION_MINUTES, "25")),
                capabilities = setOf(RequiredCapability.AUDIO_MANAGER),
                tools = listOf("set_volume"),
                permissions = emptySet(),
                aiEscalation = RemoteAiEscalation.NONE,
                requiresConfirmation = true,
                confirmationReason = "Permite elegir ajuste de volumen multimedia, temporizador y sonido sin modificar permisos automáticamente.",
                presentation = BarPresentationSpec(
                    primaryTitle = "Revisar interpretación",
                    primarySubtitle = "Elegir contexto y revisar acciones disponibles",
                    badgeLabel = "Intención",
                    footerLabel = "Revisar plan",
                    hints = listOf("Concentración", "Acciones locales"),
                ),
            )
        }

        // 4. Contextual AI on Active/Selected Document or Text ("Explicame este documento", "Resumí este PDF")
        if (
            norm.contains("este archivo") ||
                norm.contains("este documento") ||
                norm.contains("este pdf") ||
                norm.contains("este texto") ||
                norm.contains("este portapapeles") ||
                norm in setOf(
                    "explicame esto",
                    "resumir esto",
                    "resumi esto",
                    "traducir esto",
                    "traduci esto",
                    "traduce este texto",
                    "corregi esto",
                    "corregir esto",
                    "corregime esto",
                    "analiza esto",
                    "analizame esto",
                ) ||
                Regex("\\bestos(?: +\\d{1,2})? +(documentos|archivos|pdfs)\\b").containsMatchIn(norm)
        ) {
            return IntentResolution(
                originalQuery = clean,
                canonicalQuery = canonical,
                primaryIntent = PrimaryIntentKind.CONTEXT_AI,
                entities = listOf(ExtractedEntity(EntityType.TOPIC_QUERY, clean)),
                capabilities = setOf(
                    RequiredCapability.DOCUMENT_TEXT_EXTRACTION,
                    RequiredCapability.AI_PROVIDER,
                ),
                tools = listOf("search_files"),
                permissions = setOf(
                    RequiredPermission.READ_EXTERNAL_STORAGE_OR_SAF,
                    RequiredPermission.AI_CONTEXT_CONSENT,
                ),
                aiEscalation = RemoteAiEscalation.EXPLICIT_WITH_CONTEXT_CONFIRMATION,
                requiresConfirmation = true,
                confirmationReason = "El contenido del documento o texto debe revisarse y autorizarse antes de enviarse a $providerName.",
                presentation = BarPresentationSpec(
                    primaryTitle = "Trabajar con contexto autorizado",
                    primarySubtitle = "Elegir o revisar el contenido antes de usar IA",
                    badgeLabel = "IA",
                    footerLabel = "Revisar contexto",
                    hints = listOf("Contexto local", "Confirmación previa"),
                ),
            )
        }

        // 5. File Search ("Buscá el PDF de anatomía que abrí ayer", "Mostrame mis archivos PDF")
        QueryInterpreter.files(clean, today, zone)?.let { fc ->
            val entities = buildList {
                add(ExtractedEntity(EntityType.FILE_KIND, fc.kind))
                if (fc.terms.isNotBlank()) add(ExtractedEntity(EntityType.FILE_TOPIC, fc.terms))
                if (fc.dateLabel.isNotBlank()) add(ExtractedEntity(EntityType.TIME_RANGE, fc.dateLabel))
            }
            val hints = listOf(
                when (fc.kind) {
                    "pdf" -> "PDF"
                    "image" -> "Imágenes"
                    "video" -> "Vídeos"
                    else -> "Documentos"
                },
                fc.dateLabel,
                fc.terms,
                "Archivos locales",
            ).filter { it.isNotBlank() }
            return IntentResolution(
                originalQuery = clean,
                canonicalQuery = canonical,
                primaryIntent = PrimaryIntentKind.FILE_SEARCH,
                entities = entities,
                capabilities = setOf(RequiredCapability.STORAGE_INDEX),
                tools = listOf("search_files"),
                permissions = setOf(RequiredPermission.READ_EXTERNAL_STORAGE_OR_SAF),
                aiEscalation = RemoteAiEscalation.NONE,
                requiresConfirmation = false,
                presentation = BarPresentationSpec(
                    primaryTitle = "Buscar archivos",
                    primarySubtitle = fc.query,
                    badgeLabel = "Archivos",
                    footerLabel = "Abrir documento",
                    hints = hints,
                ),
                fileConstraint = fc,
            )
        }

        // 6. Communication ("Mandale un WhatsApp a Juan")
        val msgAction = (IntentRouter.route(clean, config.messageApp) as? Action.ComposeMessage)
            ?: ActionCatalog.message(clean, config.messageApp)
        if (msgAction != null) {
            val entities = buildList {
                add(ExtractedEntity(EntityType.MESSAGE_CHANNEL, msgAction.channel))
                if (msgAction.target.isNotBlank()) add(ExtractedEntity(EntityType.CONTACT_NAME, msgAction.target))
                if (msgAction.text.isNotBlank()) add(ExtractedEntity(EntityType.MESSAGE_BODY, msgAction.text))
            }
            return IntentResolution(
                originalQuery = clean,
                canonicalQuery = canonical,
                primaryIntent = PrimaryIntentKind.COMMUNICATION,
                entities = entities,
                capabilities = setOf(RequiredCapability.PACKAGE_RESOLVE, RequiredCapability.CONTACTS_PROVIDER),
                tools = listOf("prepare_message", "search_contacts"),
                permissions = setOf(RequiredPermission.READ_CONTACTS),
                aiEscalation = RemoteAiEscalation.NONE,
                requiresConfirmation = true,
                confirmationReason = "El mensaje nunca se envía automáticamente; el usuario elige el destinatario y confirma en la app.",
                presentation = BarPresentationSpec(
                    primaryTitle = "Preparar mensaje a ${msgAction.target}",
                    primarySubtitle = "Revisar destinatario · requiere app compatible y número; contactos opcionales",
                    badgeLabel = "Contactos y comunicación",
                    footerLabel = "Preparar mensaje",
                ),
            )
        }

        // 7. Weather ("Va a llover mañana en Mar del Plata", "Clima en Córdoba")
        if (WeatherRequests.matches(clean) || norm.startsWith("clima")) {
            val city = WeatherRequests.extractLocation(clean)
            val tomorrow = Regex("\\bma[nñ]ana\\b", RegexOption.IGNORE_CASE).containsMatchIn(clean)
            val entities = buildList {
                if (tomorrow) add(ExtractedEntity(EntityType.TIME_RANGE, "tomorrow"))
                if (city.isNotBlank()) add(ExtractedEntity(EntityType.LOCATION, city))
            }
            return IntentResolution(
                originalQuery = clean,
                canonicalQuery = canonical,
                primaryIntent = PrimaryIntentKind.WEATHER,
                entities = entities,
                capabilities = setOf(RequiredCapability.NETWORK_PROVIDER),
                tools = emptyList(),
                permissions = emptySet(),
                aiEscalation = RemoteAiEscalation.NONE,
                requiresConfirmation = false,
                presentation = BarPresentationSpec(
                    primaryTitle = if (tomorrow) "Pronóstico de mañana" else "Consultar pronóstico",
                    primarySubtitle = "Open-Meteo · consulta solo al seleccionar",
                    badgeLabel = "Clima",
                    footerLabel = "Consultar clima",
                ),
            )
        }

        // 8. Sports ("Cómo va River", "Boca próximo partido")
        val sportsMatch = Regex("^¿?(?:c[oó]mo va|resultado de|partido de|c[oó]mo sali[oó]|cu[aá]ndo juega) (.+?)\\??$", RegexOption.IGNORE_CASE)
            .matchEntire(clean)
        val sportsReq = SportsRequest.parse(clean)
        if (sportsMatch != null || sportsReq != null) {
            val entityName = sportsMatch?.groupValues?.get(1)?.trim() ?: sportsReq?.entity.orEmpty()
            return IntentResolution(
                originalQuery = clean,
                canonicalQuery = canonical,
                primaryIntent = PrimaryIntentKind.SPORTS,
                entities = if (entityName.isNotBlank()) listOf(ExtractedEntity(EntityType.SPORT_ENTITY, entityName)) else emptyList(),
                capabilities = setOf(RequiredCapability.NETWORK_PROVIDER),
                tools = emptyList(),
                permissions = emptySet(),
                aiEscalation = RemoteAiEscalation.NONE,
                requiresConfirmation = false,
                presentation = BarPresentationSpec(
                    primaryTitle = if (entityName.isNotBlank()) "Consultar $entityName" else "Consultar agenda deportiva",
                    primarySubtitle = "Elegir equipo o competición real · datos del proveedor",
                    badgeLabel = "Deportes",
                    footerLabel = "Ver agenda",
                ),
            )
        }

        // 9. Calendar ("¿Qué tengo mañana?", "Crear evento mañana a las 10")
        if (
            norm in setOf("que tengo manana", "mi agenda manana", "agenda manana", "que tengo hoy", "mi agenda hoy", "agenda hoy", "calendario", "mi calendario", "agenda", "mi agenda") ||
                CalendarLanguage.draft(clean) != null ||
                CalendarLanguage.free(clean) != null ||
                NaturalIntents.event(clean) != null
        ) {
            val period = if (norm.contains("manana")) "tomorrow" else "today"
            return IntentResolution(
                originalQuery = clean,
                canonicalQuery = canonical,
                primaryIntent = PrimaryIntentKind.CALENDAR,
                entities = listOf(ExtractedEntity(EntityType.TIME_RANGE, period)),
                capabilities = setOf(RequiredCapability.CALENDAR_PROVIDER),
                tools = listOf("calendar_agenda"),
                permissions = setOf(RequiredPermission.READ_CALENDAR),
                aiEscalation = RemoteAiEscalation.NONE,
                requiresConfirmation = CalendarLanguage.draft(clean) != null || NaturalIntents.event(clean) != null,
                presentation = BarPresentationSpec(
                    primaryTitle = "Calendario",
                    primarySubtitle = "Calendarios sincronizados · permisos opcionales",
                    badgeLabel = "Agenda",
                    footerLabel = "Abrir agenda",
                ),
            )
        }

        // 10. Open App ("Abrí Spotify") or Device Control ("Volumen al 40%", "Brillo al 35%")
        UniversalIntent.recognize(clean)?.let { uni ->
            when (uni) {
                is UniversalIntent.OpenApp -> return IntentResolution(
                    originalQuery = clean,
                    canonicalQuery = canonical,
                    primaryIntent = PrimaryIntentKind.OPEN_APP,
                    entities = listOf(ExtractedEntity(EntityType.APP_NAME, uni.name)),
                    capabilities = setOf(RequiredCapability.PACKAGE_RESOLVE),
                    tools = listOf("open_app", "search_apps"),
                    permissions = emptySet(),
                    aiEscalation = RemoteAiEscalation.NONE,
                    requiresConfirmation = false,
                    presentation = BarPresentationSpec(
                        primaryTitle = uni.name,
                        primarySubtitle = "Acción local · abrir aplicación instalada",
                        badgeLabel = "Aplicación",
                        footerLabel = "Abrir aplicación",
                    ),
                )
                is UniversalIntent.Volume -> return IntentResolution(
                    originalQuery = clean,
                    canonicalQuery = canonical,
                    primaryIntent = PrimaryIntentKind.DEVICE_CONTROL,
                    entities = listOf(
                        ExtractedEntity(EntityType.DEVICE_PARAMETER, "volumen"),
                        ExtractedEntity(EntityType.PERCENTAGE, uni.percent.toString()),
                    ),
                    capabilities = setOf(RequiredCapability.AUDIO_MANAGER),
                    tools = listOf("set_volume"),
                    permissions = emptySet(),
                    aiEscalation = RemoteAiEscalation.NONE,
                    requiresConfirmation = false,
                    presentation = BarPresentationSpec(
                        primaryTitle = "Volumen al ${uni.percent}%",
                        primarySubtitle = "Acción local · volumen multimedia",
                        badgeLabel = "Acciones",
                        footerLabel = "Ejecutar",
                    ),
                )
                is UniversalIntent.Information -> return IntentResolution(
                    originalQuery = clean,
                    canonicalQuery = canonical,
                    primaryIntent = PrimaryIntentKind.KNOWLEDGE_AI,
                    entities = listOf(ExtractedEntity(EntityType.TOPIC_QUERY, uni.query)),
                    capabilities = setOf(RequiredCapability.AI_PROVIDER),
                    tools = emptyList(),
                    permissions = emptySet(),
                    aiEscalation = RemoteAiEscalation.EXPLICIT_PRIMARY,
                    requiresConfirmation = false,
                    presentation = BarPresentationSpec(
                        primaryTitle = primaryAiTitle(config),
                        primarySubtitle = "Solo al seleccionar · respuesta con streaming y Markdown",
                        badgeLabel = "IA",
                        footerLabel = primaryAiTitle(config),
                    ),
                )
                is UniversalIntent.Photos -> return IntentResolution(
                    originalQuery = clean,
                    canonicalQuery = canonical,
                    primaryIntent = PrimaryIntentKind.FILE_SEARCH,
                    entities = listOf(ExtractedEntity(EntityType.FILE_KIND, "image")),
                    capabilities = setOf(RequiredCapability.STORAGE_INDEX),
                    tools = listOf("search_files"),
                    permissions = setOf(RequiredPermission.READ_EXTERNAL_STORAGE_OR_SAF),
                    aiEscalation = RemoteAiEscalation.NONE,
                    requiresConfirmation = false,
                    presentation = BarPresentationSpec(
                        primaryTitle = "Buscar fotos",
                        primarySubtitle = uni.query,
                        badgeLabel = "Archivos",
                        footerLabel = "Abrir imagen",
                    ),
                )
                is UniversalIntent.InvalidLevel -> return IntentResolution(
                    originalQuery = clean,
                    canonicalQuery = canonical,
                    primaryIntent = PrimaryIntentKind.DEVICE_CONTROL,
                    entities = listOf(ExtractedEntity(EntityType.DEVICE_PARAMETER, uni.device)),
                    capabilities = emptySet(),
                    tools = emptyList(),
                    permissions = emptySet(),
                    aiEscalation = RemoteAiEscalation.NONE,
                    requiresConfirmation = false,
                    presentation = BarPresentationSpec(
                        primaryTitle = "Usá un porcentaje entre 0 y 100",
                        primarySubtitle = "El ${uni.device} no se modifica con este valor",
                        badgeLabel = "Información",
                        footerLabel = "Corregir",
                    ),
                )
            }
        }

        // 11. Prefer deterministic local arithmetic over generic question routing
        val calculation = Calculator.evaluate(clean)
        val conversion = if (calculation == null) LocalTools.convert(clean) else null
        val localAnswer = calculation ?: conversion?.title
        localAnswer?.let { answer ->
            return IntentResolution(
                originalQuery = clean,
                canonicalQuery = canonical,
                primaryIntent = PrimaryIntentKind.LOCAL_TOOL,
                entities = listOf(ExtractedEntity(EntityType.TOPIC_QUERY, clean)),
                capabilities = emptySet(),
                tools = listOf(if (calculation != null) "calculate" else "convert_units"),
                permissions = emptySet(),
                aiEscalation = RemoteAiEscalation.NONE,
                requiresConfirmation = false,
                presentation = BarPresentationSpec(
                    primaryTitle = answer,
                    primarySubtitle = conversion?.subtitle ?: "$clean · tocar para copiar",
                    badgeLabel = if (calculation != null) "Calculadora" else "Conversión",
                    footerLabel = "Copiar",
                ),
            )
        }

        // 12. Informational / Knowledge Question ("¿Cuántas Libertadores ganó Boca?", "cuantas veces gano boca la libertadores")
        if (LocalLanguage.isQuestion(clean)) {
            return IntentResolution(
                originalQuery = clean,
                canonicalQuery = canonical,
                primaryIntent = PrimaryIntentKind.KNOWLEDGE_AI,
                entities = listOf(ExtractedEntity(EntityType.TOPIC_QUERY, clean)),
                capabilities = setOf(RequiredCapability.AI_PROVIDER),
                tools = emptyList(),
                permissions = emptySet(),
                aiEscalation = RemoteAiEscalation.EXPLICIT_PRIMARY,
                requiresConfirmation = false,
                presentation = BarPresentationSpec(
                    primaryTitle = primaryAiTitle(config),
                    primarySubtitle = "Requiere conexión · consulta únicamente al presionar Enter",
                    badgeLabel = "IA",
                    footerLabel = primaryAiTitle(config),
                ),
            )
        }

        // 13. Default: Unified General Search (apps, files, commands, with optional explicit AI escalation for complex queries)
        val wordCount = norm.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val complexActionVerb = Regex("^(?:organiza|automatiza|configura|prepara|coordina|arma)\\b").containsMatchIn(norm)
        return IntentResolution(
            originalQuery = clean,
            canonicalQuery = canonical,
            primaryIntent = PrimaryIntentKind.GENERAL_SEARCH,
            entities = if (clean.isNotBlank()) listOf(ExtractedEntity(EntityType.TOPIC_QUERY, clean)) else emptyList(),
            capabilities = setOf(RequiredCapability.PACKAGE_RESOLVE, RequiredCapability.STORAGE_INDEX),
            tools = if (complexActionVerb) listOf("search_apps", "search_files", "set_volume") else emptyList(),
            permissions = emptySet(),
            aiEscalation = if (complexActionVerb && wordCount >= 3) RemoteAiEscalation.EXPLICIT_TOOL_PLAN else RemoteAiEscalation.NONE,
            requiresConfirmation = complexActionVerb,
            presentation = BarPresentationSpec(
                primaryTitle = clean,
                primarySubtitle = "Búsqueda unificada",
                badgeLabel = "Todo",
                footerLabel = "Ejecutar",
            ),
        )
    }
}
