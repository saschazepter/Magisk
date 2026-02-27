package com.topjohnwu.magisk.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topjohnwu.magisk.arch.VMFactory
import com.topjohnwu.magisk.core.Info
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.topjohnwu.magisk.core.R as CoreR

@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    onNavigateToSettings: () -> Unit,
    vm: HomeViewModel = viewModel(factory = VMFactory)
) {
    val appState by vm.appStateFlow.collectAsStateWithLifecycle()
    val managerRemoteVersion by vm.managerRemoteVersionStrFlow.collectAsStateWithLifecycle()
    val progress by vm.stateManagerProgressFlow.collectAsStateWithLifecycle()
    val isNoticeVisible by vm.isNoticeVisibleFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.startLoading() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        if (isNoticeVisible) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(CoreR.string.home_notice_content),
                        color = MiuixTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { vm.hideNotice() }) {
                        Text(text = stringResource(android.R.string.ok))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Magisk section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Magisk",
                    style = MiuixTheme.textStyles.title3
                )
                Spacer(modifier = Modifier.height(8.dp))
                val magiskVersionText = Info.env.run {
                    if (isActive) "$versionString ($versionCode)" + if (isDebug) " (D)" else ""
                    else stringResource(CoreR.string.not_available)
                }
                Text(
                    text = "${stringResource(CoreR.string.home_installed_version)}: $magiskVersionText",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                val magiskStatus = when (vm.magiskState) {
                    HomeViewModel.State.UP_TO_DATE -> "Up to date"
                    HomeViewModel.State.OUTDATED -> stringResource(CoreR.string.update_available)
                    HomeViewModel.State.INVALID -> stringResource(CoreR.string.not_available)
                    HomeViewModel.State.LOADING -> stringResource(CoreR.string.loading)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { vm.onMagiskPressed() },
                    enabled = vm.magiskState != HomeViewModel.State.LOADING
                ) {
                    Text(text = magiskStatus)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Manager section
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(CoreR.string.home_app_title),
                    style = MiuixTheme.textStyles.title3
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stringResource(CoreR.string.home_installed_version)}: ${vm.managerInstalledVersion}",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                val remoteText = when {
                    managerRemoteVersion.isNotEmpty() -> managerRemoteVersion
                    appState == HomeViewModel.State.LOADING -> stringResource(CoreR.string.loading)
                    else -> stringResource(CoreR.string.not_available)
                }
                Text(
                    text = "${stringResource(CoreR.string.home_latest_version)}: $remoteText",
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                if (progress > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$progress%", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { vm.onManagerPressed() },
                    enabled = appState != HomeViewModel.State.LOADING
                ) {
                    Text(
                        text = when (appState) {
                             HomeViewModel.State.OUTDATED -> stringResource(CoreR.string.update_available)
                            else -> stringResource(CoreR.string.home_app_title)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        SuperArrow(
            title = stringResource(CoreR.string.settings),
            onClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}
