package com.joma.mediagrab

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joma.mediagrab.ui.home.HomeScreen
import com.joma.mediagrab.ui.home.HomeViewModel
import com.joma.mediagrab.ui.theme.MediaGrabTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        val container = (application as MediaGrabApp).container
        setContent {
            MediaGrabTheme {
                val viewModel: HomeViewModel =
                    viewModel(
                        factory =
                            HomeViewModel.factory(
                                container.repository,
                                container.downloadManager,
                            ),
                    )
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
