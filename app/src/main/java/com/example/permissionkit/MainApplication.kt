package com.example.permissionkit

import android.app.Activity
import android.app.Application
import android.os.Bundle

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        registerLifeCycle()
    }

    private fun registerLifeCycle() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(
                activity: Activity,
                savedInstanceState: Bundle
            ) {
//                ActivityStack.getInstance().push(activity)
            }

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(
                activity: Activity,
                outState: Bundle
            ) {
            }

            override fun onActivityDestroyed(activity: Activity) {
//                ActivityStack.getInstance().pop(activity)
            }
        })
    }

}