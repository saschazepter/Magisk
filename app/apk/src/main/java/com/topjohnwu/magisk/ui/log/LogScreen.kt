package com.topjohnwu.magisk.ui.log

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.arch.VMFactory
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.topjohnwu.magisk.core.R as CoreR

@Composable
fun LogScreen(
    paddingValues: PaddingValues,
    vm: LogViewModel = viewModel(factory = VMFactory)
) {
    val loading by vm.loadingFlow.collectAsStateWithLifecycle()
    val suLogs by vm.suLogsFlow.collectAsStateWithLifecycle()
    val magiskLogs by vm.magiskLogsFlow.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { vm.startLoading() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(text = stringResource(CoreR.string.superuser)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(text = "Magisk") }
            )
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            if (selectedTab == 1) {
                IconButton(onClick = { vm.saveMagiskLog() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_save_md2),
                        contentDescription = stringResource(CoreR.string.menuSaveLog)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            IconButton(
                onClick = {
                    if (selectedTab == 0) vm.clearLog()
                    else vm.clearMagiskLog()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete_md2),
                    contentDescription = stringResource(CoreR.string.menuClearLog)
                )
            }
        }

        if (loading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(CoreR.string.loading))
            }
        } else if (selectedTab == 0) {
            SuLogTab(suLogs = suLogs)
        } else {
            MagiskLogTab(magiskLogs = magiskLogs)
        }
    }
}

@Composable
private fun SuLogTab(suLogs: List<SuLogRvItem>) {
    if (suLogs.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(CoreR.string.log_data_none),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(suLogs, key = { it.log.id }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(
                                if (item.log.action == 1) R.drawable.ic_close_md2
                                else R.drawable.ic_check_md2
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.log.appName,
                                style = MiuixTheme.textStyles.title3
                            )
                            Text(
                                text = item.info,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MagiskLogTab(magiskLogs: List<String>) {
    if (magiskLogs.isEmpty() || magiskLogs.all { it.isBlank() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(CoreR.string.log_data_magisk_none),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            magiskLogs.forEach { line ->
                Text(
                    text = line,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }
    }
}
