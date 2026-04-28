package com.jetpackcomposeexecise.timeflies.ui.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventEntity
import com.jetpackcomposeexecise.timeflies.data.local.model.DateWithEvents
import com.jetpackcomposeexecise.timeflies.data.local.model.EventWithCost
import com.jetpackcomposeexecise.timeflies.data.local.repository.TimeFliesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.round

data class HomeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val dateList: List<LocalDate> = (-3..3).map { LocalDate.now().plusDays(it.toLong()) },
    val eventRecords: List<EventWithCost> = emptyList(),
    val lifeConsumedText: String? = null,
    val currentTimeDisplay: String = "",
    val selectedEvent: String? = null,
    val isTimerRunning: Boolean = false,
    val isTimerPaused: Boolean = false,
    val timerSeconds: Long = 0,
    val timerDisplay: String = "00 : 00 : 00",
    val lastRecordedDurationHours: Double = 0.0,
    val availableEvents: List<EventEntity> = emptyList(),
    // 弹窗相关状态
    val showFinishPopup: Boolean = false,
    val popupEventName: String = "",
    val popupEventCost: Double = 0.0,
    val popupEventTotalCost: Double = 0.0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TimeFliesRepository
) : ViewModel() {

    var uiState by mutableStateOf(HomeUiState())
        private set

    private var clockJob: Job? = null
    private var timerJob: Job? = null
    private var recordsJob: Job? = null

    init {
        uiState = uiState.copy(currentTimeDisplay = formatTime(LocalTime.now()))
        startClock()
        observeEvents()
        observeRecords(uiState.selectedDate)
        
        // 初始化一些测试数据（如果事件库为空）
        viewModelScope.launch {
            repository.getAllEvents().collectLatest { 
                if (it.isEmpty()) {
                    listOf("编程", "吃饭", "午休", "散步", "玩手机").forEach { name ->
                        repository.upsertEvent(name)
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            repository.getAllEvents().collectLatest { events ->
                uiState = uiState.copy(availableEvents = events)
            }
        }
    }

    private fun observeRecords(date: LocalDate) {
        recordsJob?.cancel()
        recordsJob = viewModelScope.launch {
            repository.getDateWithEvents(date.toString()).collectLatest { dateWithEvents ->
                // 按 costTime 从大到小排序
                val sortedRecords = dateWithEvents?.events?.sortedByDescending { it.record.costTime } ?: emptyList()
                uiState = uiState.copy(
                    eventRecords = sortedRecords,
                    lifeConsumedText = formatLifeConsumed(dateWithEvents)
                )
            }
        }
    }

    private fun formatLifeConsumed(data: DateWithEvents?): String? {
        if (data == null || data.events.isEmpty()) return null
        // 排序后的记录展示
        val sorted = data.events.sortedByDescending { it.record.costTime }
        val sb = StringBuilder()
        sorted.forEachIndexed { index, eventWithCost ->
            val prefix = if (index < 3) "${index + 1}." else ""
            sb.append("$prefix${eventWithCost.eventDetails.event}")
            sb.append("   ")
            sb.append(String.format(Locale.getDefault(), "%.1fH", eventWithCost.record.costTime))
            if (index < sorted.size - 1) sb.append("\n")
        }
        return sb.toString()
    }

    private fun startClock() {
        clockJob?.cancel()
        clockJob = viewModelScope.launch {
            while (isActive) {
                uiState = uiState.copy(currentTimeDisplay = formatTime(LocalTime.now()))
                delay(1000)
            }
        }
    }

    private fun formatTime(time: LocalTime): String {
        return time.format(DateTimeFormatter.ofPattern("hh : mm", Locale.getDefault()))
    }

    private fun formatTimer(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d : %02d : %02d", hours, minutes, seconds)
    }

    fun onDateSelected(date: LocalDate) {
        uiState = uiState.copy(selectedDate = date)
        observeRecords(date)
    }

    fun onEventSelected(event: String) {
        uiState = uiState.copy(selectedEvent = event)
    }

    fun startTimer() {
        if (uiState.isTimerRunning && !uiState.isTimerPaused) return
        
        uiState = uiState.copy(isTimerRunning = true, isTimerPaused = false)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val newSeconds = uiState.timerSeconds + 1
                uiState = uiState.copy(
                    timerSeconds = newSeconds,
                    timerDisplay = formatTimer(newSeconds)
                )
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        uiState = uiState.copy(isTimerPaused = true)
    }

    fun stopTimer() {
        timerJob?.cancel()
        val durationHours = uiState.timerSeconds.toDouble() / 3600.0
        val roundedHours = round(durationHours * 10) / 10.0
        
        val currentEvent = uiState.selectedEvent
        val currentDate = uiState.selectedDate.toString()

        viewModelScope.launch {
            if (currentEvent != null) {
                // 保存数据
                repository.saveEventRecord(currentDate, currentEvent, roundedHours)
                
                // 获取更新后的累计时长
                val dateWithEvents = repository.getDateWithEvents(currentDate).firstOrNull()
                val totalCost = dateWithEvents?.events?.find { it.eventDetails.event == currentEvent }?.record?.costTime ?: roundedHours

                uiState = uiState.copy(
                    showFinishPopup = true,
                    popupEventName = currentEvent,
                    popupEventCost = roundedHours,
                    popupEventTotalCost = totalCost,
                    isTimerRunning = false,
                    isTimerPaused = false,
                    timerSeconds = 0,
                    timerDisplay = "00 : 00 : 00",
                    lastRecordedDurationHours = roundedHours,
                    selectedEvent = null
                )
            } else {
                uiState = uiState.copy(
                    isTimerRunning = false,
                    isTimerPaused = false,
                    timerSeconds = 0,
                    timerDisplay = "00 : 00 : 00",
                    selectedEvent = null
                )
            }
        }
    }

    fun dismissFinishPopup() {
        uiState = uiState.copy(showFinishPopup = false)
    }

    override fun onCleared() {
        super.onCleared()
        clockJob?.cancel()
        timerJob?.cancel()
        recordsJob?.cancel()
    }
}
