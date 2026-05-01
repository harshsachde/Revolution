package com.revolution.ai.engine.urgency

import com.revolution.ai.data.model.EmergencyContact
import com.revolution.ai.data.model.UrgencyRule
import java.util.Calendar

data class UrgencyEvent(
    val sender: String,
    val message: String,
    val app: String,
    val timestamp: Long
)

enum class UrgencyLevel { LOW, MEDIUM, HIGH, CRITICAL }

data class UrgencyAssessment(
    val level: UrgencyLevel,
    val reason: String,
    val shouldAlert: Boolean,
    val shouldCallName: Boolean
)

class UrgencyEngine {

    private val recentEvents = mutableListOf<UrgencyEvent>()

    private val urgentKeywords = listOf(
        "urgent", "emergency", "help", "sos", "asap", "critical",
        "important", "immediately", "right now", "dying", "accident",
        "hospital", "police", "fire", "ambulance", "911", "112",
        "please help", "come now", "hurry", "danger", "serious",
        "call me", "pick up", "answer", "need you", "life or death"
    )

    private val highPriorityKeywords = listOf(
        "important", "need", "please", "asap", "quick", "fast",
        "respond", "reply", "waiting", "where are you", "call me back"
    )

    fun assess(
        event: UrgencyEvent,
        rules: List<UrgencyRule>,
        emergencyContacts: List<EmergencyContact>
    ): UrgencyAssessment {
        addEvent(event)

        val scores = mutableListOf<ScoredReason>()

        scores.add(assessEmergencyContact(event, emergencyContacts))
        scores.add(assessKeywords(event))
        scores.add(assessFrequency(event, rules))
        scores.add(assessCrossAppAttempts(event))
        scores.add(assessTimeContext(event))
        scores.add(assessCustomRules(event, rules))

        val maxScore = scores.maxByOrNull { it.score } ?: ScoredReason(0, "No urgency detected")
        val totalScore = scores.sumOf { it.score }

        val level = when {
            totalScore >= 80 || maxScore.score >= 50 -> UrgencyLevel.CRITICAL
            totalScore >= 50 || maxScore.score >= 30 -> UrgencyLevel.HIGH
            totalScore >= 25 || maxScore.score >= 15 -> UrgencyLevel.MEDIUM
            else -> UrgencyLevel.LOW
        }

        val reasons = scores.filter { it.score > 0 }.sortedByDescending { it.score }
        val primaryReason = reasons.firstOrNull()?.reason ?: "No urgency detected"

        val shouldAlert = level == UrgencyLevel.CRITICAL || level == UrgencyLevel.HIGH
        val shouldCallName = level == UrgencyLevel.CRITICAL

        return UrgencyAssessment(
            level = level,
            reason = primaryReason,
            shouldAlert = shouldAlert,
            shouldCallName = shouldCallName
        )
    }

    fun addEvent(event: UrgencyEvent) {
        recentEvents.add(event)
        pruneOldEvents()
    }

    fun clearOldEvents(olderThan: Long) {
        recentEvents.removeAll { it.timestamp < olderThan }
    }

    fun clearAllEvents() {
        recentEvents.clear()
    }

    private fun pruneOldEvents() {
        val oneHourAgo = System.currentTimeMillis() - 3_600_000L
        recentEvents.removeAll { it.timestamp < oneHourAgo }
    }

    private fun assessEmergencyContact(
        event: UrgencyEvent,
        emergencyContacts: List<EmergencyContact>
    ): ScoredReason {
        val senderLower = event.sender.lowercase()
        val matchedContact = emergencyContacts.find { contact ->
            senderLower.contains(contact.name.lowercase()) ||
            senderLower.contains(contact.phoneNumber)
        }

        return if (matchedContact != null && matchedContact.alwaysAlert) {
            ScoredReason(40, "Message from emergency contact: ${matchedContact.name}")
        } else if (matchedContact != null) {
            ScoredReason(20, "Message from listed contact: ${matchedContact.name}")
        } else {
            ScoredReason(0, "")
        }
    }

