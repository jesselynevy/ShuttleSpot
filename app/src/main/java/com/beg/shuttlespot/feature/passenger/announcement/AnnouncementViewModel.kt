package com.beg.shuttlespot.feature.passenger.announcement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beg.shuttlespot.core.model.Announcement
import com.beg.shuttlespot.core.model.AnnouncementTagKind
import com.beg.shuttlespot.data.repository.AnnouncementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AnnouncementViewModel @Inject constructor(
    repository: AnnouncementRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val topic = MutableStateFlow(AnnouncementTopicFilter.All)
    private val time = MutableStateFlow(AnnouncementTimeFilter.Today)

    val state: StateFlow<AnnouncementUiState> = combine(
        repository.observeAnnouncements(), query, topic, time,
    ) { list, q, t, w ->
        AnnouncementUiState(
            items = list.filter { it.matches(q, t, w) }.sortedByDescending { it.postedAtEpochMillis },
            query = q,
            topic = t,
            time = w,
        )
    }
        .catch { emit(AnnouncementUiState(errorMessage = "Pengumuman belum dapat dimuat.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnnouncementUiState(isLoading = true))

    fun onQueryChange(value: String) = query.update { value }
    fun onTopicChange(value: AnnouncementTopicFilter) = topic.update { value }
    fun onTimeChange(value: AnnouncementTimeFilter) = time.update { value }
}

data class AnnouncementUiState(
    val items: List<Announcement> = emptyList(),
    val query: String = "",
    val topic: AnnouncementTopicFilter = AnnouncementTopicFilter.All,
    val time: AnnouncementTimeFilter = AnnouncementTimeFilter.Today,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

enum class AnnouncementTopicFilter(val label: String) {
    All("Semua"),
    General("Umum"),
    Route("Rute"),
    MainRoute("Rute Utama"),
}

enum class AnnouncementTimeFilter(val label: String) {
    Today("Hari ini"),
    Week("7 hari"),
    All("Semua"),
}

private fun Announcement.matches(
    query: String,
    topic: AnnouncementTopicFilter,
    time: AnnouncementTimeFilter,
): Boolean {
    val topicOk = when (topic) {
        AnnouncementTopicFilter.All -> true
        AnnouncementTopicFilter.General -> tags.any { it.kind == AnnouncementTagKind.General }
        AnnouncementTopicFilter.Route -> tags.any { it.kind == AnnouncementTagKind.Route }
        AnnouncementTopicFilter.MainRoute -> tags.any { it.kind == AnnouncementTagKind.MainRoute }
    }
    val timeOk = postedAtEpochMillis >= when (time) {
        AnnouncementTimeFilter.Today ->
            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        AnnouncementTimeFilter.Week -> System.currentTimeMillis() - 7 * 24 * 60 * 60_000L
        AnnouncementTimeFilter.All -> Long.MIN_VALUE
    }
    val q = query.trim()
    val queryOk = q.isEmpty() ||
        title.contains(q, ignoreCase = true) ||
        message.contains(q, ignoreCase = true) ||
        sender.contains(q, ignoreCase = true) ||
        tags.any { it.label.contains(q, ignoreCase = true) }
    return topicOk && timeOk && queryOk
}
