package com.lahjaily.vibechef

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import com.lahjaily.vibechef.ui.navigation.AppNavigation
import com.lahjaily.vibechef.ui.viewmodel.LoginViewModel
import com.lahjaily.vibechef.ui.viewmodel.MainViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lahjaily.vibechef.ui.theme.VibeChefTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val loginViewModel: LoginViewModel = hiltViewModel()
            val vibeChefViewModel: MainViewModel = hiltViewModel()
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