package com.lahjaily.vibechef.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lahjaily.vibechef.R
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val titleRes: Int,
    val descriptionRes: Int
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pages = listOf(
        OnboardingPage("🍳", R.string.onboarding_title_1, R.string.onboarding_desc_1),
        OnboardingPage("📸", R.string.onboarding_title_2, R.string.onboarding_desc_2),
        OnboardingPage("🎨", R.string.onboarding_title_3, R.string.onboarding_desc_3),
        OnboardingPage("💾", R.string.onboarding_title_4, R.string.onboarding_desc_4)
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        // Skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onFinished) {
                Text(stringResource(R.string.onboarding_skip))
            }
        }

        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = pages[page].emoji,
                    fontSize = 80.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = stringResource(pages[page].titleRes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(pages[page].descriptionRes),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        // Page indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (pagerState.currentPage == index) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                )
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedVisibility(
                visible = pagerState.currentPage > 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                OutlinedButton(onClick = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                }) {
                    Text(stringResource(R.string.onboarding_previous))
                }
            }
            if (pagerState.currentPage == 0) Spacer(modifier = Modifier.weight(1f))

            if (pagerState.currentPage == pages.size - 1) {
                Button(onClick = onFinished) {
                    Text(stringResource(R.string.onboarding_get_started))
                }
            } else {
                Button(onClick = {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }) {
                    Text(stringResource(R.string.onboarding_next))
                }
            }
        }
    }
}
