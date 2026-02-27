package com.topjohnwu.magisk.ui.surequest

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.topjohnwu.magisk.arch.ActivityExecutor
import com.topjohnwu.magisk.arch.ContextExecutor
import com.topjohnwu.magisk.arch.ViewEvent
import com.topjohnwu.magisk.arch.ViewModelHolder
import com.topjohnwu.magisk.arch.viewModel
import com.topjohnwu.magisk.core.Config
import com.topjohnwu.magisk.core.R
import com.topjohnwu.magisk.core.base.ActivityExtension
import com.topjohnwu.magisk.core.base.IActivityExtension
import com.topjohnwu.magisk.core.base.UntrackedActivity
import com.topjohnwu.magisk.core.su.SuCallbackHandler
import com.topjohnwu.magisk.core.su.SuCallbackHandler.REQUEST
import com.topjohnwu.magisk.core.wrap
import com.topjohnwu.magisk.ui.compose.MagiskTheme
import com.topjohnwu.magisk.ui.theme.Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Text

open class SuRequestActivity : AppCompatActivity(), UntrackedActivity, IActivityExtension, ViewModelHolder {

    override val extension = ActivityExtension(this)
    override val viewModel: SuRequestViewModel by viewModel()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base.wrap())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.setHideOverlayWindows(true)
        }
        setTheme(Theme.selected.themeRes)
        super.onCreate(savedInstanceState)
        extension.onCreate(savedInstanceState)
        startObserveLiveData()

        if (intent.action == Intent.ACTION_VIEW) {
            val action = intent.getStringExtra("action")
            if (action == REQUEST) {
                viewModel.handleRequest(intent)
            } else {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        SuCallbackHandler.run(this@SuRequestActivity, action, intent.extras)
                    }
                    finish()
                }
            }
        } else {
            finish()
            return
        }

        setContent {
            MagiskTheme {
                val showDialog by viewModel.showDialogFlow.collectAsState()
                if (showDialog) {
                    SuRequestDialog(viewModel = viewModel)
                }
            }
        }

        if (Config.suTapjack) {
            window.decorView.rootView.accessibilityDelegate =
                SuRequestViewModel.EmptyAccessibilityDelegate
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        extension.onSaveInstanceState(outState)
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        theme.applyStyle(com.topjohnwu.magisk.R.style.Foundation_Floating, true)
        return theme
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        viewModel.denyPressed()
    }

    override fun finish() {
        super.finishAndRemoveTask()
    }

    override fun onEventDispatched(event: ViewEvent) {
        when (event) {
            is ContextExecutor -> event(this)
            is ActivityExecutor -> event(this)
        }
    }
}

@Composable
private fun SuRequestDialog(viewModel: SuRequestViewModel) {
    val denyText by viewModel.denyTextFlow.collectAsState()
    val selectedPosition by viewModel.selectedItemPositionFlow.collectAsState()
    val grantEnabled by viewModel.grantEnabledFlow.collectAsState()
    val timeoutEntries = stringArrayResource(com.topjohnwu.magisk.R.array.allow_timeout)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AndroidView(
                factory = { context ->
                    android.widget.ImageView(context).apply {
                        setImageDrawable(viewModel.icon)
                        layoutParams = android.view.ViewGroup.LayoutParams(96, 96)
                    }
                },
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = viewModel.title)
            Text(text = viewModel.packageName)

            Spacer(modifier = Modifier.height(12.dp))

            AndroidView(
                factory = { context ->
                    Spinner(context).apply {
                        adapter = ArrayAdapter(context,
                            com.topjohnwu.magisk.R.layout.item_spinner,
                            timeoutEntries)
                        setSelection(selectedPosition)
                        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                                viewModel.selectedItemPositionFlow.value = position
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                        }
                        setOnTouchListener { v, e ->
                            viewModel.spinnerTouched()
                            v.onTouchEvent(e)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { viewModel.denyPressed() }) {
                    Text(text = denyText.ifEmpty { stringResource(R.string.deny) })
                }

                Button(
                    onClick = { viewModel.grantPressed() },
                    enabled = grantEnabled
                ) {
                    Text(text = stringResource(R.string.grant))
                }
            }
        }
    }
}
