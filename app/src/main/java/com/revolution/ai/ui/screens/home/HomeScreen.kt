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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val assistantState by viewModel.assistantState.collectAsState()
    val aiName by viewModel.aiName.collectAsState()
    val lastResponse by viewModel.lastResponse.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()

    var textCommand by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { viewModel.toggleListening() },
                containerColor = when (assistantState) {
                    AssistantState.LISTENING -> MaterialTheme.colorScheme.secondary
                    AssistantState.PROCESSING -> MaterialTheme.colorScheme.tertiary
                    AssistantState.ERROR -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                },
                contentColor = when (assistantState) {
                    AssistantState.LISTENING -> MaterialTheme.colorScheme.onSecondary
                    AssistantState.PROCESSING -> MaterialTheme.colorScheme.onTertiary
                    AssistantState.ERROR -> MaterialTheme.colorScheme.onError
                    else -> MaterialTheme.colorScheme.onPrimary
                }
            ) {
                Icon(
                    imageVector = when (assistantState) {
                        AssistantState.LISTENING -> Icons.Filled.MicOff
                        AssistantState.PROCESSING -> Icons.Filled.HourglassTop
                        AssistantState.SPEAKING -> Icons.Filled.VolumeUp
                        AssistantState.ERROR -> Icons.Filled.ErrorOutline
                        else -> Icons.Filled.Mic
                    },
                    contentDescription = "Toggle microphone",
                    modifier = Modifier.size(36.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = aiName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

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
                            AssistantState.IDLE -> "Ready to assist"
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

                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                AiOrbVisualization(
                    assistantState = assistantState,
                    modifier = Modifier
                        .size(220.dp)
                        .padding(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                AnimatedVisibility(
                    visible = lastResponse.isNotBlank(),
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
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
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = lastResponse,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                OutlinedTextField(
                    value = textCommand,
                    onValueChange = { textCommand = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Type a command...") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                viewModel.processTextCommand(textCommand)
                                textCommand = ""
                            },
                            enabled = textCommand.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                contentDescription = "Send command",
                                tint = if (textCommand.isNotBlank())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    shape = RoundedCornerShape(28.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
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

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (recentLogs.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Commands",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(recentLogs) { log ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
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
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.command,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = log.result,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
private fun AiOrbVisualization(
    assistantState: AssistantState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbTransition")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
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
                    orbColor.copy(alpha = glowAlpha * 0.4f),
                    orbColor.copy(alpha = 0f)
                ),
                center = Offset(centerX, centerY),
                radius = baseRadius * 2f
            ),
            radius = baseRadius * 2f,
            center = Offset(centerX, centerY)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    orbColor.copy(alpha = glowAlpha * 0.6f),
                    orbColor.copy(alpha = glowAlpha * 0.2f),
                    accentColor.copy(alpha = 0f)
                ),
                center = Offset(centerX, centerY),
                radius = baseRadius * 1.5f * pulseScale
            ),
            radius = baseRadius * 1.5f * pulseScale,
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

        val ringCount = if (assistantState == AssistantState.PROCESSING) 3 else 2
        for (i in 0 until ringCount) {
            val ringRadius = baseRadius * (1.2f + i * 0.2f) * pulseScale
            val angleOffset = rotationAngle + i * 120f
            val ringColor = if (i % 2 == 0) orbColor else accentColor

            drawCircle(
                color = ringColor.copy(alpha = (0.5f - i * 0.15f) * glowAlpha),
                radius = ringRadius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2.dp.toPx())
            )

            val dotCount = 4 + i * 2
            for (j in 0 until dotCount) {
                val angle = Math.toRadians((angleOffset + j * (360.0 / dotCount)).toDouble())
                val dotX = centerX + ringRadius * cos(angle).toFloat()
                val dotY = centerY + ringRadius * sin(angle).toFloat()
                drawCircle(
                    color = accentColor.copy(alpha = glowAlpha),
                    radius = 3.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.6f),
                    Color.White.copy(alpha = 0f)
                ),
                center = Offset(
                    centerX - baseRadius * 0.25f,
                    centerY - baseRadius * 0.25f
                ),
                radius = baseRadius * 0.4f
            ),
            radius = baseRadius * 0.4f,
            center = Offset(
                centerX - baseRadius * 0.25f,
                centerY - baseRadius * 0.25f
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickActionChips(onAction: (String) -> Unit) {
    data class QuickAction(val label: String, val icon: @Composable () -> Unit)

    val actions = listOf(
        QuickAction("Call") { Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(18.dp)) },
        QuickAction("Message") { Icon(Icons.Filled.Message, contentDescription = null, modifier = Modifier.size(18.dp)) },
        QuickAction("Search") { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
        QuickAction("Settings") { Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp)) },
        QuickAction("Alarm") { Icon(Icons.Filled.Alarm, contentDescription = null, modifier = Modifier.size(18.dp)) }
    )

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { action ->
            AssistChip(
                onClick = { onAction(action.label) },
                label = { Text(action.label) },
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
