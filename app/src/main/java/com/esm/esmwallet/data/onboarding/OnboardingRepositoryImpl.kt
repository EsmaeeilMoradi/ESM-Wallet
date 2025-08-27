package com.esm.esmwallet.data.onboarding

import com.esm.esmwallet.presentation.onboarding.OnboardingData
import com.esm.esmwallet.presentation.onboarding.OnboardingPage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A fake implementation of the OnboardingRepository for development and testing.
 * It provides static, hardcoded data.
 */
class OnboardingRepositoryImpl : OnboardingRepository {
    override fun getOnboardingPages(): Flow<List<OnboardingPage>> = flow {
        // Here we emit a static list of Onboarding pages as a Flow.
        // The resource IDs are placeholders that you should replace with your own.
        emit(
            OnboardingData.pages
        )
    }
}