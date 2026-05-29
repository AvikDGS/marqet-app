package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.AppBackgroundGradient
import com.example.GlassBackground
import com.example.GlassBorder
import com.example.TextSecondaryColor
import com.example.BottomNavBg
import com.example.viewmodel.AuthUiState
import com.example.viewmodel.AuthViewModel

@Composable
fun WelcomeScreen(
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(AppBackgroundGradient)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription=null, tint=Color.White, modifier=Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Marqet", 
                style = androidx.compose.ui.text.TextStyle(
                    brush = Brush.linearGradient(listOf(Color.White, Color.LightGray)),
                    fontSize = 42.sp, 
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier=Modifier.height(16.dp))
            Text(
                "Markets. News. Intelligence.",
                color = TextSecondaryColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(64.dp))
            
            Button(
                onClick = onSignUpClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Get Started", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.clickable { onSignInClick() }.padding(8.dp)) {
                Text("Already have an account? ", color = TextSecondaryColor)
                Text("Sign In", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    isSignUp: Boolean,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    onToggleMode: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            viewModel.resetState()
            onSuccess()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(AppBackgroundGradient).padding(padding)) {
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(scroll),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    if (isSignUp) "Create Account" else "Welcome Back",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    if (isSignUp) "Subscribe to Marqet to get personalized insights." else "Sign in to pick up where you left off.",
                    color = TextSecondaryColor,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                if (authState is AuthUiState.Error) {
                    Text((authState as AuthUiState.Error).message, color = Color.Red, modifier = Modifier.padding(bottom=16.dp))
                }

                if (isSignUp) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name", color = TextSecondaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = GlassBorder,
                            cursorColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = TextSecondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = GlassBorder,
                        cursorColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = TextSecondaryColor) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = GlassBorder,
                        cursorColor = Color.White
                    ),
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle Password", tint=TextSecondaryColor)
                        }
                    }
                )

                if (!isSignUp) {
                    Text(
                        "Forgot Password?",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.End).padding(top = 16.dp).clickable { 
                            if (email.isNotBlank()) viewModel.resetPassword(email)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        if (isSignUp) viewModel.signUp(name, email, password) else viewModel.signIn(email, password)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, disabledContainerColor = Color.DarkGray),
                    shape = RoundedCornerShape(16.dp),
                    enabled = authState !is AuthUiState.Loading && email.isNotBlank() && password.isNotBlank()
                ) {
                    if (authState is AuthUiState.Loading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (isSignUp) "Create Account" else "Sign In", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = GlassBorder)
                    Text(" OR ", color = TextSecondaryColor, modifier = Modifier.padding(horizontal = 8.dp))
                    HorizontalDivider(modifier = Modifier.weight(1f), color = GlassBorder)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { viewModel.signInWithGoogle() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Continue with Google", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Continue with Apple", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(modifier = Modifier.clickable { onToggleMode() }.padding(8.dp)) {
                    Text(if (isSignUp) "Already have an account? " else "Don't have an account? ", color = TextSecondaryColor)
                    Text(if (isSignUp) "Sign In" else "Sign Up", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                if (isSignUp) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "By continuing, you agree to Marqet's Terms of Service and Privacy Policy.",
                        color = TextSecondaryColor,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
