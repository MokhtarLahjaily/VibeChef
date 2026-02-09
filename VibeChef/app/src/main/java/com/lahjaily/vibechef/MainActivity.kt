package com.lahjaily.vibechef

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.lahjaily.vibechef.ui.navigation.AppNavigation
import com.lahjaily.vibechef.ui.viewmodel.LoginViewModel
import com.lahjaily.vibechef.ui.viewmodel.MainViewModel as VibeChefViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lahjaily.vibechef.ui.theme.VibeChefTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val loginViewModel: LoginViewModel = viewModel()
            val vibeChefViewModel: VibeChefViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return VibeChefViewModel(application) as T
                    }
                }
            )
            val isDark by vibeChefViewModel.isDarkMode.collectAsState()
            VibeChefTheme(darkTheme = isDark) {
                AppNavigation(
                    loginViewModel = loginViewModel,
                    vibeChefViewModel = vibeChefViewModel
                )
            }
        }
    }
}
