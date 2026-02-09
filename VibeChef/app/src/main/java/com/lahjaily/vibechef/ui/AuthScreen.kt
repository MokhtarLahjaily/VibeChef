package com.lahjaily.vibechef.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.lahjaily.vibechef.R
import com.lahjaily.vibechef.ui.viewmodel.AuthUiState
import com.lahjaily.vibechef.ui.viewmodel.LoginViewModel

import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(loginViewModel: LoginViewModel, onAuthenticated: () -> Unit) {
    val authState by loginViewModel.authState.collectAsState()
    val formState by loginViewModel.formState.collectAsState()
    var isSignUpMode by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Authenticated) {
            onAuthenticated()
        } else if (authState is AuthUiState.Error) {
            snackbarHostState.showSnackbar((authState as AuthUiState.Error).message)
        }
    }

    // Forgot password dialog
    if (showForgotPassword) {
        var resetEmail by remember { mutableStateOf(formState.email) }
        AlertDialog(
            onDismissRequest = { showForgotPassword = false },
            title = { Text(stringResource(R.string.forgot_password_title)) },
            text = {
                Column {
                    Text(
                        stringResource(R.string.forgot_password_desc),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text(stringResource(R.string.label_email)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    loginViewModel.resetPassword(resetEmail) { success, message ->
                        showForgotPassword = false
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    }
                }) {
                    Text(stringResource(R.string.forgot_password_send))
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPassword = false }) {
                    Text(stringResource(R.string.delete_confirm_no))
                }
            }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(id = R.string.title_app),
                modifier = Modifier.size(120.dp)
            )
            Text(
                text = if (isSignUpMode) stringResource(R.string.signup_title) else stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = if (isSignUpMode) stringResource(R.string.signup_subtitle) else stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            // Email field
            OutlinedTextField(
                value = formState.email,
                onValueChange = loginViewModel::updateEmail,
                label = { Text(stringResource(R.string.label_email)) },
                isError = formState.emailError != null,
                supportingText = { formState.emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            // Password field
            OutlinedTextField(
                value = formState.password,
                onValueChange = loginViewModel::updatePassword,
                label = { Text(stringResource(R.string.label_password)) },
                isError = formState.passwordError != null,
                supportingText = { formState.passwordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true,
                visualTransformation = if (formState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { loginViewModel.togglePasswordVisibility() }) {
                        Icon(
                            imageVector = if (formState.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (formState.isPasswordVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            // Action button
            Button(
                onClick = {
                    keyboardController?.hide()
                    if (isSignUpMode) loginViewModel.signUp() else loginViewModel.signIn()
                },
                enabled = authState !is AuthUiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (authState is AuthUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                }
                   Text(text = if (isSignUpMode) stringResource(R.string.auth_action_sign_up) else stringResource(R.string.auth_action_sign_in))
            }

            // Toggle link
            Text(
                text = if (isSignUpMode) stringResource(R.string.prompt_have_account) else stringResource(R.string.prompt_no_account),
                modifier = Modifier.clickable { isSignUpMode = !isSignUpMode },
                color = MaterialTheme.colorScheme.primary
            )

            // Extra: forgot password
            if (!isSignUpMode) {
                Text(
                    text = stringResource(R.string.action_forgot_password),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { showForgotPassword = true },
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
        }
    }
}
