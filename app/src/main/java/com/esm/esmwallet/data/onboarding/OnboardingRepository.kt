package com.esm.esmwallet.data.onboarding

import com.esm.esmwallet.presentation.onboarding.OnboardingPage
import kotlinx.coroutines.flow.Flow

/**
 * Interface for the Onboarding data source.
 * It defines the contract for providing onboarding pages.
 */
interface OnboardingRepository {
    // Corrected the method name to be plural for clarity, as it returns a list of pages.
    fun getOnboardingPages(): Flow<List<OnboardingPage>>
}
