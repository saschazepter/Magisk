package com.topjohnwu.magisk.ui.log

import com.topjohnwu.magisk.core.AppContext
import com.topjohnwu.magisk.core.ktx.timeDateFormat
import com.topjohnwu.magisk.core.ktx.toTime
import com.topjohnwu.magisk.core.model.su.SuLog
import com.topjohnwu.magisk.core.R as CoreR

class SuLogRvItem(val log: SuLog) {

    val info = genInfo()

    private fun genInfo(): String {
        val res = AppContext.resources
        val sb = StringBuilder()
        val date = log.time.toTime(timeDateFormat)
        val toUid = res.getString(CoreR.string.target_uid, log.toUid)
        val fromPid = res.getString(CoreR.string.pid, log.fromPid)
        sb.append("$date\n$toUid  $fromPid")
        if (log.target != -1) {
            val pid = if (log.target == 0) "magiskd" else log.target.toString()
            val target = res.getString(CoreR.string.target_pid, pid)
            sb.append("  $target")
        }
        if (log.context.isNotEmpty()) {
            val context = res.getString(CoreR.string.selinux_context, log.context)
            sb.append("\n$context")
        }
        if (log.gids.isNotEmpty()) {
            val gids = res.getString(CoreR.string.supp_group, log.gids)
            sb.append("\n$gids")
        }
        sb.append("\n${log.command}")
        return sb.toString()
    }
}
