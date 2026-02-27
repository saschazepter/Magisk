package com.topjohnwu.magisk.ui.compose

import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MagiskTheme(content: @Composable () -> Unit) {
    MiuixTheme(
        colorSchemeMode = ColorSchemeMode.System,
        content = content
    )
}
