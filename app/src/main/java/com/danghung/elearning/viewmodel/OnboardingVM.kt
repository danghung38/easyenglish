package com.danghung.elearning.viewmodel

import com.danghung.elearning.model.OnboardingItem

class OnboardingVM: BaseViewModel() {
    private val onboardingItems: MutableList<OnboardingItem> = ArrayList<OnboardingItem>()
    fun getOnboardingItems(): MutableList<OnboardingItem> {
        return onboardingItems
    }
}