package com.revolution.ai.engine.command

data class ParsedCommand(
    val type: CommandType,
    val target: String = "",
    val content: String = "",
    val appPackage: String = "",
    val extras: Map<String, String> = emptyMap()
)

enum class CommandType {
    CALL, SMS, MESSAGE_APP, OPEN_APP, WEB_SEARCH, PLAY_MEDIA,
    TOGGLE_WIFI, TOGGLE_BLUETOOTH, TOGGLE_HOTSPOT, TOGGLE_GPS,
    SET_ALARM, SEND_EMAIL, SCHEDULE_MEETING, TAKE_PHOTO, RECORD_VIDEO,
    NAVIGATE, SHARE_LOCATION, IOT_CONTROL, SCHEDULE_TASK,
    UPI_PAYMENT, READ_NEWS, SEND_FILE, UNKNOWN
}

class CommandParser {

    private data class PatternMapping(
        val regex: Regex,
        val type: CommandType,
        val extractor: (MatchResult, String) -> ParsedCommand
    )

    private val patterns: List<PatternMapping> = buildPatterns()

    fun parse(input: String): ParsedCommand {
        val lower = input.lowercase().trim()

        for (mapping in patterns) {
            val match = mapping.regex.find(lower)
            if (match != null) {
                return mapping.extractor(match, lower)
            }
        }

        if (matchesKeywords(lower, listOf("call", "phone", "dial", "ring"))) {
            return parseCallFallback(lower)
        }
        if (matchesKeywords(lower, listOf("message", "text", "sms"))) {
            return parseSmsFallback(lower)
        }
        if (matchesKeywords(lower, listOf("open", "launch", "start", "run"))) {
            return parseOpenAppFallback(lower)
        }

        return ParsedCommand(type = CommandType.UNKNOWN, content = input)
    }

