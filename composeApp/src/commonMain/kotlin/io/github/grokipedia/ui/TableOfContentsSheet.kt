package io.github.grokipedia.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TocItem(
    val text: String,
    val level: Int,
    val elementId: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableOfContentsSheet(
    items: List<TocItem>,
    onItemClick: (TocItem) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF1E1E1E),
        modifier = Modifier.testTag("toc_sheet")
    ) {
        Column(
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = "Table of Contents",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(items) { index, item ->
                    val indent = ((item.level - 1).coerceAtLeast(0)) * 16
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (item.level <= 2) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = when (item.level) {
                                1 -> 16.sp
                                2 -> 15.sp
                                else -> 14.sp
                            }
                        ),
                        color = if (item.level <= 2) Color.White else Color(0xFFB0B0B0),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(item) }
                            .padding(
                                start = (16 + indent).dp,
                                end = 16.dp,
                                top = 10.dp,
                                bottom = 10.dp
                            )
                            .testTag("toc_item_$index")
                    )
                }
            }
        }
    }
}
