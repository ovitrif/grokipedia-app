package io.github.grokipedia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.hazeEffect
import io.github.grokipedia.data.HistoryItem
import io.github.grokipedia.data.ReadingHistoryRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    repository: ReadingHistoryRepository,
    onNavigateBack: () -> Unit,
    onPageClick: (String) -> Unit
) {
    val historyItems by repository.history.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }
    val snackbarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
            .testTag("history_screen")
    ) {
        // Content with haze source
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 56.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (historyItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                            .testTag("history_empty_state"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No browsing history yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                itemsIndexed(historyItems, key = { _, item -> item.url + item.timestamp }) { index, item ->
                    HistoryItemRow(
                        item = item,
                        index = index,
                        onPageClick = { onPageClick(item.url) },
                        onDeleteClick = {
                            scope.launch {
                                repository.removeItem(item.url)
                                snackbarHostState.showSnackbar("Item removed")
                            }
                        }
                    )
                }
            }
        }

        // Topbar with haze blur
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(56.dp)
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = Color(0xFF141414),
                        blurRadius = 20.dp,
                        tint = HazeTint(color = Color(0xFF141414).copy(alpha = 0.7f))
                    )
                )
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "History",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            if (historyItems.isNotEmpty()) {
                IconButton(
                    onClick = {
                        scope.launch {
                            repository.clearHistory()
                            snackbarHostState.showSnackbar("History cleared")
                        }
                    },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteSweep,
                        contentDescription = "Clear All",
                        tint = Color.White
                    )
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun HistoryItemRow(
    item: HistoryItem,
    index: Int,
    onPageClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPageClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("history_item_$index"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.url,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                tint = Color.Gray
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = Color.Gray.copy(alpha = 0.2f)
    )
}
