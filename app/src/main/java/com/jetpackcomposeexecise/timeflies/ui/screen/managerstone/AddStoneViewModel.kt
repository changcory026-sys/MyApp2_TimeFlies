package com.jetpackcomposeexecise.timeflies.ui.screen.managerstone

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.LifeEventEntity
import com.jetpackcomposeexecise.timeflies.data.local.repository.TimeFliesRepository
import com.jetpackcomposeexecise.timeflies.ui.navigation.AddStoneScreenRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddStoneUiState(
    val lifeEventInput: String = "",
    val eventInput: String = "",
    val availableLifeEvents: List<LifeEventEntity> = emptyList(),
    val filteredLifeEvents: List<LifeEventEntity> = emptyList(),
    val isDropdownExpanded: Boolean = false,
    val isSaveEnabled: Boolean = false,
    val isEditMode: Boolean = false,
    val originalLifeEvent: String? = null,
    val originalEvent: String? = null
)

@HiltViewModel
class AddStoneViewModel @Inject constructor(
    private val repository: TimeFliesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val routeData = savedStateHandle.toRoute<AddStoneScreenRoute>()

    var uiState by mutableStateOf(AddStoneUiState())
        private set

    init {
        // 初始化编辑状态
        val isEdit = routeData.originalLifeEvent != null && routeData.originalEvent != null
        uiState = uiState.copy(
            lifeEventInput = routeData.originalLifeEvent ?: "",
            eventInput = routeData.originalEvent ?: "",
            isEditMode = isEdit,
            originalLifeEvent = routeData.originalLifeEvent,
            originalEvent = routeData.originalEvent
        )
        validateInputs()

        viewModelScope.launch {
            repository.getAllLifeEvents().collectLatest { lifeEvents ->
                uiState = uiState.copy(availableLifeEvents = lifeEvents)
                updateFilteredLifeEvents(uiState.lifeEventInput)
            }
        }
    }

    fun onLifeEventInputChanged(input: String) {
        uiState = uiState.copy(
            lifeEventInput = input,
            isDropdownExpanded = true
        )
        updateFilteredLifeEvents(input)
        validateInputs()
    }

    fun onEventInputChanged(input: String) {
        uiState = uiState.copy(eventInput = input)
        validateInputs()
    }

    fun onLifeEventSelected(name: String) {
        uiState = uiState.copy(
            lifeEventInput = name,
            isDropdownExpanded = false
        )
        validateInputs()
    }

    fun addNewLifeEvent() {
        val newName = uiState.lifeEventInput
        if (newName.isNotBlank()) {
            viewModelScope.launch {
                repository.getOrCreateLifeEventId(newName)
                uiState = uiState.copy(isDropdownExpanded = false)
            }
        }
    }

    private fun updateFilteredLifeEvents(input: String) {
        val filtered = uiState.availableLifeEvents.filter {
            it.lifeEvent.contains(input, ignoreCase = true)
        }
        uiState = uiState.copy(filteredLifeEvents = filtered)
    }

    private fun validateInputs() {
        uiState = uiState.copy(
            isSaveEnabled = uiState.lifeEventInput.isNotBlank() && uiState.eventInput.isNotBlank()
        )
    }

    fun saveStone(onSuccess: () -> Unit) {
        if (!uiState.isSaveEnabled) return

        viewModelScope.launch {
            // 如果是编辑模式，先删除旧的数据
            if (uiState.isEditMode) {
                // 1. 找到旧类别的 ID
                val lifeEvents = repository.getAllLifeEvents().first()
                val oldLifeEventId = lifeEvents.find { it.lifeEvent == uiState.originalLifeEvent }?.id
                
                if (oldLifeEventId != null) {
                    // 2. 找到旧的事项 Entity
                    val events = repository.getAllEvents().first()
                    val eventToDelete = events.find { 
                        it.event == uiState.originalEvent && it.lifeEventId == oldLifeEventId 
                    }
                    
                    // 3. 删除旧事项
                    if (eventToDelete != null) {
                        repository.deleteEvent(eventToDelete)
                    }
                }
            }

            // 4. 执行新增（无论是纯新增还是修改后的新增）
            performInsert(onSuccess)
        }
    }

    private suspend fun performInsert(onSuccess: () -> Unit) {
        val lifeEventId = repository.getOrCreateLifeEventId(uiState.lifeEventInput)
        if (lifeEventId != -1L) {
            repository.insertEventToLifeEvent(lifeEventId, uiState.eventInput)
            onSuccess()
        }
    }
    
    fun setDropdownExpanded(expanded: Boolean) {
        uiState = uiState.copy(isDropdownExpanded = expanded)
    }
}
