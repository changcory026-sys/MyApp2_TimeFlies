package com.jetpackcomposeexecise.timeflies.ui.screen.managerstone

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.LifeEventEntity
import com.jetpackcomposeexecise.timeflies.data.local.model.LifeEventWithEvents
import com.jetpackcomposeexecise.timeflies.data.local.repository.TimeFliesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManagerStoneUiState(
    val lifeEventsWithEvents: List<LifeEventWithEvents> = emptyList()
)

@HiltViewModel
class ManagerStoneViewModel @Inject constructor(
    private val repository: TimeFliesRepository
) : ViewModel() {

    var uiState by mutableStateOf(ManagerStoneUiState())
        private set

    init {
        observeLifeEvents()
    }

    private fun observeLifeEvents() {
        viewModelScope.launch {
            repository.getAllLifeEventsWithEvents().collectLatest { data ->
                // UI 层过滤，仅显示有事项的类别
                uiState = uiState.copy(
                    lifeEventsWithEvents = data.filter { it.events.isNotEmpty() }
                )
            }
        }
    }

    /**
     * 更新 LifeEvent 类别名称
     */
    fun updateLifeEvent(lifeEvent: LifeEventEntity, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            val updated = lifeEvent.copy(lifeEvent = newName)
            repository.updateLifeEvent(updated)
        }
    }

    /**
     * 更新具体事项名称
     */
    fun updateEvent(event: EventEntity, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            val updated = event.copy(event = newName)
            repository.updateEvent(updated)
        }
    }

    /**
     * 删除事项并自动清理空类别
     */
    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            // 需求：当一个 LifeEvent 下全部 Event 被删除后，自动删除该 LifeEvent
            repository.deleteEmptyLifeEvents()
        }
    }
}
