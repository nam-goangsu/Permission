package com.namgs.permissiopn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.namgs.permissiopn.data.IntroConfig
import com.namgs.permissiopn.ui.intro.PermissionIntroScreen
import com.namgs.permissiopn.ui.main.MainPage

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                var showMainPage by remember {
                    mutableStateOf(false)
                }

                if (showMainPage) {
                    MainPage()
                } else {
                    PermissionIntroScreen(
                        onNavigateToMain = {
                            showMainPage = true
                        },
                        slides = IntroConfig.slides,
                        permissions = IntroConfig.permissions
                    )
                }
            }
        }
    }
}