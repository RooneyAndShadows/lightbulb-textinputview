package com.rooneyandshadows.lightbulb.textinputviewdemo.fragment

import android.os.Bundle
import android.view.View
import com.github.rooneyandshadows.lightbulb.application.fragment.base.BaseFragment
import com.github.rooneyandshadows.lightbulb.application.fragment.cofiguration.ActionBarConfiguration
import com.github.rooneyandshadows.lightbulb.apt.annotations.FragmentScreen
import com.github.rooneyandshadows.lightbulb.apt.annotations.FragmentViewBinding
import com.github.rooneyandshadows.lightbulb.apt.annotations.FragmentViewModel
import com.github.rooneyandshadows.lightbulb.apt.annotations.LightbulbFragment
import com.github.rooneyandshadows.lightbulb.textinputviewdemo.R
import com.github.rooneyandshadows.lightbulb.textinputviewdemo.databinding.FragmentHomeBinding

@FragmentScreen(screenName = "Home", screenGroup = "Demo")
@LightbulbFragment(layoutName = "fragment_home")
class HomeFragment : BaseFragment() {
    @FragmentViewBinding(layoutName = "fragment_home")
    lateinit var viewBinding: FragmentHomeBinding

    @FragmentViewModel
    lateinit var viewModel: HomeVM

    @Override
    override fun configureActionBar(): ActionBarConfiguration {
        return ActionBarConfiguration(R.id.toolbar)
            .withActionButtons(true)
            .attachToDrawer(false)
            .withSubTitle("Sfasf")
            .withTitle("ASfasfasf")
    }

    @Override
    override fun onViewCreated(fragmentView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(fragmentView, savedInstanceState)
        viewBinding.vm = viewModel


    }
}