    private fun buildPatterns(): List<PatternMapping> {
        val list = mutableListOf<PatternMapping>()

        // --- CALL (with dual SIM support) ---
        list.add(PatternMapping(
            Regex("""(?:call|phone|dial|ring)\s+(.+?)\s+(?:on|using|with|from)\s+sim\s*(\d)"""),
            CommandType.CALL
        ) { m, _ ->
            ParsedCommand(
                type = CommandType.CALL,
                target = cleanContactName(m.groupValues[1]),
                extras = mapOf("sim" to m.groupValues[2])
            )
        })
        list.add(PatternMapping(
            Regex("""(?:call|phone|dial|ring)\s+(.+?)(?:\s+on\s+(\w+))?$"""),
            CommandType.CALL
        ) { m, _ ->
            val extras = if (m.groupValues[2].isNotBlank()) mapOf("via" to m.groupValues[2]) else emptyMap()
            ParsedCommand(type = CommandType.CALL, target = cleanContactName(m.groupValues[1]), extras = extras)
        })
        list.add(PatternMapping(
            Regex("""(?:make|place)\s+(?:a\s+)?(?:phone\s+)?call\s+to\s+(.+)"""),
            CommandType.CALL
        ) { m, _ ->
            ParsedCommand(type = CommandType.CALL, target = cleanContactName(m.groupValues[1]))
        })

        // --- SMS ---
        list.add(PatternMapping(
            Regex("""(?:send|write)\s+(?:a\s+)?(?:sms|text|message)\s+to\s+(.+?)\s+(?:saying|that|with message|with text|message)\s+(.+)"""),
            CommandType.SMS
        ) { m, _ ->
            ParsedCommand(type = CommandType.SMS, target = cleanContactName(m.groupValues[1]), content = m.groupValues[2].trim())
        })
        list.add(PatternMapping(
            Regex("""(?:text|sms)\s+(.+?)\s+(?:saying|that|with message)\s+(.+)"""),
            CommandType.SMS
        ) { m, _ ->
            ParsedCommand(type = CommandType.SMS, target = cleanContactName(m.groupValues[1]), content = m.groupValues[2].trim())
        })
        list.add(PatternMapping(
            Regex("""(?:send|write)\s+(?:a\s+)?(?:sms|text|message)\s+to\s+(.+)"""),
            CommandType.SMS
        ) { m, _ ->
            ParsedCommand(type = CommandType.SMS, target = cleanContactName(m.groupValues[1]))
        })
        list.add(PatternMapping(
            Regex("""(?:text|sms)\s+(.+)"""),
            CommandType.SMS
        ) { m, _ ->
            ParsedCommand(type = CommandType.SMS, target = cleanContactName(m.groupValues[1]))
        })

        // --- MESSAGE APP (WhatsApp, Telegram, etc.) ---
        list.add(PatternMapping(
            Regex("""(?:send|write)\s+(?:a\s+)?(?:whatsapp|telegram|signal|viber)\s+(?:message\s+)?to\s+(.+?)\s+(?:saying|that|with message)\s+(.+)"""),
            CommandType.MESSAGE_APP
        ) { m, lower ->
            val app = extractMessagingApp(lower)
            ParsedCommand(
                type = CommandType.MESSAGE_APP,
                target = cleanContactName(m.groupValues[1]),
                content = m.groupValues[2].trim(),
                appPackage = app
            )
        })
        list.add(PatternMapping(
            Regex("""(?:whatsapp|telegram|signal|viber)\s+(.+?)\s+(?:saying|that|with message)\s+(.+)"""),
            CommandType.MESSAGE_APP
        ) { m, lower ->
            val app = extractMessagingApp(lower)
            ParsedCommand(
                type = CommandType.MESSAGE_APP,
                target = cleanContactName(m.groupValues[1]),
                content = m.groupValues[2].trim(),
                appPackage = app
            )
        })
        list.add(PatternMapping(
            Regex("""(?:whatsapp|telegram|signal|viber)\s+(.+)"""),
            CommandType.MESSAGE_APP
        ) { m, lower ->
            val app = extractMessagingApp(lower)
            ParsedCommand(type = CommandType.MESSAGE_APP, target = cleanContactName(m.groupValues[1]), appPackage = app)
        })
        list.add(PatternMapping(
            Regex("""(?:send|write)\s+(?:a\s+)?message\s+(?:on|via|through|using)\s+(whatsapp|telegram|signal|viber)\s+to\s+(.+)"""),
            CommandType.MESSAGE_APP
        ) { m, _ ->
            val app = getMessagingAppPackage(m.groupValues[1])
            ParsedCommand(type = CommandType.MESSAGE_APP, target = cleanContactName(m.groupValues[2]), appPackage = app)
        })

        // --- UPI PAYMENT ---
        list.add(PatternMapping(
            Regex("""(?:pay|send|transfer)\s+(?:rs\.?|₹|inr)?\s*(\d+)\s+(?:to|for)\s+(.+?)\s+(?:via|using|on|through)\s+(?:upi|gpay|phonepe|paytm|bhim)"""),
            CommandType.UPI_PAYMENT
        ) { m, lower ->
            val paymentApp = extractPaymentApp(lower)
            ParsedCommand(
                type = CommandType.UPI_PAYMENT,
                target = cleanContactName(m.groupValues[2]),
                content = m.groupValues[1],
                appPackage = paymentApp,
                extras = mapOf("amount" to m.groupValues[1])
            )
        })
        list.add(PatternMapping(
            Regex("""(?:pay|send|transfer)\s+(.+?)\s+(?:rs\.?|₹|inr)?\s*(\d+)\s+(?:via|using|on|through)\s+(?:upi|gpay|phonepe|paytm|bhim)"""),
            CommandType.UPI_PAYMENT
        ) { m, lower ->
            val paymentApp = extractPaymentApp(lower)
            ParsedCommand(
                type = CommandType.UPI_PAYMENT,
                target = cleanContactName(m.groupValues[1]),
                content = m.groupValues[2],
                appPackage = paymentApp,
                extras = mapOf("amount" to m.groupValues[2])
            )
        })
        list.add(PatternMapping(
            Regex("""(?:pay|send money|transfer)\s+(.+?)\s+(?:via|using|on|through)\s+upi"""),
            CommandType.UPI_PAYMENT
        ) { m, _ ->
            ParsedCommand(type = CommandType.UPI_PAYMENT, target = cleanContactName(m.groupValues[1]))
        })

        // --- EMAIL ---
        list.add(PatternMapping(
            Regex("""(?:send|write|compose|draft)\s+(?:an?\s+)?email\s+to\s+(.+?)\s+(?:about|subject|regarding|with subject)\s+(.+?)\s+(?:saying|body|message|content|with message)\s+(.+)"""),
            CommandType.SEND_EMAIL
        ) { m, _ ->
            ParsedCommand(
                type = CommandType.SEND_EMAIL,
                target = cleanContactName(m.groupValues[1]),
                content = m.groupValues[3].trim(),
                extras = mapOf("subject" to m.groupValues[2].trim())
            )
        })
        list.add(PatternMapping(
            Regex("""(?:send|write|compose|draft)\s+(?:an?\s+)?email\s+to\s+(.+?)\s+(?:about|subject|regarding|with subject)\s+(.+)"""),
            CommandType.SEND_EMAIL
        ) { m, _ ->
            ParsedCommand(
                type = CommandType.SEND_EMAIL,
                target = cleanContactName(m.groupValues[1]),
                extras = mapOf("subject" to m.groupValues[2].trim())
            )
        })
        list.add(PatternMapping(
            Regex("""(?:send|write|compose|draft)\s+(?:an?\s+)?email\s+to\s+(.+)"""),
            CommandType.SEND_EMAIL
        ) { m, _ ->
            ParsedCommand(type = CommandType.SEND_EMAIL, target = cleanContactName(m.groupValues[1]))
        })
        list.add(PatternMapping(
            Regex("""(?:email|mail)\s+(.+?)\s+(?:about|subject|regarding)\s+(.+)"""),
            CommandType.SEND_EMAIL
        ) { m, _ ->
            ParsedCommand(
                type = CommandType.SEND_EMAIL,
                target = cleanContactName(m.groupValues[1]),
                extras = mapOf("subject" to m.groupValues[2].trim())
            )
        })

        // --- SCHEDULE MEETING ---
        list.add(PatternMapping(
            Regex("""(?:schedule|create|set up|book)\s+(?:a\s+)?(?:meeting|call|conference)\s+(?:with\s+(.+?)\s+)?(?:at|for|on)\s+(.+)"""),
            CommandType.SCHEDULE_MEETING
        ) { m, _ ->
            ParsedCommand(
                type = CommandType.SCHEDULE_MEETING,
                target = cleanContactName(m.groupValues[1]),
                extras = mapOf("time" to m.groupValues[2].trim())
            )
        })
        list.add(PatternMapping(
            Regex("""(?:join|start|enter)\s+(?:a\s+)?(?:zoom|google meet|teams|webex)\s+(?:meeting|call)"""),
            CommandType.SCHEDULE_MEETING
        ) { m, lower ->
            val platform = extractMeetingPlatform(lower)
            ParsedCommand(
                type = CommandType.SCHEDULE_MEETING,
                appPackage = platform,
                extras = mapOf("action" to "join")
            )
        })
        list.add(PatternMapping(
            Regex("""(?:schedule|create|set up|book)\s+(?:a\s+)?meeting"""),
            CommandType.SCHEDULE_MEETING
        ) { _, _ ->
            ParsedCommand(type = CommandType.SCHEDULE_MEETING)
        })

        // --- PLAY MEDIA ---
        list.add(PatternMapping(
            Regex("""play\s+(.+?)\s+(?:on|using|in|from|with)\s+(youtube|spotify|apple music|amazon music|wynk|gaana|jiosaavn|soundcloud)"""),
            CommandType.PLAY_MEDIA
        ) { m, _ ->
            val app = getMediaAppPackage(m.groupValues[2].trim())
            ParsedCommand(type = CommandType.PLAY_MEDIA, content = m.groupValues[1].trim(), appPackage = app)
        })
        list.add(PatternMapping(
            Regex("""play\s+(?:song|music|track|album|artist)\s+(.+)"""),
            CommandType.PLAY_MEDIA
        ) { m, _ ->
            ParsedCommand(type = CommandType.PLAY_MEDIA, content = m.groupValues[1].trim())
        })
        list.add(PatternMapping(
            Regex("""play\s+(.+?)\s+(?:song|music|video)"""),
            CommandType.PLAY_MEDIA
        ) { m, _ ->
            ParsedCommand(type = CommandType.PLAY_MEDIA, content = m.groupValues[1].trim())
        })
        list.add(PatternMapping(
            Regex("""(?:play|listen to|put on)\s+(.+)"""),
            CommandType.PLAY_MEDIA
        ) { m, _ ->
            ParsedCommand(type = CommandType.PLAY_MEDIA, content = m.groupValues[1].trim())
        })

        // --- WEB SEARCH ---
        list.add(PatternMapping(
            Regex("""(?:search|google|look up|find|browse|search for|google for|look for|search the web for)\s+(.+)"""),
            CommandType.WEB_SEARCH
        ) { m, _ ->
            ParsedCommand(type = CommandType.WEB_SEARCH, content = m.groupValues[1].trim())
        })
        list.add(PatternMapping(
            Regex("""(?:what|who|when|where|why|how|which)\s+(.+)"""),
            CommandType.WEB_SEARCH
        ) { m, lower ->
            ParsedCommand(type = CommandType.WEB_SEARCH, content = lower)
        })

        // --- SETTINGS: WIFI ---
        list.add(PatternMapping(
            Regex("""(?:turn|switch|toggle|enable|disable)\s+(?:on|off)?\s*(?:the\s+)?wi-?fi\s*(?:on|off)?"""),
            CommandType.TOGGLE_WIFI
        ) { _, lower ->
            ParsedCommand(type = CommandType.TOGGLE_WIFI, extras = mapOf("state" to extractToggleState(lower)))
        })
        list.add(PatternMapping(
            Regex("""wi-?fi\s+(?:on|off|enable|disable)"""),
            CommandType.TOGGLE_WIFI
        ) { _, lower ->
            ParsedCommand(type = CommandType.TOGGLE_WIFI, extras = mapOf("state" to extractToggleState(lower)))
        })

        // --- SETTINGS: BLUETOOTH ---
        list.add(PatternMapping(
            Regex("""(?:turn|switch|toggle|enable|disable)\s+(?:on|off)?\s*(?:the\s+)?bluetooth\s*(?:on|off)?"""),
            CommandType.TOGGLE_BLUETOOTH
        ) { _, lower ->
            ParsedCommand(type = CommandType.TOGGLE_BLUETOOTH, extras = mapOf("state" to extractToggleState(lower)))
        })
        list.add(PatternMapping(
            Regex("""bluetooth\s+(?:on|off|enable|disable)"""),
            CommandType.TOGGLE_BLUETOOTH
        ) { _, lower ->
            ParsedCommand(type = CommandType.TOGGLE_BLUETOOTH, extras = mapOf("state" to extractToggleState(lower)))
        })

        // --- SETTINGS: HOTSPOT ---
        list.add(PatternMapping(
            Regex("""(?:turn|switch|toggle|enable|disable)\s+(?:on|off)?\s*(?:the\s+)?(?:hotspot|tethering|mobile hotspot)\s*(?:on|off)?"""),
            CommandType.TOGGLE_HOTSPOT
        ) { _, lower ->
            ParsedCommand(type = CommandType.TOGGLE_HOTSPOT, extras = mapOf("state" to extractToggleState(lower)))
        })

        // --- SETTINGS: GPS/LOCATION ---
        list.add(PatternMapping(
            Regex("""(?:turn|switch|toggle|enable|disable)\s+(?:on|off)?\s*(?:the\s+)?(?:gps|location|location services)\s*(?:on|off)?"""),
            CommandType.TOGGLE_GPS
        ) { _, lower ->
            ParsedCommand(type = CommandType.TOGGLE_GPS, extras = mapOf("state" to extractToggleState(lower)))
        })

        // --- ALARM ---
        list.add(PatternMapping(
            Regex("""(?:set|create|add)\s+(?:an?\s+)?alarm\s+(?:for|at)\s+(\d{1,2})\s*:\s*(\d{2})\s*(am|pm)?"""),
            CommandType.SET_ALARM
        ) { m, _ ->
            val (hour, minute) = parseTime(m.groupValues[1], m.groupValues[2], m.groupValues[3])
            ParsedCommand(
                type = CommandType.SET_ALARM,
                extras = mapOf("hour" to hour.toString(), "minute" to minute.toString())
            )
        })
        list.add(PatternMapping(
            Regex("""(?:set|create|add)\s+(?:an?\s+)?alarm\s+(?:for|at)\s+(\d{1,2})\s*(am|pm)"""),
            CommandType.SET_ALARM
        ) { m, _ ->
            val (hour, minute) = parseTime(m.groupValues[1], "0", m.groupValues[2])
            ParsedCommand(
                type = CommandType.SET_ALARM,
                extras = mapOf("hour" to hour.toString(), "minute" to minute.toString())
            )
        })
        list.add(PatternMapping(
            Regex("""(?:wake|get)\s+me\s+up\s+(?:at|by)\s+(\d{1,2})\s*:?\s*(\d{2})?\s*(am|pm)?"""),
            CommandType.SET_ALARM
        ) { m, _ ->
            val minStr = m.groupValues[2].ifBlank { "0" }
            val amPm = m.groupValues[3]
            val (hour, minute) = parseTime(m.groupValues[1], minStr, amPm)
            ParsedCommand(
                type = CommandType.SET_ALARM,
                extras = mapOf("hour" to hour.toString(), "minute" to minute.toString())
            )
        })
        list.add(PatternMapping(
            Regex("""alarm\s+(\d{1,2})\s*:?\s*(\d{2})?\s*(am|pm)?"""),
            CommandType.SET_ALARM
        ) { m, _ ->
            val minStr = m.groupValues[2].ifBlank { "0" }
            val amPm = m.groupValues[3]
            val (hour, minute) = parseTime(m.groupValues[1], minStr, amPm)
            ParsedCommand(
                type = CommandType.SET_ALARM,
                extras = mapOf("hour" to hour.toString(), "minute" to minute.toString())
            )
        })

        // --- CAMERA ---
        list.add(PatternMapping(
            Regex("""(?:take|capture|snap|click|shoot)\s+(?:a\s+)?(?:photo|picture|selfie|snapshot|pic)"""),
            CommandType.TAKE_PHOTO
        ) { _, lower ->
            val facing = if (lower.contains("selfie") || lower.contains("front")) "front" else "back"
            ParsedCommand(type = CommandType.TAKE_PHOTO, extras = mapOf("facing" to facing))
        })
        list.add(PatternMapping(
            Regex("""(?:open|launch|start)\s+(?:the\s+)?camera"""),
            CommandType.TAKE_PHOTO
        ) { _, _ ->
            ParsedCommand(type = CommandType.TAKE_PHOTO, extras = mapOf("facing" to "back"))
        })
        list.add(PatternMapping(
            Regex("""(?:record|start|capture|shoot|take)\s+(?:a\s+)?video"""),
            CommandType.RECORD_VIDEO
        ) { _, _ ->
            ParsedCommand(type = CommandType.RECORD_VIDEO)
        })

        // --- NAVIGATION ---
        list.add(PatternMapping(
            Regex("""(?:navigate|take me|drive|go)\s+to\s+(.+)"""),
            CommandType.NAVIGATE
        ) { m, _ ->
            ParsedCommand(type = CommandType.NAVIGATE, target = m.groupValues[1].trim())
        })
        list.add(PatternMapping(
            Regex("""(?:directions|direction|route)\s+(?:to|for)\s+(.+)"""),
            CommandType.NAVIGATE
        ) { m, _ ->
            ParsedCommand(type = CommandType.NAVIGATE, target = m.groupValues[1].trim())
        })
        list.add(PatternMapping(
            Regex("""(?:how\s+(?:do\s+i\s+)?(?:get|go)|find\s+(?:the\s+)?(?:way|route))\s+to\s+(.+)"""),
            CommandType.NAVIGATE
        ) { m, _ ->
            ParsedCommand(type = CommandType.NAVIGATE, target = m.groupValues[1].trim())
        })
        list.add(PatternMapping(
            Regex("""(?:map|maps|show)\s+(.+)"""),
            CommandType.NAVIGATE
        ) { m, _ ->
            ParsedCommand(type = CommandType.NAVIGATE, target = m.groupValues[1].trim())
        })

        // --- SHARE LOCATION ---
        list.add(PatternMapping(
            Regex("""(?:share|send)\s+(?:my\s+)?(?:location|gps|coordinates|position)\s*(?:to|with)?\s*(.*)"""),
            CommandType.SHARE_LOCATION
        ) { m, _ ->
            ParsedCommand(type = CommandType.SHARE_LOCATION, target = m.groupValues[1].trim())
        })

        // --- IOT CONTROL ---
        list.add(PatternMapping(
            Regex("""(?:turn|switch)\s+(on|off)\s+(?:the\s+)?(?:smart\s+)?(.+?)(?:\s+(?:light|bulb|plug|switch|fan|ac|tv|device))?$"""),
            CommandType.IOT_CONTROL
        ) { m, _ ->
            ParsedCommand(
                type = CommandType.IOT_CONTROL,
                target = m.groupValues[2].trim(),
                extras = mapOf("state" to m.groupValues[1])
            )
        })
        list.add(PatternMapping(
            Regex("""(?:control|adjust|change|set)\s+(?:the\s+)?(?:smart\s+)?(.+?)(?:\s+to\s+(.+))?$"""),
            CommandType.IOT_CONTROL
        ) { m, _ ->
            val extras = if (m.groupValues[2].isNotBlank()) mapOf("value" to m.groupValues[2].trim()) else emptyMap()
            ParsedCommand(type = CommandType.IOT_CONTROL, target = m.groupValues[1].trim(), extras = extras)
        })

        // --- SCHEDULE TASK / REMINDER ---
        list.add(PatternMapping(
            Regex("""(?:remind|reminder)\s+(?:me\s+)?(?:to\s+)?(.+?)\s+(?:at|on|by|in|for|tomorrow|today)\s+(.+)"""),
            CommandType.SCHEDULE_TASK
        ) { m, _ ->
            ParsedCommand(
                type = CommandType.SCHEDULE_TASK,
                content = m.groupValues[1].trim(),
                extras = mapOf("time" to m.groupValues[2].trim())
            )
        })
        list.add(PatternMapping(
            Regex("""(?:schedule|plan)\s+(.+?)\s+(?:for|at|on)\s+(.+)"""),
            CommandType.SCHEDULE_TASK
        ) { m, _ ->
            ParsedCommand(
                type = CommandType.SCHEDULE_TASK,
                content = m.groupValues[1].trim(),
                extras = mapOf("time" to m.groupValues[2].trim())
            )
        })
        list.add(PatternMapping(
            Regex("""(?:remind|reminder)\s+(?:me\s+)?(?:to\s+)?(.+)"""),
            CommandType.SCHEDULE_TASK
        ) { m, _ ->
            ParsedCommand(type = CommandType.SCHEDULE_TASK, content = m.groupValues[1].trim())
        })

        // --- READ NEWS ---
        list.add(PatternMapping(
            Regex("""(?:read|get|show|tell me|what'?s)\s+(?:the\s+)?(?:latest\s+)?(?:news|headlines)(?:\s+(?:about|on|for)\s+(.+))?"""),
            CommandType.READ_NEWS
        ) { m, _ ->
            ParsedCommand(type = CommandType.READ_NEWS, content = m.groupValues[1].trim())
        })

        // --- SEND FILE ---
        list.add(PatternMapping(
            Regex("""(?:send|share|transfer)\s+(?:a\s+)?(?:file|document|photo|image|video|pdf)\s+(?:to\s+)?(.+)"""),
            CommandType.SEND_FILE
        ) { m, _ ->
            ParsedCommand(type = CommandType.SEND_FILE, target = m.groupValues[1].trim())
        })

        // --- OPEN APP (must be near the end to avoid swallowing other patterns) ---
        list.add(PatternMapping(
            Regex("""(?:open|launch|start|run|go to)\s+(?:the\s+)?(.+?)(?:\s+app)?$"""),
            CommandType.OPEN_APP
        ) { m, _ ->
            val appName = m.groupValues[1].trim()
            ParsedCommand(
                type = CommandType.OPEN_APP,
                target = appName,
                appPackage = guessAppPackage(appName)
            )
        })

        return list
    }

