package com.topjohnwu.magisk.ui.superuser

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.topjohnwu.magisk.R
import com.topjohnwu.magisk.arch.VMFactory
import com.topjohnwu.magisk.core.model.su.SuPolicy
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.extra.SuperSwitch
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.topjohnwu.magisk.core.R as CoreR

@Composable
fun SuperuserScreen(
    paddingValues: PaddingValues,
    vm: SuperuserViewModel = viewModel(factory = VMFactory)
) {
    val loading by vm.loadingFlow.collectAsStateWithLifecycle()
    val policies by vm.policiesFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.startLoading() }

    if (loading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(CoreR.string.loading))
        }
    } else if (policies.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(CoreR.string.superuser_policy_none),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(policies, key = { it.packageName }) { policy ->
                PolicyCard(policy = policy, vm = vm)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PolicyCard(policy: PolicyRvItem, vm: SuperuserViewModel) {
    var isEnabled by remember(policy.packageName) { mutableStateOf(policy.item.policy >= SuPolicy.ALLOW) }
    var shouldNotify by remember(policy.packageName) { mutableStateOf(policy.item.notification) }
    var shouldLog by remember(policy.packageName) { mutableStateOf(policy.item.logging) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DrawableImage(
                    drawable = policy.icon,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = policy.title, style = MiuixTheme.textStyles.title3)
                    Text(
                        text = policy.packageName,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
                IconButton(onClick = { vm.deletePressed(policy) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_md2),
                        contentDescription = stringResource(CoreR.string.superuser_toggle_revoke)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            SuperSwitch(
                title = stringResource(CoreR.string.grant),
                checked = isEnabled,
                onCheckedChange = { checked ->
                    isEnabled = checked
                    vm.updatePolicy(policy, if (checked) SuPolicy.ALLOW else SuPolicy.DENY)
                }
            )
            SuperSwitch(
                title = stringResource(CoreR.string.superuser_toggle_notification),
                checked = shouldNotify,
                onCheckedChange = { checked ->
                    shouldNotify = checked
                    policy.item.notification = checked
                    vm.updateNotify(policy)
                }
            )
            SuperSwitch(
                title = stringResource(CoreR.string.logs),
                checked = shouldLog,
                onCheckedChange = { checked ->
                    shouldLog = checked
                    policy.item.logging = checked
                    vm.updateLogging(policy)
                }
            )
        }
    }
}

@Composable
private fun DrawableImage(drawable: Drawable, modifier: Modifier = Modifier) {
    val bitmap: ImageBitmap = remember(drawable) {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            drawable.bitmap.asImageBitmap()
        } else {
            val width = drawable.intrinsicWidth.coerceAtLeast(1)
            val height = drawable.intrinsicHeight.coerceAtLeast(1)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp.asImageBitmap()
        }
    }
    Image(bitmap = bitmap, contentDescription = null, modifier = modifier)
}
