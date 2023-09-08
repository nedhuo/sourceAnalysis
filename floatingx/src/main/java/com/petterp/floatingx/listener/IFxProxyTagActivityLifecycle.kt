package com.petterp.floatingx.listener

import android.app.Activity
import android.os.Bundle

/**
 * 对允许显示浮窗的activity生命周期的转发
 * @author petterp
 */
interface IFxProxyTagActivityLifecycle {

    fun onCreated(activity: Activity, bundle: Bundle?)

    fun onStarted(activity: Activity)

    fun onResumes(activity: Activity)

    fun onPaused(activity: Activity)

    fun onStopped(activity: Activity)

    fun onSaveInstanceState(activity: Activity, bundle: Bundle?)

    fun onDestroyed(activity: Activity)
}
