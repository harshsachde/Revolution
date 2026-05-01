package com.revolution.ai.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revolution.ai.data.preferences.UserPreferences
import com.revolution.ai.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val wakeWord by viewModel.wakeWord.collectAsState()
    val aiName by viewModel.aiName.collectAsState()
    val appName by viewModel.appName.collectAsState()
    val voiceId by viewModel.voiceId.collectAsState()
    val voicePitch by viewModel.voicePitch.collectAsState()
    val voiceSpeed by viewModel.voiceSpeed.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isAlwaysListening by viewModel.isAlwaysListening.collectAsState()
    val interactionMode by viewModel.interactionMode.collectAsState()
    val logEmail by viewModel.logEmail.collectAsState()
    val dailySummaryEnabled by viewModel.dailySummaryEnabled.collectAsState()
    val isWorkMode by viewModel.isWorkMode.collectAsState()
    val cloudSync by viewModel.cloudSync.collectAsState()
    val workWhenLocked by viewModel.workWhenLocked.collectAsState()
    val urgencyEnabled by viewModel.urgencyEnabled.collectAsState()

    var editableWakeWord by remember(wakeWord) { mutableStateOf(wakeWord) }
    var editableAiName by remember(aiName) { mutableStateOf(aiName) }
    var editableAppName by remember(appName) { mutableStateOf(appName) }
    var editableLogEmail by remember(logEmail) { mutableStateOf(logEmail) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // AI Personality Section
            SettingsSectionHeader(
                icon = Icons.Outlined.SmartToy,
                title = "AI Personality"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = editableWakeWord,
                        onValueChange = { editableWakeWord = it },
                        label = { Text("Wake Word") },
                        supportingText = { Text("Say this word to activate the assistant") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            if (editableWakeWord != wakeWord) {
                                IconButton(onClick = {
                                    viewModel.updatePreference(UserPreferences.WAKE_WORD, editableWakeWord)
                                }) {
                                    Icon(Icons.Filled.Check, contentDescription = "Save")
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editableAiName,
                        onValueChange = { editableAiName = it },
                        label = { Text("AI Name") },
                        supportingText = { Text("Your assistant's name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            if (editableAiName != aiName) {
                                IconButton(onClick = {
                                    viewModel.updatePreference(UserPreferences.AI_NAME, editableAiName)
                                }) {
                                    Icon(Icons.Filled.Check, contentDescription = "Save")
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editableAppName,
                        onValueChange = { editableAppName = it },
                        label = { Text("App Name") },
                        supportingText = { Text("Displayed name of this app") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            if (editableAppName != appName) {
                                IconButton(onClick = {
                                    viewModel.updatePreference(UserPreferences.APP_NAME, editableAppName)
                                }) {
                                    Icon(Icons.Filled.Check, contentDescription = "Save")
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Voice Settings Section
            SettingsSectionHeader(
                icon = Icons.Outlined.RecordVoiceOver,
                title = "Voice Settings"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Voice Selection",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        VoiceOptionCard(
                            label = "Male",
                            icon = Icons.Filled.Male,
                            isSelected = voiceId == "male",
                            onClick = {
                                viewModel.updatePreference(UserPreferences.VOICE_ID, "male")
                            },
                            modifier = Modifier.weight(1f)
                        )

                        VoiceOptionCard(
                            label = "Female",
                            icon = Icons.Filled.Female,
                            isSelected = voiceId == "female" || voiceId == "default",
                            onClick = {
                                viewModel.updatePreference(UserPreferences.VOICE_ID, "female")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Pitch: ${"%.1f".format(voicePitch)}x",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Slider(
                        value = voicePitch,
                        onValueChange = {
                            viewModel.updatePreference(
                                UserPreferences.VOICE_PITCH,
                                "%.1f".format(it).toFloat()
                            )
                        },
                        valueRange = 0.5f..2.0f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Speed: ${"%.1f".format(voiceSpeed)}x",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Slider(
                        value = voiceSpeed,
                        onValueChange = {
                            viewModel.updatePreference(
                                UserPreferences.VOICE_SPEED,
                                "%.1f".format(it).toFloat()
                            )
                        },
                        valueRange = 0.5f..2.0f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { /* Preview voice with current settings */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Preview Voice")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Interaction Mode Section
            SettingsSectionHeader(
                icon = Icons.Outlined.TouchApp,
                title = "Interaction Mode"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InteractionModeOption(
                        label = "Voice",
                        description = "Control with voice commands",
                        icon = Icons.Filled.Mic,
                        isSelected = interactionMode == "VOICE",
                        onClick = {
                            viewModel.updatePreference(UserPreferences.INTERACTION_MODE, "VOICE")
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    InteractionModeOption(
                        label = "Notification",
                        description = "Respond through notifications",
                        icon = Icons.Filled.Notifications,
                        isSelected = interactionMode == "NOTIFICATION",
                        onClick = {
                            viewModel.updatePreference(UserPreferences.INTERACTION_MODE, "NOTIFICATION")
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    InteractionModeOption(
                        label = "Both",
                        description = "Voice and notification combined",
                        icon = Icons.Filled.Tune,
                        isSelected = interactionMode == "BOTH",
                        onClick = {
                            viewModel.updatePreference(UserPreferences.INTERACTION_MODE, "BOTH")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // General Settings Section
            SettingsSectionHeader(
                icon = Icons.Outlined.Settings,
                title = "General"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    SettingsToggleItem(
                        icon = Icons.Filled.HearingDisabled,
                        title = "Always Listening",
                        description = "Keep microphone active for wake word",
                        isChecked = isAlwaysListening,
                        onCheckedChange = {
                            viewModel.updatePreference(UserPreferences.ALWAYS_LISTENING, it)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    SettingsToggleItem(
                        icon = Icons.Filled.DarkMode,
                        title = "Dark Theme",
                        description = "Use dark color scheme",
                        isChecked = isDarkTheme,
                        onCheckedChange = {
                            viewModel.updatePreference(UserPreferences.DARK_THEME, it)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    SettingsToggleItem(
                        icon = Icons.Filled.Work,
                        title = "Work Mode",
                        description = "Filter personal content during work hours",
                        isChecked = isWorkMode,
                        onCheckedChange = {
                            viewModel.updatePreference(UserPreferences.WORK_MODE, it)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    SettingsToggleItem(
                        icon = Icons.Filled.CloudSync,
                        title = "Cloud Sync",
                        description = "Sync settings and data across devices",
                        isChecked = cloudSync,
                        onCheckedChange = {
                            viewModel.updatePreference(UserPreferences.CLOUD_SYNC, it)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    SettingsToggleItem(
                        icon = Icons.Filled.Lock,
                        title = "Work When Locked",
                        description = "Allow assistant when device is locked",
                        isChecked = workWhenLocked,
                        onCheckedChange = {
                            viewModel.updatePreference(UserPreferences.WORK_WHEN_LOCKED, it)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )

                    SettingsToggleItem(
                        icon = Icons.Filled.PriorityHigh,
                        title = "Urgency Detection",
                        description = "Detect and prioritize urgent messages",
                        isChecked = urgencyEnabled,
                        onCheckedChange = {
                            viewModel.updatePreference(UserPreferences.URGENCY_ENABLED, it)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logs & Reports Section
            SettingsSectionHeader(
                icon = Icons.Outlined.Analytics,
                title = "Logs & Reports"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = editableLogEmail,
                        onValueChange = { editableLogEmail = it },
                        label = { Text("Log Email") },
                        supportingText = { Text("Email address for log exports") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(Icons.Filled.Email, contentDescription = null)
                        },
                        trailingIcon = {
                            if (editableLogEmail != logEmail) {
                                IconButton(onClick = {
                                    viewModel.updatePreference(UserPreferences.LOG_EMAIL, editableLogEmail)
                                }) {
                                    Icon(Icons.Filled.Check, contentDescription = "Save")
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsToggleRow(
                        title = "Daily Summary",
                        description = "Receive daily activity summary",
                        isChecked = dailySummaryEnabled,
                        onCheckedChange = {
                            viewModel.updatePreference(UserPreferences.DAILY_SUMMARY_ENABLED, it)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Appearance Section
            SettingsSectionHeader(
                icon = Icons.Outlined.Palette,
                title = "Appearance"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { /* Open icon picker */ }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Custom App Icon",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Choose a custom launcher icon",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            SettingsSectionHeader(
                icon = Icons.Outlined.Info,
                title = "About"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.SmartToy,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Revolution AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Your intelligent voice assistant",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    icon: ImageVector,
    title: String
) {
    Row(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun VoiceOptionCard(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InteractionModeOption(
    label: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isChecked)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!isChecked) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
