package com.revolution.ai.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.revolution.ai.data.model.AssistantState
import com.revolution.ai.ui.MainViewModel
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val assistantState by viewModel.assistantState.collectAsState()
    val aiName by viewModel.aiName.collectAsState()
    val lastResponse by viewModel.lastResponse.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val isAlwaysListening by viewModel.isAlwaysListening.collectAsState()

    var textCommand by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = aiName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            AnimatedContent(
                targetState = assistantState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "stateLabel"
            ) { state ->
                Text(
                    text = when (state) {
                        AssistantState.IDLE -> if (isAlwaysListening) "Listening for wake word..." else "Ready to assist"
                        AssistantState.LISTENING -> "Listening..."
                        AssistantState.PROCESSING -> "Processing..."
                        AssistantState.SPEAKING -> "Speaking..."
                        AssistantState.ERROR -> "Something went wrong"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            AiOrbVisualization(
                assistantState = assistantState,
                modifier = Modifier
                    .size(160.dp)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            MicButton(
                assistantState = assistantState,
                onClick = { viewModel.toggleListening() }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            AnimatedVisibility(
                visible = lastResponse.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SmartToy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = lastResponse,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textCommand,
                    onValueChange = { textCommand = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a command...") },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilledIconButton(
                    onClick = {
                        if (textCommand.isNotBlank()) {
                            viewModel.processTextCommand(textCommand)
                            textCommand = ""
                        }
                    },
                    enabled = textCommand.isNotBlank(),
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = "Send command",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuickActionChips(
                onAction = { action -> viewModel.processTextCommand(action) }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        if (recentLogs.isNotEmpty()) {
            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            items(recentLogs.take(5)) { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 3.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (log.wasSuccessful)
                                Icons.Filled.CheckCircle
                            else
                                Icons.Filled.Cancel,
                            contentDescription = null,
                            tint = if (log.wasSuccessful)
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.command,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = log.result,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MicButton(
    assistantState: AssistantState,
    onClick: () -> Unit
) {
    val isActive = assistantState == AssistantState.LISTENING

    val containerColor by animateColorAsState(
        targetValue = when (assistantState) {
            AssistantState.LISTENING -> MaterialTheme.colorScheme.secondary
            AssistantState.PROCESSING -> MaterialTheme.colorScheme.tertiary
            AssistantState.ERROR -> MaterialTheme.colorScheme.error
            AssistantState.SPEAKING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            else -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(300),
        label = "micColor"
    )

    val contentColor by animateColorAsState(
        targetValue = when (assistantState) {
            AssistantState.LISTENING -> MaterialTheme.colorScheme.onSecondary
            AssistantState.PROCESSING -> MaterialTheme.colorScheme.onTertiary
            AssistantState.ERROR -> MaterialTheme.colorScheme.onError
            else -> MaterialTheme.colorScheme.onPrimary
        },
        animationSpec = tween(300),
        label = "micContentColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micPulseScale"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .height(52.dp)
            .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale },
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(26.dp),
        contentPadding = PaddingValues(horizontal = 28.dp)
    ) {
        Icon(
            imageVector = when (assistantState) {
                AssistantState.LISTENING -> Icons.Filled.MicOff
                AssistantState.PROCESSING -> Icons.Filled.HourglassTop
                AssistantState.SPEAKING -> Icons.Filled.GraphicEq
                AssistantState.ERROR -> Icons.Filled.ErrorOutline
                else -> Icons.Filled.Mic
            },
            contentDescription = "Toggle microphone",
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when (assistantState) {
                AssistantState.LISTENING -> "Stop Listening"
                AssistantState.PROCESSING -> "Processing..."
                AssistantState.SPEAKING -> "Speaking..."
                AssistantState.ERROR -> "Retry"
                else -> "Tap to Speak"
            },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AiOrbVisualization(
    assistantState: AssistantState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbTransition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (assistantState) {
                    AssistantState.LISTENING -> 800
                    AssistantState.PROCESSING -> 400
                    AssistantState.SPEAKING -> 600
                    else -> 2000
                },
                easing = EaseInOutCubic
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (assistantState) {
                    AssistantState.PROCESSING -> 2000
                    AssistantState.LISTENING -> 4000
                    else -> 8000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error

    val orbColor = when (assistantState) {
        AssistantState.IDLE -> primaryColor
        AssistantState.LISTENING -> secondaryColor
        AssistantState.PROCESSING -> tertiaryColor
        AssistantState.SPEAKING -> primaryColor
        AssistantState.ERROR -> errorColor
    }

    val accentColor = when (assistantState) {
        AssistantState.IDLE -> secondaryColor
        AssistantState.LISTENING -> primaryColor
        AssistantState.PROCESSING -> secondaryColor
        AssistantState.SPEAKING -> tertiaryColor
        AssistantState.ERROR -> Color(0xFFFF8A80)
    }

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val baseRadius = size.minDimension / 3f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    orbColor.copy(alpha = glowAlpha * 0.3f),
                    orbColor.copy(alpha = 0f)
                ),
                center = Offset(centerX, centerY),
                radius = baseRadius * 1.8f
            ),
            radius = baseRadius * 1.8f,
            center = Offset(centerX, centerY)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    orbColor.copy(alpha = glowAlpha * 0.5f),
                    orbColor.copy(alpha = glowAlpha * 0.15f),
                    accentColor.copy(alpha = 0f)
                ),
                center = Offset(centerX, centerY),
                radius = baseRadius * 1.3f * pulseScale
            ),
            radius = baseRadius * 1.3f * pulseScale,
            center = Offset(centerX, centerY)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    orbColor,
                    orbColor.copy(alpha = 0.8f)
                ),
                center = Offset(centerX - baseRadius * 0.3f, centerY - baseRadius * 0.3f),
                radius = baseRadius * pulseScale
            ),
            radius = baseRadius * pulseScale,
            center = Offset(centerX, centerY)
        )

        val ringRadius = baseRadius * 1.2f * pulseScale
        drawCircle(
            color = orbColor.copy(alpha = 0.4f * glowAlpha),
            radius = ringRadius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 1.5.dp.toPx())
        )

        val dotCount = 6
        for (j in 0 until dotCount) {
            val angle = Math.toRadians((rotationAngle + j * (360.0 / dotCount)).toDouble())
            val dotX = centerX + ringRadius * cos(angle).toFloat()
            val dotY = centerY + ringRadius * sin(angle).toFloat()
            drawCircle(
                color = accentColor.copy(alpha = glowAlpha),
                radius = 2.5.dp.toPx(),
                center = Offset(dotX, dotY)
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.5f),
                    Color.White.copy(alpha = 0f)
                ),
                center = Offset(
                    centerX - baseRadius * 0.2f,
                    centerY - baseRadius * 0.2f
                ),
                radius = baseRadius * 0.35f
            ),
            radius = baseRadius * 0.35f,
            center = Offset(
                centerX - baseRadius * 0.2f,
                centerY - baseRadius * 0.2f
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickActionChips(onAction: (String) -> Unit) {
    data class QuickAction(
        val label: String,
        val command: String,
        val icon: @Composable () -> Unit
    )

    val actions = listOf(
        QuickAction("Call", "call ") {
            Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        QuickAction("Message", "send message to ") {
            Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        QuickAction("Search", "search for ") {
            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        QuickAction("Alarm", "set alarm for ") {
            Icon(Icons.Filled.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        QuickAction("Navigate", "navigate to ") {
            Icon(Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        QuickAction("WiFi", "toggle wifi") {
            Icon(Icons.Filled.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        QuickAction("Camera", "take a photo") {
            Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        QuickAction("News", "read news") {
            Icon(Icons.Filled.Newspaper, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        QuickAction("Email", "send email to ") {
            Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(16.dp))
        },
        QuickAction("Play Music", "play music") {
            Icon(Icons.Filled.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    )

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        actions.forEach { action ->
            AssistChip(
                onClick = { onAction(action.command) },
                label = {
                    Text(
                        action.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = action.icon,
                shape = RoundedCornerShape(20.dp),
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    leadingIconContentColor = MaterialTheme.colorScheme.primary
                ),
                border = AssistChipDefaults.assistChipBorder(
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }
    }
}
