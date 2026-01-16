package com.rooneyandshadows.lightbulb.textinputviewdemo

import android.os.Bundle
import com.github.rooneyandshadows.lightbulb.application.activity.BaseActivity
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbActivity
import com.github.rooneyandshadows.lightbulb.textinputviewdemo.R
import com.github.rooneyandshadows.lightbulb.textinputviewdemo.lightbulb.service.LightbulbService
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.google.android.material.color.MaterialColors

@LightbulbActivity(layoutName = "activity_main", fragmentContainerId = "fragmentContainer")
class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        println("+===================")
        println(DynamicColors.isDynamicColorAvailable())
        //if (DynamicColors.isDynamicColorAvailable()) {
        //    val options = DynamicColorsOptions.Builder()
        //        .setPrecondition { _, _ -> true }
        //        .build()
        //    DynamicColors.applyToActivityIfAvailable(this, options)
        //}


        // Make status bar colored according to colorPrimary
        val primaryColor = MaterialColors.getColor(this, R.attr.colorSurface, "Primary not found")
        window.statusBarColor = primaryColor


        if (savedInstanceState == null) {
            LightbulbService.route().toDemoHome().newRootScreen()
        }
    }

    //override fun onUnhandledException(paramThread: Thread?, exception: Throwable) {
    //    super.onUnhandledException(paramThread, exception)
    //    exception.printStackTrace()
    //}
}