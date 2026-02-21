package io.github.grokipedia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FindInPageBar(
    query: String,
    onQueryChange: (String) -> Unit,
    matchCount: Int,
    currentMatch: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(52.dp)
            .background(Color(0xFF1E1E1E))
            .padding(horizontal = 8.dp)
            .testTag("find_in_page_bar"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .focusRequester(focusRequester)
                .testTag("find_input"),
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
            singleLine = true,
            cursorBrush = SolidColor(Color.White),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "Find in page",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                innerTextField()
            }
        )

        if (query.isNotEmpty()) {
            Text(
                text = if (matchCount > 0) "$currentMatch/$matchCount" else "0/0",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .testTag("find_match_count")
            )
        }

        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(36.dp).testTag("find_previous"),
            enabled = matchCount > 0
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Previous match",
                tint = if (matchCount > 0) Color.White else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = onNext,
            modifier = Modifier.size(36.dp).testTag("find_next"),
            enabled = matchCount > 0
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Next match",
                tint = if (matchCount > 0) Color.White else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.size(36.dp).testTag("find_close")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
