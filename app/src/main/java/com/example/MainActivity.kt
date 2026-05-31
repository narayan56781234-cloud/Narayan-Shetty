package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.local.AppDatabase
import com.example.data.repository.AlagzaRepository
import com.example.ui.AlagzaApp
import com.example.ui.AlagzaViewModel
import com.example.ui.AlagzaViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core Offline Local Cache Instantiation
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AlagzaRepository(database.localDao())
        val factory = AlagzaViewModelFactory(application, repository)

        setContent {
            MyApplicationTheme {
                // Initialize clean architectural View Model
                val viewModel: AlagzaViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = factory
                )
                AlagzaApp(viewModel = viewModel)
            }
        }
    }
}
