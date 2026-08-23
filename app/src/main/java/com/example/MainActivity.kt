package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.screens.CncMainScreen
import com.example.ui.theme.CncBackground
import com.example.ui.theme.LinuxCncTheme
import com.example.viewmodel.CncViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CncViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LinuxCncTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CncBackground
                ) {
                    CncMainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
