package com.example.sinalize

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.sinalize.navigation.AppNavigation
import com.example.sinalize.ui.theme.SinalizeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SinalizeTheme {
                AppNavigation()
            }
        }
    }
}
