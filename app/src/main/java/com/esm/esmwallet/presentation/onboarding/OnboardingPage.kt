package com.esm.esmwallet.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.esm.esmwallet.R

/**
 * Defines a single page of the onboarding flow.
 * Each page has an image, a title, and a description.
 */
data class OnboardingPage(
    @DrawableRes val imageResId: Int,
    @StringRes val titleResId: Int,
    @StringRes val descriptionResId: Int
)


// Object Class as a static data provider
object OnboardingData {
    val pages = listOf(
        OnboardingPage(
            imageResId = R.drawable.ic_independence,
            titleResId = R.string.Onboarding_Wallet_Screen2Title,
            descriptionResId = R.string.Onboarding_Wallet_Screen2Description
        ),
        OnboardingPage(
            imageResId = R.drawable.ic_knowledge,
            titleResId = R.string.Onboarding_Wallet_Screen3Title,
            descriptionResId = R.string.Onboarding_Wallet_Screen3Description
        ),
        OnboardingPage(
            imageResId = R.drawable.ic_privacy,
            titleResId = R.string.Onboarding_Wallet_Screen4Title,
            descriptionResId = R.string.Onboarding_Wallet_Screen4Description
        )
    )
}