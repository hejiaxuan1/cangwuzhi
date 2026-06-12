package com.codex.focusflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.codex.focusflow.AppSettings
import com.codex.focusflow.ThemeMode
import com.codex.focusflow.UiStylePreset

private val SpaceLight = lightColorScheme(
    primary = Color(0xFF4E6FAE),
    secondary = Color(0xFF7A5CA8),
    tertiary = Color(0xFFB77C32),
    background = Color(0xFFF8FAFC),
    surface = Color(0xEFFFFFFF),
    surfaceVariant = Color(0xFFE8ECF4),
    onSurface = Color(0xFF111827),
    onBackground = Color(0xFF111827)
)

private val SpaceDark = darkColorScheme(
    primary = Color(0xFF9DBBFF),
    secondary = Color(0xFFC6A8FF),
    tertiary = Color(0xFFFFD36F),
    background = Color(0xFF0B0F17),
    surface = Color(0xFF17202B),
    surfaceVariant = Color(0xFF263142),
    onSurface = Color(0xFFF8FAFC),
    onBackground = Color(0xFFF8FAFC)
)

@Composable
fun FocusFlowTheme(
    settings: AppSettings,
    content: @Composable () -> Unit
) {
    val darkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = inventoryColorScheme(settings.uiStylePreset, darkTheme),
        content = content
    )
}

private fun inventoryColorScheme(stylePreset: UiStylePreset, darkTheme: Boolean): ColorScheme {
    return when (stylePreset) {
        UiStylePreset.SPACE -> if (darkTheme) SpaceDark else SpaceLight
        UiStylePreset.PEARL -> if (darkTheme) {
            SpaceDark.copy(
                primary = Color(0xFFD7E3FF),
                secondary = Color(0xFFE5D7FF),
                tertiary = Color(0xFFFFDFA8),
                background = Color(0xFF111318),
                surface = Color(0xFF1B1E27),
                surfaceVariant = Color(0xFF2A2E3A)
            )
        } else {
            SpaceLight.copy(
                primary = Color(0xFF385C96),
                secondary = Color(0xFF6D6387),
                tertiary = Color(0xFF9A6B20),
                background = Color(0xFFF8FAFC),
                surface = Color(0xF2FFFFFF),
                surfaceVariant = Color(0xFFE9EEF5)
            )
        }
        UiStylePreset.AURORA -> if (darkTheme) {
            SpaceDark.copy(
                primary = Color(0xFF7AA7FF),
                secondary = Color(0xFFB889FF),
                tertiary = Color(0xFFFFD36F),
                background = Color(0xFF0F172A),
                surface = Color(0xFF18243A),
                surfaceVariant = Color(0xFF263752)
            )
        } else {
            SpaceLight.copy(
                primary = Color(0xFF4169E1),
                secondary = Color(0xFF8E5AD8),
                tertiary = Color(0xFFC4862B),
                background = Color(0xFFF7F8FD),
                surface = Color(0xEFFFFFFF)
            )
        }
        UiStylePreset.HELLO_KITTY -> if (darkTheme) {
            SpaceDark.copy(
                primary = Color(0xFFFF9EC8),
                secondary = Color(0xFFFFD7E8),
                tertiary = Color(0xFFFFF0A6),
                background = Color(0xFF1A1018),
                surface = Color(0xFF2A1A25),
                surfaceVariant = Color(0xFF3B2433)
            )
        } else {
            SpaceLight.copy(
                primary = Color(0xFFE64D86),
                secondary = Color(0xFFFF8FBA),
                tertiary = Color(0xFFD29B2D),
                background = Color(0xFFFFF7FB),
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFFFE4EF)
            )
        }
        UiStylePreset.NOTION -> if (darkTheme) {
            SpaceDark.copy(
                primary = Color(0xFFEDE7DB),
                secondary = Color(0xFFB9B0A2),
                tertiary = Color(0xFFD2A65A),
                background = Color(0xFF141412),
                surface = Color(0xFF1F1F1D),
                surfaceVariant = Color(0xFF30302C)
            )
        } else {
            SpaceLight.copy(
                primary = Color(0xFF2F3437),
                secondary = Color(0xFF6B6F73),
                tertiary = Color(0xFFB7892F),
                background = Color(0xFFFBFAF8),
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFEDEBE7)
            )
        }
    }
}
