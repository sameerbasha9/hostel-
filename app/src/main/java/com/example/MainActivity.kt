package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.HostelToast
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CustomerDashboard
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.OwnerDashboard
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.HostelViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: HostelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val toastState by viewModel.toastState.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // High-fidelity state-driven animated screen navigator
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(250))
                            },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                Screen.LANDING -> {
                                    LandingScreen(
                                        viewModel = viewModel,
                                        onNavigateToLogin = { viewModel.navigateTo(Screen.LOGIN) },
                                        onNavigateToRegister = { viewModel.navigateTo(Screen.REGISTER) }
                                    )
                                }
                                Screen.LOGIN -> {
                                    AuthScreen(
                                        viewModel = viewModel,
                                        initialIsRegister = false,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                                Screen.REGISTER -> {
                                    AuthScreen(
                                        viewModel = viewModel,
                                        initialIsRegister = true,
                                        onBack = { viewModel.navigateBack() }
                                    )
                                }
                                Screen.OWNER_DASHBOARD -> {
                                    OwnerDashboard(
                                        viewModel = viewModel,
                                        onLogout = { viewModel.logout() }
                                    )
                                }
                                Screen.CUSTOMER_DASHBOARD -> {
                                    CustomerDashboard(
                                        viewModel = viewModel,
                                        onLogout = { viewModel.logout() }
                                    )
                                }
                            }
                        }

                        // App-wide custom Toast / Feedback banner system
                        toastState?.let { (msg, isSuccess) ->
                            HostelToast(
                                message = msg,
                                isSuccess = isSuccess,
                                onDismiss = { viewModel.dismissToast() },
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
