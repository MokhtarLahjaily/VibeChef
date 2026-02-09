package fr.unica.fetheddine.lahjaily.vibechef.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.unica.fetheddine.lahjaily.vibechef.R
import fr.unica.fetheddine.lahjaily.vibechef.ui.AuthScreen
import fr.unica.fetheddine.lahjaily.vibechef.ui.HistoryScreen
import fr.unica.fetheddine.lahjaily.vibechef.ui.RecipeDetailScreen
import fr.unica.fetheddine.lahjaily.vibechef.ui.VibeChefScreen
import fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel.LoginViewModel
import fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object History : Screen("history")
    data object Detail : Screen("detail")
}

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    vibeChefViewModel: MainViewModel
) {
    val navController = rememberNavController()
    val startDestination = if (loginViewModel.currentUser != null) Screen.Home.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {
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
        composable(Screen.Home.route) {
            // On récupère l'utilisateur courant (non null ici car protégé par la navigation)
            val user = loginViewModel.currentUser
            if (user != null) {
                VibeChefScreen(
                    viewModel = vibeChefViewModel,
                    userId = user.uid,
                    onSignOut = {
                        loginViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route)
                    }
                )
            } else {
                // Fallback si jamais user est null (ne devrait pas arriver)
                LaunchedEffect(Unit) { navController.navigate(Screen.Login.route) }
            }
        }
        composable(Screen.History.route) {
            val user = loginViewModel.currentUser
            if (user != null) {
                HistoryScreen(
                    viewModel = vibeChefViewModel,
                    userId = user.uid,
                    onBack = { navController.popBackStack() },
                    onRecipeClick = { recipe ->
                        vibeChefViewModel.selectRecipe(recipe)
                        navController.navigate(Screen.Detail.route)
                    }
                )
            }
        }
        composable(Screen.Detail.route) {
            val selected by vibeChefViewModel.selectedRecipe.collectAsState()
            val user = loginViewModel.currentUser
            if (selected != null) {
                RecipeDetailScreen(
                    recipe = selected!!,
                    onBack = { navController.popBackStack() },
                    onDelete = if (user != null) {
                        { vibeChefViewModel.deleteRecipe(user.uid, selected!!.id) }
                    } else null
                )
            } else {
                Text(stringResource(R.string.detail_no_recipe))
            }
        }
    }
}
