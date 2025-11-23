package fr.unica.fetheddine.lahjaily.vibechef.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.unica.fetheddine.lahjaily.vibechef.data.AuthRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Représente l'état d'authentification
sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Authenticated(val user: FirebaseUser) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

data class LoginFormState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null
)

class LoginViewModel(private val authRepository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _formState = MutableStateFlow(LoginFormState())
    val formState: StateFlow<LoginFormState> = _formState.asStateFlow()

    val currentUser get() = authRepository.currentUser

    init {
        // Si un utilisateur est déjà connecté
        authRepository.currentUser?.let { user ->
            _authState.value = AuthUiState.Authenticated(user)
        }
    }

    fun updateEmail(value: String) {
        _formState.update { it.copy(email = value, emailError = null) }
    }

    fun updatePassword(value: String) {
        _formState.update { it.copy(password = value, passwordError = null) }
    }

    fun togglePasswordVisibility() {
        _formState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    private fun validate(): Boolean {
        val email = _formState.value.email.trim()
        val pass = _formState.value.password
        var valid = true
        var emailErr: String? = null
        var passErr: String? = null
        if (email.isBlank() || !email.contains('@')) {
            emailErr = "Email invalide"
            valid = false
        }
        if (pass.length < 6) {
            passErr = "Mot de passe trop court (>=6)"
            valid = false
        }
        if (!valid) {
            _formState.update { it.copy(emailError = emailErr, passwordError = passErr) }
        }
        return valid
    }

    private fun getFriendlyErrorMessage(e: Throwable): String {
        return when (e) {
            is FirebaseAuthInvalidUserException -> "Ce compte n'existe pas. Veuillez créer un compte."
            is FirebaseAuthInvalidCredentialsException -> "Email ou mot de passe incorrect."
            is FirebaseAuthUserCollisionException -> "Cet email est déjà utilisé."
            else -> "Erreur de connexion. Vérifiez votre réseau ou réessayez."
        }
    }

    fun signIn() {
        if (!validate()) return
        val email = _formState.value.email.trim()
        val pass = _formState.value.password
        _authState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.signIn(email, pass)
            result.onSuccess { user ->
                _authState.value = AuthUiState.Authenticated(user)
            }.onFailure { e ->
                _authState.value = AuthUiState.Error(getFriendlyErrorMessage(e))
            }
        }
    }

    fun signUp() {
        if (!validate()) return
        val email = _formState.value.email.trim()
        val pass = _formState.value.password
        _authState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = authRepository.signUp(email, pass)
            result.onSuccess { user ->
                _authState.value = AuthUiState.Authenticated(user)
            }.onFailure { e ->
                _authState.value = AuthUiState.Error(getFriendlyErrorMessage(e))
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = AuthUiState.Idle
        _formState.value = LoginFormState()
    }
}