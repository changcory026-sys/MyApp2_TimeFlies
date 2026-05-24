package com.jetpackcomposeexecise.timeflies.ui.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.TimerSessionEntity
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
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
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
    private var sessionJob: Job? = null

    init {
        uiState = uiState.copy(currentTimeDisplay = formatTime(LocalTime.now()))
        startClock()
        observeEvents()
        observeRecords(uiState.selectedDate)
        observeTimerSession()
    }

    private fun observeTimerSession() {
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch {
            repository.getTimerSession().collectLatest { session ->
                if (session != null && session.isRunning) {
                    val elapsed = if (session.startTimeMillis != null) {
                        (System.currentTimeMillis() - session.startTimeMillis) / 1000
                    } else 0
                    
                    val currentTotalSeconds = session.accumulatedSeconds + elapsed
                    
                    uiState = uiState.copy(
                        isTimerRunning = true,
                        isTimerPaused = session.startTimeMillis == null,
                        selectedEvent = session.eventName,
                        timerSeconds = currentTotalSeconds,
                        timerDisplay = formatTimer(currentTotalSeconds)
                    )
                    
                    if (session.startTimeMillis != null) {
                        startUiTimerJob(session.startTimeMillis, session.accumulatedSeconds)
                    } else {
                        timerJob?.cancel()
                    }
                } else {
                    timerJob?.cancel()
                    uiState = uiState.copy(
                        isTimerRunning = false,
                        isTimerPaused = false,
                        timerSeconds = 0,
                        timerDisplay = "00 : 00 : 00"
                    )
                }
            }
        }
    }

    private fun startUiTimerJob(startTimeMillis: Long, baseSeconds: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                val elapsed = (System.currentTimeMillis() - startTimeMillis) / 1000
                val total = baseSeconds + elapsed
                uiState = uiState.copy(
                    timerSeconds = total,
                    timerDisplay = formatTimer(total)
                )
                delay(1000)
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
                val sortedRecords = dateWithEvents?.events?.sortedByDescending { it.record.costTime } ?: emptyList()
                val mergedRecords = mergeEventsByEventName(sortedRecords)
                uiState = uiState.copy(
                    eventRecords = mergedRecords,
                    lifeConsumedText = formatLifeConsumed(dateWithEvents)
                )
            }
        }
    }

    private fun mergeEventsByEventName(events: List<EventWithCost>): List<EventWithCost> {
        return events
            .groupBy { it.eventDetails.event }
            .map { (eventName, eventList) ->
                val totalCostTime = eventList.sumOf { it.record.costTime }
                val first = eventList.first()
                // 返回合并后的记录，costTime 为总和
                EventWithCost(
                    record = first.record.copy(costTime = totalCostTime),
                    eventDetails = first.eventDetails
                )
            }
            .sortedByDescending { it.record.costTime }
    }

    private fun formatLifeConsumed(data: DateWithEvents?): String? {
        if (data == null || data.events.isEmpty()) return null
        // 同样使用合并后的数据进行格式化显示
        val merged = mergeEventsByEventName(data.events)
        val sb = StringBuilder()
        merged.forEachIndexed { index, eventWithCost ->
            val prefix = if (index < 3) "${index + 1}." else ""
            sb.append("$prefix${eventWithCost.eventDetails.event}")
            sb.append("   ")
            sb.append(String.format(Locale.getDefault(), "%.1fH", eventWithCost.record.costTime))
            if (index < merged.size - 1) sb.append("\n")
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
        val event = uiState.selectedEvent ?: return
        viewModelScope.launch {
            repository.updateTimerSession(
                TimerSessionEntity(
                    eventName = event,
                    startTimeMillis = System.currentTimeMillis(),
                    accumulatedSeconds = uiState.timerSeconds,
                    isRunning = true
                )
            )
        }
    }

    fun pauseTimer() {
        viewModelScope.launch {
            repository.updateTimerSession(
                TimerSessionEntity(
                    eventName = uiState.selectedEvent,
                    startTimeMillis = null,
                    accumulatedSeconds = uiState.timerSeconds,
                    isRunning = true
                )
            )
        }
    }

    fun stopTimer() {
        val currentEvent = uiState.selectedEvent
        val currentDate = uiState.selectedDate.toString()
        val finalSeconds = uiState.timerSeconds

        viewModelScope.launch {
            if (currentEvent != null && finalSeconds > 0) {
                // 将总耗时按跨越的小时段进行分配
                var remainingMillis = finalSeconds * 1000
                var currentEndTime = LocalDateTime.now()

                while (remainingMillis > 0) {
                    val startOfHour = currentEndTime.withMinute(0).withSecond(0).withNano(0)
                    var millisInThisSlot = Duration.between(startOfHour, currentEndTime).toMillis()
                    
                    if (millisInThisSlot == 0L) {
                        // 刚好在整点结束，或者已经处理完当前小时，进入前一个小时
                        millisInThisSlot = 3600000L
                        val slot = (currentEndTime.hour - 1 + 24) % 24
                        val toRecord = minOf(remainingMillis, millisInThisSlot)
                        repository.saveEventRecord(currentDate, currentEvent, slot, toRecord.toDouble() / 3600000.0)
                        remainingMillis -= toRecord
                        currentEndTime = startOfHour.minusHours(1)
                    } else {
                        // 处理当前小时内的部分时间段
                        val slot = currentEndTime.hour
                        val toRecord = minOf(remainingMillis, millisInThisSlot)
                        repository.saveEventRecord(currentDate, currentEvent, slot, toRecord.toDouble() / 3600000.0)
                        remainingMillis -= toRecord
                        currentEndTime = startOfHour
                    }
                }

                // 计算当日该 Event 的总耗时，用于弹窗显示
                val dateWithEvents = repository.getDateWithEvents(currentDate).firstOrNull()
                val totalCost = dateWithEvents?.events
                    ?.filter { it.eventDetails.event == currentEvent }
                    ?.sumOf { it.record.costTime } ?: (finalSeconds.toDouble() / 3600.0)

                uiState = uiState.copy(
                    showFinishPopup = true,
                    popupEventName = currentEvent,
                    popupEventCost = round((finalSeconds.toDouble() / 3600.0) * 10) / 10.0,
                    popupEventTotalCost = round(totalCost * 10) / 10.0
                )
            }
            repository.clearTimerSession()
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
        sessionJob?.cancel()
    }
}