    private fun assessKeywords(event: UrgencyEvent): ScoredReason {
        val messageLower = event.message.lowercase()

        val criticalMatches = urgentKeywords.filter { messageLower.contains(it) }
        if (criticalMatches.isNotEmpty()) {
            val score = minOf(criticalMatches.size * 15, 50)
            return ScoredReason(score, "Urgent keywords detected: ${criticalMatches.joinToString(", ")}")
        }

        val highMatches = highPriorityKeywords.filter { messageLower.contains(it) }
        if (highMatches.isNotEmpty()) {
            val score = minOf(highMatches.size * 8, 25)
            return ScoredReason(score, "Priority keywords: ${highMatches.joinToString(", ")}")
        }

        val exclamationCount = messageLower.count { it == '!' }
        val capsRatio = if (event.message.isNotEmpty()) {
            event.message.count { it.isUpperCase() }.toFloat() / event.message.length
        } else 0f

        if (exclamationCount >= 3 || capsRatio > 0.7f) {
            return ScoredReason(10, "Message appears emphatic")
        }

        return ScoredReason(0, "")
    }

    private fun assessFrequency(event: UrgencyEvent, rules: List<UrgencyRule>): ScoredReason {
        val senderLower = event.sender.lowercase()
        val defaultWindow = 5 * 60 * 1000L
        val defaultThreshold = 3

        val matchingRule = rules.find { rule ->
            rule.isEnabled && (
                senderLower.contains(rule.contactName.lowercase()) ||
                rule.contactName.isBlank()
            )
        }

        val windowMs = (matchingRule?.timeWindowMinutes?.toLong() ?: 5) * 60 * 1000L
        val threshold = matchingRule?.threshold ?: defaultThreshold
        val cutoff = event.timestamp - windowMs

        val recentFromSender = recentEvents.count { e ->
            e.sender.lowercase() == senderLower && e.timestamp >= cutoff
        }

        return when {
            recentFromSender >= threshold * 2 -> {
                ScoredReason(40, "$recentFromSender messages from ${event.sender} in ${windowMs / 60000} min")
            }
            recentFromSender >= threshold -> {
                ScoredReason(25, "$recentFromSender messages from ${event.sender} in ${windowMs / 60000} min")
            }
            recentFromSender >= 2 -> {
                ScoredReason(10, "Multiple messages from ${event.sender}")
            }
            else -> ScoredReason(0, "")
        }
    }

    private fun assessCrossAppAttempts(event: UrgencyEvent): ScoredReason {
        val senderLower = event.sender.lowercase()
        val fiveMinAgo = event.timestamp - 5 * 60 * 1000L

        val appsUsed = recentEvents
            .filter { it.sender.lowercase() == senderLower && it.timestamp >= fiveMinAgo }
            .map { it.app }
            .distinct()

        return when {
            appsUsed.size >= 3 -> {
                ScoredReason(35, "${event.sender} contacted via ${appsUsed.size} different apps")
            }
            appsUsed.size >= 2 -> {
                ScoredReason(20, "${event.sender} contacted via ${appsUsed.joinToString(" and ")}")
            }
            else -> ScoredReason(0, "")
        }
    }

    private fun assessTimeContext(event: UrgencyEvent): ScoredReason {
        val calendar = Calendar.getInstance().apply { timeInMillis = event.timestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when {
            hour in 0..5 -> {
                ScoredReason(15, "Message received during late night hours (${hour}:00)")
            }
            hour in 23..23 -> {
                ScoredReason(10, "Message received late at night")
            }
            else -> ScoredReason(0, "")
        }
    }

    private fun assessCustomRules(event: UrgencyEvent, rules: List<UrgencyRule>): ScoredReason {
        val messageLower = event.message.lowercase()
        val senderLower = event.sender.lowercase()

        for (rule in rules) {
            if (!rule.isEnabled) continue

            val contactMatches = rule.contactName.isBlank() ||
                senderLower.contains(rule.contactName.lowercase())

            val appMatches = rule.appPackage.isBlank() ||
                event.app.contains(rule.appPackage)

            val keywordMatches = rule.keyword.isBlank() ||
                messageLower.contains(rule.keyword.lowercase())

            if (contactMatches && appMatches && keywordMatches &&
                (rule.contactName.isNotBlank() || rule.keyword.isNotBlank())) {
                return ScoredReason(30, "Custom urgency rule matched: ${rule.keyword.ifBlank { rule.contactName }}")
            }
        }

        return ScoredReason(0, "")
    }

    private data class ScoredReason(val score: Int, val reason: String)
}
