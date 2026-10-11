package com.jagoalt.praisebreak.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jagoalt.praisebreak.ui.apps.AppsRoute
import com.jagoalt.praisebreak.ui.bible.BibleRoute
import com.jagoalt.praisebreak.ui.home.AboutRoute
import com.jagoalt.praisebreak.ui.home.HomeRoute
import com.jagoalt.praisebreak.ui.setup.PermissionsRoute
import com.jagoalt.praisebreak.ui.setup.SetupRoute

private enum class PraiseTab(val route: String, val label: String) {
    Today("today", "Today"),
    Apps("apps", "Apps"),
    Setup("setup", "Setup")
}

@Composable
fun PraiseApp(model: PraiseViewModel = viewModel()) {
    val nav = rememberNavController()
    val lifecycle = LocalLifecycleOwner.current
    DisposableEffect(lifecycle, model) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) model.refreshAccess()
        }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer) }
    }
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    Scaffold(
        bottomBar = {
            NavigationBar {
                PraiseTab.values().forEach { tab ->
                    val selected = when (tab) {
                        PraiseTab.Setup -> route == tab.route || route == "bible" || route == "permissions"
                        PraiseTab.Today -> route == tab.route || route == "about"
                        else -> route == tab.route
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(tab.label.take(1)) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = PraiseTab.Today.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(PraiseTab.Today.route) { HomeRoute(model) { nav.navigate("about") } }
            composable(PraiseTab.Apps.route) { AppsRoute(model) }
            composable(PraiseTab.Setup.route) {
                SetupRoute(
                    model,
                    onBible = { nav.navigate("bible") },
                    onPermissions = { nav.navigate("permissions") }
                )
            }
            composable("permissions") { PermissionsRoute(model) { nav.popBackStack() } }
            composable("bible") { BibleRoute(model) { nav.popBackStack() } }
            composable("about") { AboutRoute { nav.popBackStack() } }
        }
    }
}
