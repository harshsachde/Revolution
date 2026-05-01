package com.revolution.ai.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.revolution.ai.service.VoiceAssistantService
import com.revolution.ai.ui.screens.guide.GuideScreen
import com.revolution.ai.ui.screens.home.HomeScreen
import com.revolution.ai.ui.screens.logs.LogsScreen
import com.revolution.ai.ui.screens.permissions.PermissionsScreen
import com.revolution.ai.ui.screens.settings.SettingsScreen
import com.revolution.ai.ui.theme.RevolutionTheme
import com.revolution.ai.util.PermissionHelper

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Permissions handled; UI recomposes based on granted state */ }

    private var serviceReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestAllPermissions()

        setContent {
            val viewModel: MainViewModel = viewModel()

            val darkTheme by viewModel.isDarkTheme.collectAsState()
            val appName by viewModel.appName.collectAsState()
            val isAlwaysListening by viewModel.isAlwaysListening.collectAsState()

            LaunchedEffect(isAlwaysListening) {
                if (isAlwaysListening) {
                    VoiceAssistantService.startService(this@MainActivity)
                } else {
                    VoiceAssistantService.stopService(this@MainActivity)
                }
            }

            RevolutionTheme(darkTheme = darkTheme) {
                RevolutionApp(viewModel = viewModel, appName = appName)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerServiceReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterServiceReceiver()
    }

    private fun requestAllPermissions() {
        val missing = PermissionHelper.getMissingPermissions(this)
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    private fun registerServiceReceiver() {
        serviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent ?: return
                when (intent.action) {
                    VoiceAssistantService.ACTION_STATE_CHANGED -> {
                        val stateName = intent.getStringExtra(VoiceAssistantService.EXTRA_STATE)
                        val spokenText = intent.getStringExtra(VoiceAssistantService.EXTRA_SPOKEN_TEXT)
                        stateName?.let {
                            try {
                                val state = com.revolution.ai.data.model.AssistantState.valueOf(it)
                                // State updates are received; ViewModel observes via service broadcasts
                            } catch (_: IllegalArgumentException) { }
                        }
                    }
                    VoiceAssistantService.ACTION_RESPONSE -> {
                        val message = intent.getStringExtra(VoiceAssistantService.EXTRA_MESSAGE)
                        // Response messages are received from the service
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(VoiceAssistantService.ACTION_STATE_CHANGED)
            addAction(VoiceAssistantService.ACTION_RESPONSE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(serviceReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(serviceReceiver, filter)
        }
    }

    private fun unregisterServiceReceiver() {
        serviceReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: IllegalArgumentException) { }
            serviceReceiver = null
        }
    }
}

private data class NavDestination(
    val route: String,
    val label: String,
    val selectedIcon: @Composable () -> Unit,
    val unselectedIcon: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RevolutionApp(viewModel: MainViewModel, appName: String) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val destinations = remember {
        listOf(
            NavDestination(
                route = "home",
                label = "Home",
                selectedIcon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                unselectedIcon = { Icon(Icons.Outlined.Home, contentDescription = "Home") }
            ),
            NavDestination(
                route = "settings",
                label = "Settings",
                selectedIcon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                unselectedIcon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") }
            ),
            NavDestination(
                route = "logs",
                label = "Logs",
                selectedIcon = { Icon(Icons.Filled.List, contentDescription = "Logs") },
                unselectedIcon = { Icon(Icons.Outlined.List, contentDescription = "Logs") }
            ),
            NavDestination(
                route = "permissions",
                label = "Permissions",
                selectedIcon = { Icon(Icons.Filled.Security, contentDescription = "Permissions") },
                unselectedIcon = { Icon(Icons.Outlined.Security, contentDescription = "Permissions") }
            ),
            NavDestination(
                route = "guide",
                label = "Guide",
                selectedIcon = { Icon(Icons.Filled.MenuBook, contentDescription = "Guide") },
                unselectedIcon = { Icon(Icons.Outlined.MenuBook, contentDescription = "Guide") }
            )
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = appName)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = NavigationBarDefaults.Elevation
            ) {
                destinations.forEach { destination ->
                    val selected = currentRoute == destination.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != destination.route) {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            if (selected) destination.selectedIcon()
                            else destination.unselectedIcon()
                        },
                        label = { Text(destination.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                    initialOffsetX = { it / 4 },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                    targetOffsetX = { -it / 4 },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                    initialOffsetX = { -it / 4 },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                    targetOffsetX = { it / 4 },
                    animationSpec = tween(300)
                )
            }
        ) {
            composable("home") {
                HomeScreen(viewModel = viewModel)
            }
            composable("settings") {
                SettingsScreen(viewModel = viewModel)
            }
            composable("logs") {
                LogsScreen(viewModel = viewModel)
            }
            composable("permissions") {
                PermissionsScreen(viewModel = viewModel)
            }
            composable("guide") {
                GuideScreen()
            }
        }
    }
}
