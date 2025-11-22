package fr.unica.fetheddine.lahjaily.vibechef

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import fr.unica.fetheddine.lahjaily.vibechef.ui.VibeChefScreen
import fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel.MainViewModel as VibeChefViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = VibeChefViewModel()
        setContent {
            VibeChefScreen(viewModel = viewModel)
        }
    }
}
