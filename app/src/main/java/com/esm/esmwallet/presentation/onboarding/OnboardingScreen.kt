package com.esm.esmwallet.presentation.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.esm.esmwallet.navigation.Screen
import com.esm.esmwallet.ui.component.RadialBackground
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { uiState.pages.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = uiState.currentPageIndex) {
        pagerState.scrollToPage(uiState.currentPageIndex)
    }
    // Use a LaunchedEffect to react to changes in the pager state and send them to the ViewModel.

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }
    // 💡 A LaunchedEffect to listen for navigation events from the ViewModel.
    LaunchedEffect(key1 = Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToWelcomeScreen -> {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Place the RadialBackground here to act as the bottom-most layer
        RadialBackground()
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = true // Allow the user to scroll manually

            ) { pageIndex ->
                OnboardingPageContent(
                    page = uiState.pages[pageIndex]
                )
            }
            Button(
                // The onClick event is what drives the ViewModel's state.
                onClick = {
                    // We use coroutineScope to launch the page change request
                    coroutineScope.launch {
                        if (uiState.isFinalPage) {
                            // If it's the last page, send the navigation event
                            viewModel.onNextClicked()
                        } else {
                            // Otherwise, move to the next page
                            pagerState.scrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                AnimatedContent(
                    targetState = uiState.isFinalPage,
                    transitionSpec = {
                        slideInVertically(
                            animationSpec = tween(500),
                            initialOffsetY = { it }) togetherWith
                                slideOutVertically(
                                    animationSpec = tween(500),
                                    targetOffsetY = { -it })
                    }, label = "Onboarding Button"
                ) { isFinalPage ->
                    Text(text = if (isFinalPage) "Finish" else "Next")
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imageResId),
            contentDescription = null,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Text(
            text = stringResource(id = page.titleResId),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = stringResource(id = page.descriptionResId),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}



