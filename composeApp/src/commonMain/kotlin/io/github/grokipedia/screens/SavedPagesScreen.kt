package io.github.grokipedia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import io.github.grokipedia.data.SavedPage
import io.github.grokipedia.data.SavedPagesRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPagesScreen(
    repository: SavedPagesRepository,
    onNavigateBack: () -> Unit,
    onPageClick: (String) -> Unit
) {
    val savedPages by repository.savedPages.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }
    val snackbarHostState = remember { SnackbarHostState() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
    ) {
        // Content with haze source
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 56.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (savedPages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No saved pages yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(savedPages, key = { it.url }) { page ->
                    SavedPageItem(
                        page = page,
                        onPageClick = { onPageClick(page.url) },
                        onDeleteClick = {
                            scope.launch {
                                repository.removePage(page.url)
                                snackbarHostState.showSnackbar("Page removed")
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
                .hazeChild(
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
                text = "Saved Pages",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
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
private fun SavedPageItem(
    page: SavedPage,
    onPageClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPageClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = page.url,
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
