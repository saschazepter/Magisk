package com.topjohnwu.magisk.ui.compose

import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun MagiskTheme(content: @Composable () -> Unit) {
    MiuixTheme(
        controller = ThemeController(ColorSchemeMode.System),
        content = content
    )
}
