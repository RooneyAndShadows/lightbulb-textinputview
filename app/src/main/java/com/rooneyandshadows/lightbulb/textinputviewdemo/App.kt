package com.rooneyandshadows.lightbulb.textinputviewdemo

import android.app.Application
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbApplication
import com.github.rooneyandshadows.lightbulb.textinputviewdemo.R

@LightbulbApplication
class App : Application(){
    override fun onCreate() {
        super.onCreate()
       setTheme(R.style.DemoTheme)
    }
}