    // ---- Fallback parsers ----

    private fun parseCallFallback(lower: String): ParsedCommand {
        val name = lower
            .replace(Regex("""^(?:please\s+)?(?:can you\s+)?(?:call|phone|dial|ring)\s+"""), "")
            .replace(Regex("""\s+(?:please|now|for me)$"""), "")
            .trim()
        return ParsedCommand(type = CommandType.CALL, target = cleanContactName(name))
    }

    private fun parseSmsFallback(lower: String): ParsedCommand {
        val name = lower
            .replace(Regex("""^(?:please\s+)?(?:send\s+(?:a\s+)?)?(?:message|text|sms)\s+(?:to\s+)?"""), "")
            .replace(Regex("""\s+(?:please|now|for me)$"""), "")
            .trim()
        return ParsedCommand(type = CommandType.SMS, target = cleanContactName(name))
    }

    private fun parseOpenAppFallback(lower: String): ParsedCommand {
        val name = lower
            .replace(Regex("""^(?:please\s+)?(?:can you\s+)?(?:open|launch|start|run)\s+(?:the\s+)?"""), "")
            .replace(Regex("""\s+(?:app|application|please|now|for me)$"""), "")
            .trim()
        return ParsedCommand(type = CommandType.OPEN_APP, target = name, appPackage = guessAppPackage(name))
    }

