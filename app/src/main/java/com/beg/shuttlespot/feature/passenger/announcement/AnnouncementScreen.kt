package com.beg.shuttlespot.feature.passenger.announcement

// Overview: Defines announcement screen for the passenger announcement area of ShuttleSpot.

// Dependencies used by this file.
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.beg.shuttlespot.core.model.Announcement
import com.beg.shuttlespot.core.model.AnnouncementTag
import com.beg.shuttlespot.core.model.AnnouncementTagKind
import com.beg.shuttlespot.core.ui.components.EmptyState
import com.beg.shuttlespot.core.ui.components.ErrorState
import com.beg.shuttlespot.core.ui.components.FilterDropdown
import com.beg.shuttlespot.core.ui.components.LoadingState
import com.beg.shuttlespot.core.ui.components.SearchField
import com.beg.shuttlespot.core.ui.components.WaveBackground
import com.beg.shuttlespot.core.ui.components.WaveStyle
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme

/**
 * Layar penuh daftar pengumuman (di luar shell).
 * [onAnnouncementClick] menerima id pengumuman; saat halaman detail ada, sambungkan di nav graph.
 */
@Composable
// Renders the announcement screen UI section.
fun AnnouncementScreen(
    onBack: () -> Unit,
    onAnnouncementClick: (String) -> Unit = {},
    viewModel: AnnouncementViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    AnnouncementContent(
        state = state,
        onBack = onBack,
        onQueryChange = viewModel::onQueryChange,
        onTopicChange = viewModel::onTopicChange,
        onTimeChange = viewModel::onTimeChange,
        onAnnouncementClick = onAnnouncementClick,
    )
}

@Composable
// Renders the announcement content UI section.
fun AnnouncementContent(
    state: AnnouncementUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onTopicChange: (AnnouncementTopicFilter) -> Unit,
    onTimeChange: (AnnouncementTimeFilter) -> Unit,
    onAnnouncementClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                text = "Pengumuman",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SearchField(value = state.query, onValueChange = onQueryChange)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterDropdown(
                    label = "Topik",
                    selected = state.topic,
                    options = AnnouncementTopicFilter.entries,
                    optionLabel = { it.label },
                    onSelected = onTopicChange,
                )
                FilterDropdown(
                    label = "Waktu",
                    selected = state.time,
                    options = AnnouncementTimeFilter.entries,
                    optionLabel = { it.label },
                    onSelected = onTimeChange,
                )
            }
        }

        when {
            state.isLoading -> LoadingState("Memuat pengumuman")
            state.errorMessage != null -> ErrorState(state.errorMessage)
            state.items.isEmpty() -> EmptyState("Tidak ada pengumuman", "Coba ubah kata kunci atau filter.")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
            ) {
                items(state.items, key = { it.id }) { item ->
                    AnnouncementListItem(item = item, onClick = { onAnnouncementClick(item.id) })
                }
            }
        }
    }
}

// File-level constants and shared definitions.
private val previewItems = listOf(
    Announcement(
        "1", "Admin", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor ...",
        System.currentTimeMillis() - 5 * 60_000L,
        title = "Bus BSD akan mengalami pelambatan!",
        tags = listOf(
            AnnouncementTag("Rute BSD", AnnouncementTagKind.Route),
            AnnouncementTag("Rute Utama", AnnouncementTagKind.MainRoute),
        ),
    ),
    Announcement(
        "2", "Admin", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor ...",
        System.currentTimeMillis() - 30 * 60_000L,
        title = "Pesan dari Admin [UDAHDIBACA]",
        tags = listOf(AnnouncementTag("Umum", AnnouncementTagKind.General)),
        isRead = true,
    ),
)

@Preview(showBackground = true, name = "Berisi")
@Composable
// Renders the announcement content preview UI section.
private fun AnnouncementContentPreview() {
    ShuttleSpotTheme(darkTheme = false) {
        WaveBackground(WaveStyle.Main) {
            AnnouncementContent(AnnouncementUiState(items = previewItems), {}, {}, {}, {}, {})
        }
    }
}

@Preview(showBackground = true, name = "Kosong")
@Composable
// Renders the announcement content empty preview UI section.
private fun AnnouncementContentEmptyPreview() {
    ShuttleSpotTheme(darkTheme = false) {
        WaveBackground(WaveStyle.Main) {
            AnnouncementContent(AnnouncementUiState(), {}, {}, {}, {}, {})
        }
    }
}
