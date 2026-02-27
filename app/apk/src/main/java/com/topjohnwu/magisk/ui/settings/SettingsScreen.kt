package com.topjohnwu.magisk.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topjohnwu.magisk.arch.VMFactory
import com.topjohnwu.magisk.core.Config
import com.topjohnwu.magisk.core.Const
import com.topjohnwu.magisk.core.Info
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.extra.SuperArrow
import top.yukonga.miuix.kmp.extra.SuperSwitch
import com.topjohnwu.magisk.core.R as CoreR

@Composable
fun SettingsScreen(
    paddingValues: PaddingValues,
    onNavigateToDenyList: () -> Unit,
    vm: SettingsViewModel = viewModel(factory = VMFactory)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // --- Customization ---
        SmallTitle(text = stringResource(CoreR.string.settings_customization))

        SuperArrow(
            title = stringResource(CoreR.string.section_theme),
            onClick = { /* Theme selection - handled separately */ }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- App Settings ---
        SmallTitle(text = stringResource(CoreR.string.home_app_title))

        var checkUpdate by remember { mutableStateOf(Config.checkUpdate) }
        SuperSwitch(
            title = stringResource(CoreR.string.settings_check_update_title),
            summary = stringResource(CoreR.string.settings_check_update_summary),
            checked = checkUpdate,
            onCheckedChange = { checked ->
                checkUpdate = checked
                Config.checkUpdate = checked
            }
        )

        var doh by remember { mutableStateOf(Config.doh) }
        SuperSwitch(
            title = stringResource(CoreR.string.settings_doh_title),
            summary = stringResource(CoreR.string.settings_doh_description),
            checked = doh,
            onCheckedChange = { checked ->
                doh = checked
                Config.doh = checked
            }
        )

        var randName by remember { mutableStateOf(Config.randName) }
        SuperSwitch(
            title = stringResource(CoreR.string.settings_random_name_title),
            summary = stringResource(CoreR.string.settings_random_name_description),
            checked = randName,
            onCheckedChange = { checked ->
                randName = checked
                Config.randName = checked
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Magisk ---
        if (Info.env.isActive) {
            SmallTitle(text = stringResource(CoreR.string.magisk))

            SuperArrow(
                title = stringResource(CoreR.string.settings_hosts_title),
                summary = stringResource(CoreR.string.settings_hosts_summary),
                onClick = { vm.createHosts() }
            )

            if (Const.Version.atLeast_24_0()) {
                var zygisk by remember { mutableStateOf(Config.zygisk) }
                SuperSwitch(
                    title = stringResource(CoreR.string.zygisk),
                    summary = stringResource(
                        if (Zygisk.mismatch) CoreR.string.reboot_apply_change
                        else CoreR.string.settings_zygisk_summary
                    ),
                    checked = zygisk,
                    onCheckedChange = { checked ->
                        zygisk = checked
                        Config.zygisk = checked
                    }
                )

                var denyListEnabled by remember { mutableStateOf(Config.denyList) }
                SuperSwitch(
                    title = stringResource(CoreR.string.settings_denylist_title),
                    summary = stringResource(CoreR.string.settings_denylist_summary),
                    checked = denyListEnabled,
                    onCheckedChange = { checked ->
                        denyListEnabled = checked
                        DenyList.value = checked
                    }
                )

                SuperArrow(
                    title = stringResource(CoreR.string.settings_denylist_config_title),
                    summary = stringResource(CoreR.string.settings_denylist_config_summary),
                    onClick = onNavigateToDenyList
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- Superuser ---
        if (Info.showSuperUser) {
            SmallTitle(text = stringResource(CoreR.string.superuser))

            var suAuth by remember { mutableStateOf(Config.suAuth) }
            SuperSwitch(
                title = stringResource(CoreR.string.settings_su_auth_title),
                summary = stringResource(
                    if (Info.isDeviceSecure) CoreR.string.settings_su_auth_summary
                    else CoreR.string.settings_su_auth_insecure
                ),
                checked = suAuth,
                enabled = Info.isDeviceSecure,
                onCheckedChange = { checked ->
                    suAuth = checked
                    Config.suAuth = checked
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
