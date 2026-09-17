package com.example.billreminder.util

import android.app.Activity
import android.provider.Settings
import com.example.billreminder.R

/**
 * Gives screen transitions real meaning instead of a hard cut: drilling into
 * a screen slides forward, returning slides back, and switching between
 * sibling bottom-nav tabs cross-fades (lateral, not hierarchical). Respects
 * the system's "remove animations" accessibility setting.
 */
object TransitionHelper {

    private fun reducedMotion(activity: Activity): Boolean {
        val scale = Settings.Global.getFloat(activity.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        return scale == 0f
    }

    /** Drilling into a new screen (e.g. Dashboard -> Add/Edit Bill). */
    fun forward(activity: Activity) {
        if (reducedMotion(activity)) return
        @Suppress("DEPRECATION")
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    /** Returning from a drilled-into screen — call right after finish(). */
    fun back(activity: Activity) {
        if (reducedMotion(activity)) return
        @Suppress("DEPRECATION")
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    /** Switching between sibling bottom-nav tabs. */
    fun crossFade(activity: Activity) {
        if (reducedMotion(activity)) return
        @Suppress("DEPRECATION")
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
