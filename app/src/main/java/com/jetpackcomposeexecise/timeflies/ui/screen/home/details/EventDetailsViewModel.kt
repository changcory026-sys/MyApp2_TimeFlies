package com.jetpackcomposeexecise.timeflies.ui.screen.home.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jetpackcomposeexecise.timeflies.data.local.model.EventWithCost
import com.jetpackcomposeexecise.timeflies.data.local.repository.TimeFliesRepository
import com.jetpackcomposeexecise.timeflies.ui.navigation.EventDetailsScreenRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailedEvent(
    val record: EventWithCost,
    val timeRange: String,
    val percentage: Int
)

data class EventDetailsUiState(
    val date: String = "",
    val events: List<DetailedEvent> = emptyList(),
    val totalHours: Double = 0.0
)

@HiltViewModel
class EventDetailsViewModel @Inject constructor(
    private val repository: TimeFliesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routeData = savedStateHandle.toRoute<EventDetailsScreenRoute>()
    
    var uiState by mutableStateOf(EventDetailsUiState(date = routeData.date))
        private set

    init {
        observeDetails()
    }

    private fun observeDetails() {
        viewModelScope.launch {
            repository.getDateWithEvents(uiState.date).collectLatest { dateWithEvents ->
                val rawEvents = dateWithEvents?.events ?: emptyList()
                val total = rawEvents.sumOf { it.record.costTime }
                
                val detailedEvents = rawEvents.map { event ->
                    DetailedEvent(
                        record = event,
                        timeRange = formatTimeSlot(event.record.timeSlot),
                        percentage = if (total > 0) (event.record.costTime / total * 100).toInt() else 0
                    )
                }.sortedBy { it.record.record.timeSlot } // 按时间顺序排列

                uiState = uiState.copy(
                    events = detailedEvents,
                    totalHours = total
                )
            }
        }
    }

    private fun formatTimeSlot(slot: Int): String {
        val start = "$slot:00"
        val end = if (slot == 23) "24:00" else "${slot + 1}:00"
        return "$start-$end"
    }

    fun deleteRecord(event: DetailedEvent) {
        viewModelScope.launch {
            repository.deleteEventRecord(
                dateId = event.record.record.dateId,
                eventId = event.record.record.eventId,
                timeSlot = event.record.record.timeSlot
            )
        }
    }
}
