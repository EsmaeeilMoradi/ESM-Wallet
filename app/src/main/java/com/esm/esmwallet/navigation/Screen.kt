package com.esm.esmwallet.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome_screen")
    object CreateWallet : Screen("create_wallet_screen")
    object ImportWallet : Screen("import_wallet_screen")

    object Home : Screen("home")
    object Trending : Screen("trending")
    object Swap : Screen("swap")
    object Earn : Screen("earn")
    object Discover : Screen("discover")
    object History : Screen("history_screen")

    object Send : Screen("send_screen")
    object TokenSelection : Screen("token_selection_screen")
    object ImportToken : Screen("import_token")
}