package fr.unica.fetheddine.lahjaily.vibechef

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import fr.unica.fetheddine.lahjaily.vibechef.ui.VibeChefScreen
import fr.unica.fetheddine.lahjaily.vibechef.ui.AuthScreen
import fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel.LoginViewModel
import fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel.AuthUiState
import fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel.MainViewModel as VibeChefViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recipeViewModel = VibeChefViewModel()
        val loginViewModel = LoginViewModel()
        setContent {
            val authState by loginViewModel.authState.collectAsState()
            when (authState) {
                is AuthUiState.Authenticated -> {
                    // Scaffold avec possibilité de déconnexion
                    VibeChefScreen(
                        viewModel = recipeViewModel,
                        onSignOut = { loginViewModel.signOut() }
                    )
                }
                AuthUiState.Idle, is AuthUiState.Error, AuthUiState.Loading -> {
                    AuthScreen(loginViewModel = loginViewModel, onAuthenticated = { /* rien ici, recomposition gère */ })
                }
            }
        }
    }
}
