package fr.unica.fetheddine.lahjaily.vibechef

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.unica.fetheddine.lahjaily.vibechef.ui.navigation.AppNavigation
import fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel.LoginViewModel
import fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel.MainViewModel as VibeChefViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val loginViewModel: LoginViewModel = viewModel()
            val vibeChefViewModel: VibeChefViewModel = viewModel()

            AppNavigation(
                loginViewModel = loginViewModel,
                vibeChefViewModel = vibeChefViewModel
            )
        }
    }
}
