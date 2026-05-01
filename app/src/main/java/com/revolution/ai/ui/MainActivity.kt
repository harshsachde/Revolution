package com.revolution.ai.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
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
import com.revolution.ai.data.model.AppPermissionEntry
import com.revolution.ai.data.model.AssistantState
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
    ) { }

    private var serviceReceiver: BroadcastReceiver? = null
    private var viewModelRef: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestAllPermissions()

        setContent {
            val vm: MainViewModel = viewModel()

            LaunchedEffect(Unit) {
                viewModelRef = vm
            }

            val darkTheme by vm.isDarkTheme.collectAsState()
            val appName by vm.appName.collectAsState()
            val isAlwaysListening by vm.isAlwaysListening.collectAsState()

            LaunchedEffect(isAlwaysListening) {
                if (isAlwaysListening) {
                    VoiceAssistantService.startService(this@MainActivity)
                }
            }

            LaunchedEffect(Unit) {
                loadInstalledApps(vm)
            }

            RevolutionTheme(darkTheme = darkTheme) {
                RevolutionApp(viewModel = vm, appName = appName)
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

    private fun loadInstalledApps(vm: MainViewModel) {
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
            .filter { app ->
                pm.getLaunchIntentForPackage(app.packageName) != null &&
                        app.packageName != packageName
            }
            .sortedBy { pm.getApplicationLabel(it).toString().lowercase() }

        val currentPerms = vm.appPermissions.value.associateBy { it.packageName }

        apps.forEach { app ->
            if (!currentPerms.containsKey(app.packageName)) {
                val label = pm.getApplicationLabel(app).toString()
                vm.insertAppPermission(
                    AppPermissionEntry(
                        packageName = app.packageName,
                        appName = label,
                        isAllowed = true
                    )
                )
            }
        }
    }

    private fun registerServiceReceiver() {
        serviceReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent ?: return
                val vm = viewModelRef ?: return
                when (intent.action) {
                    VoiceAssistantService.ACTION_STATE_CHANGED -> {
                        val stateName = intent.getStringExtra(VoiceAssistantService.EXTRA_STATE)
                        stateName?.let {
                            try {
                                val state = AssistantState.valueOf(it)
                                vm.setAssistantState(state)
                            } catch (_: IllegalArgumentException) { }
                        }
                    }
                    VoiceAssistantService.ACTION_RESPONSE -> {
                        val message = intent.getStringExtra(VoiceAssistantService.EXTRA_MESSAGE)
                        if (!message.isNullOrBlank()) {
                            vm.setLastResponse(message)
                        }
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
                selectedIcon = { Icon(Icons.Filled.Assessment, contentDescription = "Logs") },
                unselectedIcon = { Icon(Icons.Outlined.Assessment, contentDescription = "Logs") }
            ),
            NavDestination(
                route = "permissions",
                label = "Apps",
                selectedIcon = { Icon(Icons.Filled.Security, contentDescription = "Apps") },
                unselectedIcon = { Icon(Icons.Outlined.Security, contentDescription = "Apps") }
            ),
            NavDestination(
                route = "guide",
                label = "Guide",
                selectedIcon = { Icon(Icons.Filled.Help, contentDescription = "Guide") },
                unselectedIcon = { Icon(Icons.Outlined.Help, contentDescription = "Guide") }
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
                fadeIn(animationSpec = tween(250))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(250))
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
