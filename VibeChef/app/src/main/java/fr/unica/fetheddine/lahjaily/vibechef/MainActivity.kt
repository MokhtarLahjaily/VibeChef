package fr.unica.fetheddine.lahjaily.vibechef

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.unica.fetheddine.lahjaily.vibechef.ui.theme.VibeChefTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VibeChefTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val recipeViewModel: RecipeViewModel = viewModel()
                    val uiState by recipeViewModel.uiState.collectAsStateWithLifecycle()

                    RecipeScreen(
                        uiState = uiState,
                        onGenerateRecipe = { prompt ->
                            recipeViewModel.generateRecipe(prompt)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeScreen(
    uiState: RecipeUiState,
    onGenerateRecipe: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var promptInput by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            label = { Text("Enter ingredients or preferences (e.g., 'chicken, rice, easy')") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onGenerateRecipe(promptInput) },
            enabled = promptInput.isNotBlank() && uiState !is RecipeUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate Recipe")
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            RecipeUiState.Initial -> {
                Text("Enter ingredients and generate your recipe!")
            }
            RecipeUiState.Loading -> {
                CircularProgressIndicator()
            }
            is RecipeUiState.Success -> {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = uiState.output,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is RecipeUiState.Error -> {
                Text(
                    text = "Error: ${uiState.error}",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeScreenPreview() {
    VibeChefTheme {
        RecipeScreen(
            uiState = RecipeUiState.Initial,
            onGenerateRecipe = {}
        )
    }
}