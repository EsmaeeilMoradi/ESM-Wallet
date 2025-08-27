package com.esm.esmwallet.presentation.onboarding

// This sealed class encapsulates all possible navigation events
// that the OnboardingViewModel might send.
sealed class NavigationEvent {
    object NavigateToWelcomeScreen : NavigationEvent()
    // You can add more events here in the future, like:
    // object NavigateToLoginScreen : NavigationEvent()
}