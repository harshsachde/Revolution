package com.revolution.ai.ui.screens.guide

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class GuideSection(
    val title: String,
    val icon: ImageVector,
    val items: List<GuideItem>
)

data class GuideItem(
    val title: String,
    val description: String,
    val tip: String? = null,
    val example: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen() {
    val sections = remember { buildGuideSections() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "User Guide",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Welcome to Revolution",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Revolution is your intelligent voice assistant that learns from your habits and automates your daily tasks. This guide covers everything you need to know to get the most out of it.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(sections) { section ->
                ExpandableGuideSection(section = section)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Pro tip: The more you use Revolution, the smarter it gets. It learns your patterns and preferences over time to provide increasingly personalized assistance.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ExpandableGuideSection(section: GuideSection) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${section.items.size} topic${if (section.items.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    section.items.forEachIndexed { index, item ->
                        GuideItemCard(item = item)
                        if (index < section.items.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideItemCard(item: GuideItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (item.example != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Filled.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.example,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            if (item.tip != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.tip,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun buildGuideSections(): List<GuideSection> = listOf(
    GuideSection(
        title = "Getting Started",
        icon = Icons.Outlined.RocketLaunch,
        items = listOf(
            GuideItem(
                title = "Activating the Assistant",
                description = "Say your wake word (default: \"Nikhil\") or tap the microphone button on the home screen to start a voice command. You can also type commands in the text field.",
                tip = "You can change the wake word in Settings > AI Personality.",
                example = "\"Nikhil, what's the weather today?\""
            ),
            GuideItem(
                title = "Understanding the AI Orb",
                description = "The animated orb on the home screen shows the assistant's current state. It pulses slowly when idle, faster when listening, and rapidly when processing. The color changes to indicate different states.",
                tip = "Purple = idle, Cyan = listening, Teal = processing, Red = error."
            ),
            GuideItem(
                title = "Quick Actions",
                description = "Use the quick action chips below the orb for common tasks like making calls, sending messages, searching the web, adjusting settings, or setting alarms.",
                example = "Tap the \"Call\" chip to quickly start a phone call."
            )
        )
    ),
    GuideSection(
        title = "Voice Commands",
        icon = Icons.Outlined.RecordVoiceOver,
        items = listOf(
            GuideItem(
                title = "Making Calls",
                description = "Ask Revolution to call any of your contacts by name. It will find the matching contact and initiate the call.",
                example = "\"Call Mom\" or \"Dial John's work number\""
            ),
            GuideItem(
                title = "Sending Messages",
                description = "Dictate text messages, WhatsApp messages, or emails hands-free. Specify the recipient and message content.",
                example = "\"Send a message to Sarah saying I'll be 10 minutes late\""
            ),
            GuideItem(
                title = "Setting Alarms & Reminders",
                description = "Create alarms, timers, and reminders with natural language. You can set one-time or recurring alarms.",
                example = "\"Set an alarm for 7 AM tomorrow\" or \"Remind me to buy groceries at 5 PM\""
            ),
            GuideItem(
                title = "Searching the Web",
                description = "Ask any question and Revolution will search for the answer. Results are summarized and read back to you.",
                example = "\"Search for best Italian restaurants nearby\""
            ),
            GuideItem(
                title = "App Control",
                description = "Open apps, toggle settings, adjust volume, control media playback, and more through voice commands.",
                example = "\"Open YouTube\" or \"Turn on Bluetooth\" or \"Set volume to 50%\""
            )
        )
    ),
    GuideSection(
        title = "Smart Learning",
        icon = Icons.Outlined.Psychology,
        items = listOf(
            GuideItem(
                title = "Adaptive Behavior",
                description = "Revolution learns your frequently used commands and patterns. Over time, it anticipates your needs and provides faster, more relevant responses.",
                tip = "The more consistently you use voice commands, the better it adapts to your speech patterns."
            ),
            GuideItem(
                title = "Work vs Personal Mode",
                description = "Enable Work Mode to filter personal notifications and focus on work-related tasks during business hours. The assistant adapts its responses accordingly.",
                tip = "Set work hours in Settings to automatically switch modes."
            ),
            GuideItem(
                title = "Learned Behaviors",
                description = "The assistant records patterns like \"every Monday you check your calendar\" and proactively suggests these actions. You can review and manage learned behaviors."
            )
        )
    ),
    GuideSection(
        title = "Urgency Detection",
        icon = Icons.Outlined.NotificationImportant,
        items = listOf(
            GuideItem(
                title = "How It Works",
                description = "Revolution monitors notification patterns and detects urgent situations like repeated calls from the same person or messages containing emergency keywords.",
                tip = "Configure sensitivity and rules in Settings > Urgency Detection."
            ),
            GuideItem(
                title = "Emergency Contacts",
                description = "Calls and messages from emergency contacts are always prioritized, even in Do Not Disturb mode. Add trusted contacts to your emergency list.",
                example = "Add family members as emergency contacts for priority alerts."
            ),
            GuideItem(
                title = "Custom Urgency Rules",
                description = "Create custom rules based on contact names, app packages, keywords, and frequency thresholds within specified time windows."
            )
        )
    ),
    GuideSection(
        title = "Privacy & Permissions",
        icon = Icons.Outlined.Shield,
        items = listOf(
            GuideItem(
                title = "App Access Control",
                description = "Control which installed apps the assistant can interact with. Toggle individual app permissions on the Permissions screen.",
                tip = "Disable access for sensitive apps to keep your data private."
            ),
            GuideItem(
                title = "Blocked Keywords",
                description = "Add keywords to the blocked list to prevent the assistant from processing commands containing those terms. Useful for filtering sensitive topics.",
                example = "Block keywords like \"password\" or \"banking\" for extra security."
            ),
            GuideItem(
                title = "Data & Logs",
                description = "All actions are logged locally for your review. You can export, email, or delete logs at any time from the Logs screen. No data is sent to external servers unless Cloud Sync is enabled."
            )
        )
    ),
    GuideSection(
        title = "Customization",
        icon = Icons.Outlined.Tune,
        items = listOf(
            GuideItem(
                title = "Voice Settings",
                description = "Choose between male and female voices, adjust pitch and speaking speed to your preference. Preview changes before saving.",
                tip = "A lower pitch and slightly faster speed often sounds most natural."
            ),
            GuideItem(
                title = "Interaction Modes",
                description = "Choose how the assistant communicates: Voice mode for spoken responses, Notification mode for silent text notifications, or Both for full interaction.",
                tip = "Use Notification mode in quiet environments like meetings."
            ),
            GuideItem(
                title = "Theme & Appearance",
                description = "Switch between dark and light themes. Customize the app icon from the Settings > Appearance section."
            ),
            GuideItem(
                title = "Always Listening Mode",
                description = "When enabled, the assistant continuously listens for the wake word without needing to tap the microphone. This uses additional battery.",
                tip = "Disable this when not needed to conserve battery life."
            )
        )
    ),
    GuideSection(
        title = "Scheduled Tasks",
        icon = Icons.Outlined.Schedule,
        items = listOf(
            GuideItem(
                title = "Creating Scheduled Tasks",
                description = "Schedule commands to run at specific times. Supports one-time and recurring schedules with timezone awareness.",
                example = "\"Every weekday at 8 AM, read me my calendar\""
            ),
            GuideItem(
                title = "Managing Tasks",
                description = "View, edit, and delete scheduled tasks. Completed tasks are archived but can be reviewed in the logs section."
            ),
            GuideItem(
                title = "Daily Summary",
                description = "Enable the daily summary feature to receive a recap of all assistant activities, completed tasks, and key events at the end of each day.",
                tip = "Set a preferred time for your daily summary email in Settings."
            )
        )
    ),
    GuideSection(
        title = "Troubleshooting",
        icon = Icons.Outlined.Build,
        items = listOf(
            GuideItem(
                title = "Assistant Not Responding",
                description = "If the assistant isn't responding to voice commands, check that the microphone permission is granted, the device isn't muted, and Always Listening mode is enabled if you're using the wake word.",
                tip = "Try toggling the microphone button to reset the listening state."
            ),
            GuideItem(
                title = "Incorrect Command Recognition",
                description = "Speak clearly and at a moderate pace. Background noise can interfere with recognition. The assistant improves over time as it learns your voice.",
                tip = "Use the text input as a fallback for noisy environments."
            ),
            GuideItem(
                title = "Battery Usage",
                description = "Always Listening mode and frequent voice commands can increase battery usage. Disable background listening and reduce command frequency if battery life is a concern."
            ),
            GuideItem(
                title = "Resetting the Assistant",
                description = "To reset all settings to defaults, clear the app data from your device settings. Note that this will erase all learned behaviors and logs."
            )
        )
    )
)
