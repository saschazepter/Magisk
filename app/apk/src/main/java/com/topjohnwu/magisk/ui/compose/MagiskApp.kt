package com.topjohnwu.magisk.ui.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.ui.home.HomeScreen
import com.topjohnwu.magisk.ui.log.LogScreen
import com.topjohnwu.magisk.ui.module.ModulesScreen
import com.topjohnwu.magisk.ui.settings.SettingsScreen
import com.topjohnwu.magisk.ui.superuser.SuperuserScreen
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
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
    var navBackStack by remember { mutableStateOf(listOf<MainScreen>()) }

    val isRootScreen = currentScreen in listOf(
        MainScreen.HOME, MainScreen.MODULES, MainScreen.SUPERUSER, MainScreen.LOG
    )

    MagiskTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = when (currentScreen) {
                        MainScreen.HOME -> stringResource(CoreR.string.section_home)
                        MainScreen.MODULES -> stringResource(CoreR.string.modules)
                        MainScreen.SUPERUSER -> stringResource(CoreR.string.superuser)
                        MainScreen.LOG -> stringResource(CoreR.string.logs)
                        MainScreen.SETTINGS -> stringResource(CoreR.string.settings)
                    },
                    navigationIcon = if (currentScreen == MainScreen.SETTINGS) {
                        {
                            IconButton(onClick = {
                                currentScreen = navBackStack.lastOrNull() ?: MainScreen.HOME
                                navBackStack = navBackStack.dropLast(1)
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_back_md2),
                                    contentDescription = null
                                )
                            }
                        }
                    } else null
                )
            },
            navigationBar = {
                if (isRootScreen) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.HOME,
                            onClick = {
                                currentScreen = MainScreen.HOME
                                navBackStack = emptyList()
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(
                                        if (currentScreen == MainScreen.HOME)
                                            R.drawable.ic_home_filled_md2
                                        else R.drawable.ic_home_outlined_md2
                                    ),
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(CoreR.string.section_home)) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.MODULES,
                            enabled = Info.env.isActive,
                            onClick = {
                                currentScreen = MainScreen.MODULES
                                navBackStack = emptyList()
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(
                                        if (currentScreen == MainScreen.MODULES)
                                            R.drawable.ic_module_filled_md2
                                        else R.drawable.ic_module_outlined_md2
                                    ),
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(CoreR.string.modules)) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.SUPERUSER,
                            enabled = Info.showSuperUser,
                            onClick = {
                                currentScreen = MainScreen.SUPERUSER
                                navBackStack = emptyList()
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(
                                        if (currentScreen == MainScreen.SUPERUSER)
                                            R.drawable.ic_superuser_filled_md2
                                        else R.drawable.ic_superuser_outlined_md2
                                    ),
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(CoreR.string.superuser)) }
                        )
                        NavigationBarItem(
                            selected = currentScreen == MainScreen.LOG,
                            onClick = {
                                currentScreen = MainScreen.LOG
                                navBackStack = emptyList()
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(
                                        if (currentScreen == MainScreen.LOG)
                                            R.drawable.ic_bug_filled_md2
                                        else R.drawable.ic_bug_outlined_md2
                                    ),
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(CoreR.string.logs)) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            when (currentScreen) {
                MainScreen.HOME -> HomeScreen(
                    paddingValues = paddingValues,
                    onNavigateToSettings = {
                        navBackStack = navBackStack + MainScreen.HOME
                        currentScreen = MainScreen.SETTINGS
                    }
                )
                MainScreen.MODULES -> ModulesScreen(paddingValues = paddingValues)
                MainScreen.SUPERUSER -> SuperuserScreen(paddingValues = paddingValues)
                MainScreen.LOG -> LogScreen(paddingValues = paddingValues)
                MainScreen.SETTINGS -> SettingsScreen(
                    paddingValues = paddingValues,
                    onNavigateToDenyList = {
                        // DenyList config is handled via Fragment (existing)
                    }
                )
            }
        }
    }
}