    // ---- Helper functions ----

    private fun cleanContactName(raw: String): String {
        return raw
            .replace(Regex("""^(?:to|for|with|a|the|my)\s+"""), "")
            .replace(Regex("""\s+(?:please|now|for me|right now)$"""), "")
            .trim()
    }

    private fun matchesKeywords(input: String, keywords: List<String>): Boolean {
        return keywords.any { keyword ->
            input.contains(Regex("""\b$keyword\b"""))
        }
    }

    private fun extractToggleState(input: String): String {
        return when {
            input.contains("turn on") || input.contains("switch on") ||
            input.contains("enable") -> "on"
            input.contains("turn off") || input.contains("switch off") ||
            input.contains("disable") -> "off"
            input.endsWith(" on") -> "on"
            input.endsWith(" off") -> "off"
            else -> "toggle"
        }
    }

    private fun parseTime(hourStr: String, minuteStr: String, amPm: String): Pair<Int, Int> {
        var hour = hourStr.toIntOrNull() ?: 0
        val minute = minuteStr.toIntOrNull() ?: 0
        val period = amPm.lowercase()

        if (period == "pm" && hour < 12) hour += 12
        if (period == "am" && hour == 12) hour = 0

        return Pair(hour, minute)
    }

    private fun extractMessagingApp(input: String): String {
        return when {
            input.contains("whatsapp") -> "com.whatsapp"
            input.contains("telegram") -> "org.telegram.messenger"
            input.contains("signal") -> "org.thoughtcrime.securesms"
            input.contains("viber") -> "com.viber.voip"
            else -> ""
        }
    }

