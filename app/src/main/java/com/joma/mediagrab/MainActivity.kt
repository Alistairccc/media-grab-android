package com.joma.mediagrab

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joma.mediagrab.data.settings.LocaleContextWrapper
import com.joma.mediagrab.ui.home.HomeScreen
import com.joma.mediagrab.ui.home.HomeViewModel
import com.joma.mediagrab.ui.theme.MediaGrabTheme

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        val container = (application as MediaGrabApp).container
        setContent {
            val theme by container.themePreference.theme.collectAsStateWithLifecycle()
            val language by container.languagePreference.language.collectAsStateWithLifecycle()
            MediaGrabTheme(theme = theme) {
                val viewModel: HomeViewModel =
                    viewModel(
                        factory =
                            HomeViewModel.factory(
                                container.repository,
                                container.downloadManager,
                            ),
                    )
                HomeScreen(
                    viewModel = viewModel,
                    currentTheme = theme,
                    onThemeSelected = container.themePreference::select,
                    currentLanguage = language,
                    onLanguageSelected = { selected ->
                        if (selected != language) {
                            container.languagePreference.select(selected)
                            recreate()
                        }
                    },
                )
            }
        }
    }
}
