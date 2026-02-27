package com.topjohnwu.magisk.ui.module

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.topjohnwu.magisk.core.model.module.LocalModule
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.topjohnwu.magisk.core.R as CoreR

@Composable
fun ModulesScreen(
    paddingValues: PaddingValues,
    vm: ModuleViewModel = viewModel(factory = VMFactory)
) {
    val loading by vm.loadingFlow.collectAsStateWithLifecycle()
    val modules by vm.installedModulesFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.startLoading() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        if (loading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(CoreR.string.loading))
            }
        } else if (modules.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(CoreR.string.module_empty),
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(modules, key = { it.id }) { module ->
                    ModuleCard(module = module)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Install from storage button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_install),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { vm.installPressed() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(CoreR.string.module_action_install_external))
            }
        }
    }
}

@Composable
private fun ModuleCard(module: LocalModule) {
    var isEnabled by remember(module.id) { mutableStateOf(module.enable) }
    var isRemoved by remember(module.id) { mutableStateOf(module.remove) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = module.name,
                style = MiuixTheme.textStyles.title3
            )
            Text(
                text = "${module.version} · ${module.author}",
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
            if (module.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = module.description,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(
                    onClick = {
                        isEnabled = !isEnabled
                        module.enable = isEnabled
                    }
                ) {
                    Text(
                        text = if (isEnabled) "Enabled" else "Disabled"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        isRemoved = !isRemoved
                        module.remove = isRemoved
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_md2),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
