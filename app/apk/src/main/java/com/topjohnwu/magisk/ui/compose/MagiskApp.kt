package com.topjohnwu.magisk.ui.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.ui.home.HomeScreen
import com.topjohnwu.magisk.ui.log.LogScreen
import com.topjohnwu.magisk.ui.module.ModulesScreen
import com.topjohnwu.magisk.ui.settings.SettingsScreen
import com.topjohnwu.magisk.ui.superuser.SuperuserScreen
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import com.topjohnwu.magisk.core.R as CoreR

enum class MainScreen { HOME, MODULES, SUPERUSER, LOG, SETTINGS }

@Composable
fun MagiskApp(initialSection: String? = null) {
    val initialScreen = when (initialSection) {
        "superuser" -> MainScreen.SUPERUSER
        "modules" -> MainScreen.MODULES
        "settings" -> MainScreen.SETTINGS
        else -> MainScreen.HOME
    }

    var currentScreen by remember { mutableStateOf(initialScreen) }

    MagiskTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.HOME,
                            onClick = { currentScreen = MainScreen.HOME },
                            icon = Icons.Rounded.Home,
                            label = stringResource(CoreR.string.section_home)
                        )
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.MODULES,
                            enabled = Info.env.isActive,
                            onClick = { currentScreen = MainScreen.MODULES },
                            icon = Icons.Rounded.Extension,
                            label = stringResource(CoreR.string.modules)
                        )
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.SUPERUSER,
                            enabled = Info.showSuperUser,
                            onClick = { currentScreen = MainScreen.SUPERUSER },
                            icon = Icons.Rounded.Security,
                            label = stringResource(CoreR.string.superuser)
                        )
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.LOG,
                            onClick = { currentScreen = MainScreen.LOG },
                            icon = Icons.Rounded.BugReport,
                            label = stringResource(CoreR.string.logs)
                        )
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.SETTINGS,
                            onClick = { currentScreen = MainScreen.SETTINGS },
                            icon = Icons.Rounded.Settings,
                            label = stringResource(CoreR.string.settings)
                        )
                    }
                }
        ) { paddingValues ->
            when (currentScreen) {
                MainScreen.HOME -> HomeScreen(paddingValues = paddingValues)
                MainScreen.MODULES -> ModulesScreen(paddingValues = paddingValues)
                MainScreen.SUPERUSER -> SuperuserScreen(paddingValues = paddingValues)
                MainScreen.LOG -> LogScreen(paddingValues = paddingValues)
                MainScreen.SETTINGS -> SettingsScreen(
                    paddingValues = paddingValues,
                    onNavigateToDenyList = { }
                )
            }
        }
    }
}
