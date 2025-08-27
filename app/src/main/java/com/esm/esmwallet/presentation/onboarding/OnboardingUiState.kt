package com.esm.esmwallet.presentation.onboarding

data class OnboardingUiState(
    val pages: List<OnboardingPage>,
    val currentPageIndex: Int = 0,
    val isFinalPage: Boolean = false
) {
    companion object {
        fun initial() = OnboardingUiState(
            // We initialize with an empty list. The ViewModel will fill it.
            pages = emptyList()
        )
    }

}