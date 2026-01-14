package com.rooneyandshadows.lightbulb.textinputviewdemo.fragment

import androidx.databinding.Bindable
import com.github.rooneyandshadows.lightbulb.commons.lifecycle.ObservableViewModel
import com.github.rooneyandshadows.lightbulb.textinputviewdemo.BR

class HomeVM : ObservableViewModel() {
    @get:Bindable
    var text: String = "asfasfsf"
        set(value) {
            if (text != value) {
                field = value
                notifyPropertyChanged(BR.text)
            }
        }
}