    private fun getMessagingAppPackage(name: String): String {
        return when (name.lowercase().trim()) {
            "whatsapp" -> "com.whatsapp"
            "telegram" -> "org.telegram.messenger"
            "signal" -> "org.thoughtcrime.securesms"
            "viber" -> "com.viber.voip"
            else -> ""
        }
    }

    private fun extractPaymentApp(input: String): String {
        return when {
            input.contains("gpay") || input.contains("google pay") -> "com.google.android.apps.nbu.paisa.user"
            input.contains("phonepe") -> "com.phonepe.app"
            input.contains("paytm") -> "net.one97.paytm"
            input.contains("bhim") -> "in.org.npci.upiapp"
            else -> "com.google.android.apps.nbu.paisa.user"
        }
    }

    private fun getMediaAppPackage(name: String): String {
        return when (name.lowercase().trim()) {
            "youtube" -> "com.google.android.youtube"
            "spotify" -> "com.spotify.music"
            "apple music" -> "com.apple.android.music"
            "amazon music" -> "com.amazon.mp3"
            "wynk" -> "com.bsbportal.music"
            "gaana" -> "com.gaana"
            "jiosaavn" -> "com.jio.media.jiobeats"
            "soundcloud" -> "com.soundcloud.android"
            else -> ""
        }
    }

