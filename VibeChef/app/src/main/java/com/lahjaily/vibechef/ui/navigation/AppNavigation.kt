package com.lahjaily.vibechef.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lahjaily.vibechef.R
import com.lahjaily.vibechef.ui.*
import com.lahjaily.vibechef.ui.viewmodel.LoginViewModel
import com.lahjaily.vibechef.ui.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Profile : Screen("profile")
    data object Detail : Screen("detail")
}

private data class BottomNavItem(
    val screen: Screen,
    val labelResId: Int,
    val icon: ImageVector
)

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    vibeChefViewModel: MainViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = remember {
        listOf(
            BottomNavItem(Screen.Home, R.string.nav_home, Icons.Filled.Home),
            BottomNavItem(Screen.History, R.string.nav_history, Icons.Filled.History),
            BottomNavItem(Screen.Profile, R.string.nav_profile, Icons.Filled.Person)
        )
    }
    val bottomNavRoutes = remember { bottomNavItems.map { it.screen.route }.toSet() }
    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.screen.route,
                            onClick = {
                                if (currentRoute != item.screen.route) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(item.icon, contentDescription = stringResource(item.labelResId))
                            },
                            label = { Text(stringResource(item.labelResId)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(300)
                )
            }
        ) {
            // ── Splash ──
            composable(
                route = Screen.Splash.route,
                enterTransition = { fadeIn(tween(400)) },
                exitTransition = { fadeOut(tween(400)) }
            ) {
                SplashScreen(
                    onSplashFinished = {
                        val nextRoute = when {
                            !vibeChefViewModel.hasSeenOnboarding -> Screen.Onboarding.route
                            loginViewModel.currentUser != null -> Screen.Home.route
                            else -> Screen.Login.route
                        }
                        navController.navigate(nextRoute) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Onboarding ──
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinished = {
                        vibeChefViewModel.markOnboardingSeen()
                        val nextRoute =
                            if (loginViewModel.currentUser != null) Screen.Home.route
                            else Screen.Login.route
                        navController.navigate(nextRoute) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Login ──
            composable(Screen.Login.route) {
                AuthScreen(
                    loginViewModel = loginViewModel,
                    onAuthenticated = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // ── Home (bottom nav tab) ──
            composable(
                route = Screen.Home.route,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }
            ) {
                val user = loginViewModel.currentUser
                if (user != null) {
                    VibeChefScreen(
                        viewModel = vibeChefViewModel,
                        userId = user.uid
                    )
                } else {
                    LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) }
                }
            }

            // ── History (bottom nav tab) ──
            composable(
                route = Screen.History.route,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }
            ) {
                val user = loginViewModel.currentUser
                if (user != null) {
                    HistoryScreen(
                        viewModel = vibeChefViewModel,
                        userId = user.uid,
                        onRecipeClick = { recipe ->
                            vibeChefViewModel.selectRecipe(recipe)
                            navController.navigate(Screen.Detail.route)
                        }
                    )
                }
            }

            // ── Profile (bottom nav tab) ──
            composable(
                route = Screen.Profile.route,
                enterTransition = { fadeIn(tween(300)) },
                exitTransition = { fadeOut(tween(300)) }
            ) {
                ProfileScreen(
                    loginViewModel = loginViewModel,
                    mainViewModel = vibeChefViewModel,
                    onSignOut = {
                        loginViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                        }
                    },
                    onCacheCleared = {
                        loginViewModel.currentUser?.uid?.let { uid ->
                            vibeChefViewModel.clearCache(uid)
                        }
                    }
                )
            }

            // ── Recipe Detail ──
            composable(Screen.Detail.route) {
                val selected by vibeChefViewModel.selectedRecipe.collectAsState()
                val user = loginViewModel.currentUser
                if (selected != null) {
                    RecipeDetailScreen(
                        recipe = selected!!,
                        onBack = { navController.popBackStack() },
                        onDelete = if (user != null) {
                            {
                                vibeChefViewModel.deleteRecipe(user.uid, selected!!.id)
                                navController.popBackStack()
                            }
                        } else null
                    )
                } else {
                    Text(stringResource(R.string.detail_no_recipe))
                }
            }
        }
    }
}