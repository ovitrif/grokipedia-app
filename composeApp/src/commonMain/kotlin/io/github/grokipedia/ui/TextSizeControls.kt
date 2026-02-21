package io.github.grokipedia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextSizeControls(
    currentPercent: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = Color(0xFF2A2A2A),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("text_size_controls"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = onDecrease,
            modifier = Modifier.size(32.dp).testTag("text_size_decrease"),
            enabled = currentPercent > 80,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = Color.White,
                disabledContentColor = Color.Gray
            )
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease text size",
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            text = "$currentPercent%",
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.testTag("text_size_label")
        )

        IconButton(
            onClick = onIncrease,
            modifier = Modifier.size(32.dp).testTag("text_size_increase"),
            enabled = currentPercent < 160,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = Color.White,
                disabledContentColor = Color.Gray
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase text size",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
