package com.topjohnwu.magisk.arch

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

/**
 * Class for passing events from ViewModels to Activities/Fragments
 */
abstract class ViewEvent

interface ContextExecutor {
    operator fun invoke(context: Context)
}

interface ActivityExecutor {
    operator fun invoke(activity: AppCompatActivity)
}