    private fun extractMeetingPlatform(input: String): String {
        return when {
            input.contains("zoom") -> "us.zoom.videomeetings"
            input.contains("google meet") || input.contains("gmeet") -> "com.google.android.apps.meetings"
            input.contains("teams") || input.contains("microsoft teams") -> "com.microsoft.teams"
            input.contains("webex") -> "com.cisco.webex.meetings"
            else -> ""
        }
    }

    private fun guessAppPackage(appName: String): String {
        return when (appName.lowercase().trim()) {
            "youtube" -> "com.google.android.youtube"
            "whatsapp" -> "com.whatsapp"
            "instagram" -> "com.instagram.android"
            "facebook" -> "com.facebook.katana"
            "twitter", "x" -> "com.twitter.android"
            "spotify" -> "com.spotify.music"
            "telegram" -> "org.telegram.messenger"
            "snapchat" -> "com.snapchat.android"
            "tiktok" -> "com.zhiliaoapp.musically"
            "netflix" -> "com.netflix.mediaclient"
            "chrome" -> "com.android.chrome"
            "gmail" -> "com.google.android.gm"
            "google maps", "maps" -> "com.google.android.apps.maps"
            "camera" -> "com.android.camera"
            "calendar" -> "com.google.android.calendar"
            "calculator" -> "com.google.android.calculator"
            "clock" -> "com.google.android.deskclock"
            "settings" -> "com.android.settings"
            "phone" -> "com.android.dialer"
            "contacts" -> "com.android.contacts"
            "files", "file manager" -> "com.google.android.apps.nbu.files"
            "notes", "keep" -> "com.google.android.keep"
            "google pay", "gpay" -> "com.google.android.apps.nbu.paisa.user"
            "phonepe" -> "com.phonepe.app"
            "paytm" -> "net.one97.paytm"
            "amazon" -> "in.amazon.mShop.android.shopping"
            "flipkart" -> "com.flipkart.android"
            "uber" -> "com.ubercab"
            "ola" -> "com.olacabs.customer"
            "zomato" -> "com.application.zomato"
            "swiggy" -> "in.swiggy.android"
            "linkedin" -> "com.linkedin.android"
            "reddit" -> "com.reddit.frontpage"
            "discord" -> "com.discord"
            "slack" -> "com.Slack"
            "zoom" -> "us.zoom.videomeetings"
            "teams" -> "com.microsoft.teams"
            "drive", "google drive" -> "com.google.android.apps.docs"
            "photos", "google photos" -> "com.google.android.apps.photos"
            "play store" -> "com.android.vending"
            else -> ""
        }
    }
}
