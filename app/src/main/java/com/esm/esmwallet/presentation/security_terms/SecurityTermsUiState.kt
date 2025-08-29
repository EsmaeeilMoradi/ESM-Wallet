package com.esm.esmwallet.presentation.security_terms


data class SecurityTermsUiState(
    val hasAcceptedFirstTerm: Boolean = false,
    val hasAcceptedSecondTerm: Boolean = false,
    val isButtonEnabled: Boolean = false
)