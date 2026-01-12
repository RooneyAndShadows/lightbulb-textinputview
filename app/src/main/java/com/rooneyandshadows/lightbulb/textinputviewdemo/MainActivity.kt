package com.rooneyandshadows.lightbulb.textinputviewdemo

import android.os.Bundle
import com.github.rooneyandshadows.lightbulb.application.activity.BaseActivity
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbActivity
import com.github.rooneyandshadows.lightbulb.textinputviewdemo.lightbulb.service.LightbulbService

@LightbulbActivity(layoutName = "activity_main", fragmentContainerId = "fragmentContainer")
class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            LightbulbService.route().toDemoHome().newRootScreen()
        }
    }

    override fun onUnhandledException(paramThread: Thread?, exception: Throwable) {
        super.onUnhandledException(paramThread, exception)
        exception.printStackTrace()
    }
}