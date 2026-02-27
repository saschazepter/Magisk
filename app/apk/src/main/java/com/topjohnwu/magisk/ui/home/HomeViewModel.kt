package com.topjohnwu.magisk.ui.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.topjohnwu.magisk.arch.ActivityExecutor
import com.topjohnwu.magisk.arch.AsyncLoadViewModel
import com.topjohnwu.magisk.arch.ContextExecutor
import com.topjohnwu.magisk.arch.ViewEvent
import com.topjohnwu.magisk.core.BuildConfig
import com.topjohnwu.magisk.core.Config
import com.topjohnwu.magisk.core.Info
import com.topjohnwu.magisk.core.download.Subject
import com.topjohnwu.magisk.core.download.Subject.App
import com.topjohnwu.magisk.core.ktx.await
import com.topjohnwu.magisk.core.ktx.toast
import com.topjohnwu.magisk.core.repository.NetworkService
import com.topjohnwu.magisk.dialog.EnvFixDialog
import com.topjohnwu.magisk.dialog.ManagerInstallDialog
import com.topjohnwu.magisk.dialog.UninstallDialog
import com.topjohnwu.magisk.events.SnackbarEvent
import com.topjohnwu.magisk.utils.asText
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.roundToInt
import com.topjohnwu.magisk.core.R as CoreR

class HomeViewModel(
    private val svc: NetworkService
) : AsyncLoadViewModel() {

    enum class State {
        LOADING, INVALID, OUTDATED, UP_TO_DATE
    }

    val appStateFlow = MutableStateFlow(State.LOADING)
    val managerRemoteVersionStrFlow = MutableStateFlow("")
    val stateManagerProgressFlow = MutableStateFlow(0)
    val isNoticeVisibleFlow = MutableStateFlow(Config.safetyNotice)

    val magiskState
        get() = when {
            Info.isRooted && Info.env.isUnsupported -> State.OUTDATED
            !Info.env.isActive -> State.INVALID
            Info.env.versionCode < BuildConfig.APP_VERSION_CODE -> State.OUTDATED
            else -> State.UP_TO_DATE
        }

    val magiskInstalledVersion
        get() = Info.env.run {
            if (isActive)
                ("$versionString ($versionCode)" + if (isDebug) " (D)" else "").asText()
            else
                CoreR.string.not_available.asText()
        }

    val managerInstalledVersion
        get() = "${BuildConfig.APP_VERSION_NAME} (${BuildConfig.APP_VERSION_CODE})" +
            if (BuildConfig.DEBUG) " (D)" else ""

    companion object {
        private var checkedEnv = false
    }

    override suspend fun doLoadWork() {
        appStateFlow.value = State.LOADING
        Info.fetchUpdate(svc)?.apply {
            appStateFlow.value = when {
                BuildConfig.APP_VERSION_CODE < versionCode -> State.OUTDATED
                else -> State.UP_TO_DATE
            }

            val isDebug = Config.updateChannel == Config.Value.DEBUG_CHANNEL
            val versionStr = ("$version (${versionCode})" + if (isDebug) " (D)" else "")
            managerRemoteVersionStrFlow.value = versionStr
        } ?: run {
            appStateFlow.value = State.INVALID
            managerRemoteVersionStrFlow.value = ""
        }
        ensureEnv()
    }

    override fun onNetworkChanged(network: Boolean) = startLoading()

    fun onProgressUpdate(progress: Float, subject: Subject) {
        if (subject is App)
            stateManagerProgressFlow.value = progress.times(100f).roundToInt()
    }

    fun onLinkPressed(link: String) = object : ViewEvent(), ContextExecutor {
        override fun invoke(context: Context) {
            val intent = Intent(Intent.ACTION_VIEW, link.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                context.toast(CoreR.string.open_link_failed_toast, Toast.LENGTH_SHORT)
            }
        }
    }.publish()

    fun onDeletePressed() = UninstallDialog().show()

    fun onManagerPressed() = when (appStateFlow.value) {
        State.LOADING -> SnackbarEvent(CoreR.string.loading).publish()
        State.INVALID -> SnackbarEvent(CoreR.string.no_connection).publish()
        else -> withExternalRW {
            withInstallPermission {
                ManagerInstallDialog().show()
            }
        }
    }

    fun onMagiskPressed() = withExternalRW {
        SnackbarEvent(CoreR.string.install).publish()
    }

    fun hideNotice() {
        Config.safetyNotice = false
        isNoticeVisibleFlow.value = false
    }

    private suspend fun ensureEnv() {
        if (magiskState == State.INVALID || checkedEnv) return
        val cmd = "env_check ${Info.env.versionString} ${Info.env.versionCode}"
        val code = Shell.cmd(cmd).await().code
        if (code != 0) {
            EnvFixDialog(this, code).show()
        }
        checkedEnv = true
    }

    val showTest = false
    fun onTestPressed() = object : ViewEvent(), ActivityExecutor {
        override fun invoke(activity: AppCompatActivity) {
            /* Entry point to trigger test events within the app */
        }
    }.publish()
}
