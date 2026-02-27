package com.topjohnwu.magisk.ui.superuser

import android.graphics.drawable.Drawable
import com.topjohnwu.magisk.core.model.su.SuPolicy

class PolicyRvItem(
    private val viewModel: SuperuserViewModel,
    val item: SuPolicy,
    val packageName: String,
    private val isSharedUid: Boolean,
    val icon: Drawable,
    val appName: String
) {
    val title get() = if (isSharedUid) "[SharedUID] $appName" else appName
}
