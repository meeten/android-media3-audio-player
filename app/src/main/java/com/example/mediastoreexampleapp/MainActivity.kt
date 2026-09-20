package com.example.mediastoreexampleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mediastoreexampleapp.presentation.tracks.TracksScreen
import com.example.mediastoreexampleapp.ui.theme.MediaStoreExampleAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaStoreExampleAppTheme {
                TracksScreen()
            }
        }
    }
}