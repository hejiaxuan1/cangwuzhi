package com.codex.focusflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.codex.focusflow.ui.FocusFlowApp

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<FocusFlowViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusFlowApp(viewModel = viewModel)
        }
    }
}
