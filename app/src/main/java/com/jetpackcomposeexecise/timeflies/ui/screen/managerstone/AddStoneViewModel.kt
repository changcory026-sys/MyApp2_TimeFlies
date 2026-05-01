package com.jetpackcomposeexecise.timeflies.ui.screen.managerstone

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackcomposeexecise.timeflies.data.local.entity.LifeEventEntity
import com.jetpackcomposeexecise.timeflies.data.local.repository.TimeFliesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddStoneUiState(
    val lifeEventInput: String = "",
    val eventInput: String = "",
    val availableLifeEvents: List<LifeEventEntity> = emptyList(),
    val filteredLifeEvents: List<LifeEventEntity> = emptyList(),
    val isDropdownExpanded: Boolean = false,
    val isSaveEnabled: Boolean = false
)

@HiltViewModel
class AddStoneViewModel @Inject constructor(
    private val repository: TimeFliesRepository
) : ViewModel() {

    var uiState by mutableStateOf(AddStoneUiState())
        private set

    init {
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
            // 修复点：使用健壮的获取/创建方法，确保拿到的 ID 永远有效且不会导致关联失败
            val lifeEventId = repository.getOrCreateLifeEventId(uiState.lifeEventInput)
            
            if (lifeEventId != -1L) {
                repository.insertEventToLifeEvent(lifeEventId, uiState.eventInput)
                onSuccess()
            }
        }
    }
    
    fun setDropdownExpanded(expanded: Boolean) {
        uiState = uiState.copy(isDropdownExpanded = expanded)
    }
}
