package com.revolution.ai.engine.learning

import com.revolution.ai.data.model.LearnedBehavior
import com.revolution.ai.data.repository.AppRepository
import java.util.Calendar

class LearningEngine(private val repository: AppRepository) {

    private val timeSlots = mapOf(
        "early_morning" to (5..7),
        "morning" to (8..11),
        "afternoon" to (12..16),
        "evening" to (17..20),
        "night" to (21..23),
        "late_night" to (0..4)
    )

    suspend fun learn(command: String, action: String, tag: String = "general") {
        val normalizedCommand = normalizeCommand(command)
        val timeSlot = getCurrentTimeSlot()
        val pattern = buildPattern(normalizedCommand, timeSlot)

        val existing = repository.findMatchingBehaviors(normalizedCommand)
        val match = existing.find { behavior ->
            behavior.pattern == pattern || behavior.response == action
        }

        if (match != null) {
            repository.updateBehavior(
                match.copy(
                    frequency = match.frequency + 1,
                    lastUsed = System.currentTimeMillis(),
                    tag = if (tag != "general") tag else match.tag
                )
            )
        } else {
            repository.insertBehavior(
                LearnedBehavior(
                    pattern = pattern,
                    response = action,
                    frequency = 1,
                    lastUsed = System.currentTimeMillis(),
                    tag = inferTag(command, action, tag)
                )
            )
        }
    }

    suspend fun suggest(context: String): List<LearnedBehavior> {
        val timeSlot = getCurrentTimeSlot()
        val normalizedContext = normalizeCommand(context)

        val behaviors = repository.findMatchingBehaviors(normalizedContext)

        val timeMatched = behaviors.filter { it.pattern.contains(timeSlot) }
        val highFrequency = behaviors.filter { it.frequency >= 3 }

        val combined = (timeMatched + highFrequency + behaviors)
            .distinctBy { it.id }
            .sortedByDescending { scoreBehavior(it, timeSlot) }
            .take(5)

        return combined
    }

    suspend fun suggestByTimeOfDay(): List<LearnedBehavior> {
        val timeSlot = getCurrentTimeSlot()
        val behaviors = repository.findMatchingBehaviors(timeSlot)
        return behaviors
            .sortedByDescending { it.frequency }
            .take(5)
    }

    suspend fun suggestByTag(tag: String): List<LearnedBehavior> {
        val behaviors = repository.findMatchingBehaviors(tag)
        return behaviors
            .filter { it.tag.equals(tag, ignoreCase = true) }
            .sortedByDescending { it.frequency }
            .take(5)
    }

    private fun normalizeCommand(command: String): String {
        return command
            .lowercase()
            .trim()
            .replace(Regex("""\s+"""), " ")
            .replace(Regex("""[^\w\s]"""), "")
    }

    private fun getCurrentTimeSlot(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return timeSlots.entries.find { hour in it.value }?.key ?: "unknown"
    }

    private fun buildPattern(normalizedCommand: String, timeSlot: String): String {
        val keywords = extractKeywords(normalizedCommand)
        return "${timeSlot}:${keywords.joinToString(",")}"
    }

    private fun extractKeywords(input: String): List<String> {
        val stopWords = setOf(
            "a", "an", "the", "is", "are", "was", "were", "be", "been",
            "being", "have", "has", "had", "do", "does", "did", "will",
            "would", "could", "should", "may", "might", "shall", "can",
            "to", "of", "in", "for", "on", "with", "at", "by", "from",
            "it", "this", "that", "these", "those", "i", "me", "my",
            "we", "our", "you", "your", "he", "she", "they", "them",
            "and", "or", "but", "not", "no", "so", "if", "then",
            "please", "just", "also", "very", "really"
        )

        return input.split(" ")
            .filter { it.length > 1 && it !in stopWords }
            .take(5)
    }

    private fun inferTag(command: String, action: String, providedTag: String): String {
        if (providedTag != "general") return providedTag

        val commandLower = command.lowercase()
        val workIndicators = listOf(
            "email", "meeting", "schedule", "calendar", "slack",
            "teams", "zoom", "work", "office", "presentation",
            "document", "spreadsheet", "deadline", "project",
            "client", "report", "conference"
        )
        val personalIndicators = listOf(
            "music", "play", "game", "youtube", "netflix",
            "instagram", "facebook", "twitter", "whatsapp",
            "photo", "selfie", "food", "order", "movie",
            "song", "fun", "friend", "family"
        )

        val workScore = workIndicators.count { commandLower.contains(it) }
        val personalScore = personalIndicators.count { commandLower.contains(it) }

        return when {
            workScore > personalScore -> "Work"
            personalScore > workScore -> "Personal"
            else -> "general"
        }
    }

    private fun scoreBehavior(behavior: LearnedBehavior, currentTimeSlot: String): Double {
        var score = 0.0

        score += behavior.frequency * 2.0

        if (behavior.pattern.startsWith(currentTimeSlot)) {
            score += 10.0
        }

        val ageMs = System.currentTimeMillis() - behavior.lastUsed
        val ageDays = ageMs / (1000.0 * 60 * 60 * 24)
        val recencyBonus = maxOf(0.0, 10.0 - ageDays)
        score += recencyBonus

        return score
    }
}
