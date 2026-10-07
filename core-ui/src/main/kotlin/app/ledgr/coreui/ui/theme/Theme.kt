package app.ledgr.coreui.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Emerald,
    onPrimary = NearBlack,
    secondary = Espresso,
    onSecondary = Mist,
    tertiary = Clay,
    onTertiary = Mist,
    background = Mist,
    onBackground = Espresso,
    surface = White,
    onSurface = Espresso,
    secondaryContainer = Sand,
    onSecondaryContainer = Espresso,
    error = NegativeLight,
    onError = White
)

private val DarkColorScheme = darkColorScheme(
    primary = Emerald,
    onPrimary = NearBlack,
    secondary = EspressoLift,
    onSecondary = Mist,
    tertiary = ClayLight,
    onTertiary = Espresso,
    background = DarkBackground,
    onBackground = Mist,
    surface = DarkSurface,
    onSurface = Mist,
    secondaryContainer = EspressoLift,
    onSecondaryContainer = Sand,
    error = NegativeDark,
    onError = DarkSurface
)

data class LedgrCustomColors(
    val positive: Color,
    val negative: Color,
    val heroSurface: Color,
    val chartSand: Color,
    val chartBlush: Color,
    val chartClay: Color,
    val chartRust: Color,
    val chartEspresso: Color
)

val LocalLedgrCustomColors = staticCompositionLocalOf {
    LedgrCustomColors(
        positive = PositiveLight,
        negative = NegativeLight,
        heroSurface = Espresso,
        chartSand = Sand,
        chartBlush = Color(0xFFC5A092),
        chartClay = Clay,
        chartRust = Color(0xFF85402C),
        chartEspresso = Espresso
    )
}

val lightCustomColors = LedgrCustomColors(
    positive = PositiveLight,
    negative = NegativeLight,
    heroSurface = Espresso,
    chartSand = Sand,
    chartBlush = Color(0xFFC5A092),
    chartClay = Clay,
    chartRust = Color(0xFF85402C),
    chartEspresso = Espresso
)

val darkCustomColors = LedgrCustomColors(
    positive = PositiveDark,
    negative = NegativeDark,
    heroSurface = Espresso, // or DarkSurface if hero is standard in dark
    chartSand = Sand,
    chartBlush = Color(0xFFC5A092),
    chartClay = ClayLight,
    chartRust = Color(0xFF85402C),
    chartEspresso = Espresso
)

@Composable
fun LedgrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) darkCustomColors else lightCustomColors

    CompositionLocalProvider(
        LocalLedgrCustomColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
