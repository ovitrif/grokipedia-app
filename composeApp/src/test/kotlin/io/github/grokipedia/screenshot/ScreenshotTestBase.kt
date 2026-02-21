package io.github.grokipedia.screenshot

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import io.github.grokipedia.DarkColorScheme
import io.github.grokipedia.LightColorScheme
import io.github.grokipedia.SepiaColorScheme
import io.github.grokipedia.BlackColorScheme
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w400dp-h800dp-xxhdpi")
abstract class ScreenshotTestBase {

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    fun captureScreenshot(
        name: String,
        colorScheme: ColorScheme = DarkColorScheme,
        content: @Composable () -> Unit
    ) {
        composeTestRule.setContent {
            MaterialTheme(colorScheme = colorScheme) {
                content()
            }
        }

        composeTestRule.onRoot().captureRoboImage(
            "src/test/screenshots/$name.png"
        )
    }
}
