package com.rooneyandshadows.lightbulb.textinputviewdemo

import android.app.Application
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbApplication
import com.github.rooneyandshadows.lightbulb.textinputviewdemo.R
import com.google.android.material.color.DynamicColors

@LightbulbApplication
class App : Application(){
    override fun onCreate() {
        super.onCreate()
        //DynamicColors.applyToActivitiesIfAvailable(this)
       //setTheme(R.style.DemoTheme)
    }
}
