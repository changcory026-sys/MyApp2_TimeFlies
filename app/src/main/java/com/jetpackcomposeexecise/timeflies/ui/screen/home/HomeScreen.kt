package com.jetpackcomposeexecise.timeflies.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.jetpackcomposeexecise.timeflies.R
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventEntity
import com.jetpackcomposeexecise.timeflies.data.local.model.EventWithCost
import java.time.LocalDate
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToManager: () -> Unit,
    onNavigateToStatistics: (LocalDate) -> Unit,
    onNavigateToDetails: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val defaultText = stringResource(R.string.default_event_selection)

    HomeScreenContext(
        onNavigateToManager = onNavigateToManager,
        onNavigateToStatistics = onNavigateToStatistics,
        onNavigateToDetails = onNavigateToDetails,
        modifier = modifier,
        selectedDate = uiState.selectedDate,
        dateList = uiState.dateList,
        eventRecords = uiState.eventRecords,
        currentTimeDisplay = uiState.currentTimeDisplay,
        selectedEvent = uiState.selectedEvent ?: defaultText,
        isEventSelected = uiState.selectedEvent != null,
        availableEvents = uiState.availableEvents,
        isTimerRunning = uiState.isTimerRunning,
        isTimerPaused = uiState.isTimerPaused,
        timerSeconds = uiState.timerSeconds,
        timerDisplay = uiState.timerDisplay,
        showFinishPopup = uiState.showFinishPopup,
        popupEventName = uiState.popupEventName,
        popupEventCost = uiState.popupEventCost,
        popupEventTotalCost = uiState.popupEventTotalCost,
        onDateSelected = viewModel::onDateSelected,
        onEventSelected = viewModel::onEventSelected,
        onStartTimer = viewModel::startTimer,
        onPauseTimer = viewModel::pauseTimer,
        onStopTimer = viewModel::stopTimer,
        onDismissPopup = viewModel::dismissFinishPopup
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContext(
    onNavigateToManager: () -> Unit,
    onNavigateToStatistics: (LocalDate) -> Unit,
    onNavigateToDetails: (LocalDate) -> Unit,
    selectedDate: LocalDate,
    dateList: List<LocalDate>,
    eventRecords: List<EventWithCost>,
    currentTimeDisplay: String,
    selectedEvent: String,
    isEventSelected: Boolean,
    availableEvents: List<EventEntity>,
    isTimerRunning: Boolean,
    isTimerPaused: Boolean,
    timerSeconds: Long,
    timerDisplay: String,
    showFinishPopup: Boolean,
    popupEventName: String,
    popupEventCost: Double,
    popupEventTotalCost: Double,
    onDateSelected: (LocalDate) -> Unit,
    onEventSelected: (String) -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onDismissPopup: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dateExpanded by remember { mutableStateOf(false) }
    var eventExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 顶部导航与日期
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateToManager) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.cd_menu))
                    }

                    Box {
                        TextButton(onClick = { dateExpanded = true }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedDate.toString(),
                                    fontSize = 24.sp,
                                    color = Color.Black
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = dateExpanded,
                            onDismissRequest = { dateExpanded = false },
                            modifier = Modifier
                                .heightIn(max = (LocalConfiguration.current.screenHeightDp / 3).dp)
                                .width(IntrinsicSize.Min),
                            offset = DpOffset(10.dp, 4.dp)
                        ) {
                            dateList.forEach { date ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (date == LocalDate.now()) "${date}${stringResource(R.string.today_label)}" else date.toString(),
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Center,
                                            fontSize = 16.sp
                                        )
                                    },
                                    onClick = {
                                        onDateSelected(date)
                                        dateExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(onClick = { onNavigateToStatistics(selectedDate) }) {
                        Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.cd_statistics))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            //标题文本
            Text(
                text = stringResource(id = R.string.popup_title_finish),
                fontSize = 24.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Event排行显示区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color.Black)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(R.string.where_time_fly),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth()
                    )

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (eventRecords.isEmpty()) {
                            Text(
                                text = stringResource(R.string.default_life_consumed_text),
                                fontSize = 18.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                eventRecords.forEachIndexed { index, record ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val isTopThree = index < 3
                                        val prefix = when (index) {
                                            0 -> "1️⃣ "
                                            1 -> "2️⃣ "
                                            2 -> "3️⃣ "
                                            else -> "${index + 1}. "
                                        }
                                        Text(
                                            text = "$prefix${record.eventDetails.event}",
                                            fontSize = 18.sp,
                                            fontWeight = if (isTopThree) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isTopThree) Color.Black else Color.Gray
                                        )
                                        Text(
                                            text = String.format(
                                                Locale.getDefault(),
                                                "%.1fH",
                                                record.record.costTime
                                            ),
                                            fontSize = 18.sp,
                                            fontWeight = if (isTopThree) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isTopThree) Color.Black else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 1. 新增：“查看详情”文本
                    Text(
                        text = "查看详情",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp)
                            .clickable { onNavigateToDetails(selectedDate) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))

            // 时间显示
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentTimeDisplay,
                    fontSize = 92.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1C1E),
                    maxLines = 1,
                    softWrap = false
                )
            }

            // 修改：使用固定高度的 Box 包裹计时文本，确保占位空间固定，不影响排行区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isTimerRunning || isTimerPaused) {
                    Text(
                        text = timerDisplay.replace(" ", ""), // 确保格式为 00:00:00
                        fontSize = 18.sp,
                        color = Color(0xFF9162FA),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.2f))

            // 事项下拉框
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Black)
            ) {
                TextButton(
                    onClick = { eventExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    enabled = !isTimerRunning
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedEvent,
                            color = if (isTimerRunning) Color.LightGray else Color.Black,
                            modifier = Modifier.weight(1f),
                            fontSize = 18.sp
                        )
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (isTimerRunning) Color.LightGray else Color.Black
                        )
                    }
                }
                DropdownMenu(
                    expanded = eventExpanded,
                    onDismissRequest = { eventExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    if (availableEvents.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.default_event_option)) },
                            onClick = { 
                                eventExpanded = false
                                onNavigateToManager() 
                            }
                        )
                    } else {
                        availableEvents.forEach { eventEntity ->
                            DropdownMenuItem(
                                text = { Text(eventEntity.event) },
                                onClick = {
                                    onEventSelected(eventEntity.event)
                                    eventExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(36.dp))

            // 按钮区域
            if (!isTimerRunning) {
                Button(
                    onClick = onStartTimer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9162FA)),
                    enabled = isEventSelected
                ) {
                    Text(
                        text = stringResource(R.string.timer_start),
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isTimerPaused) {
                        OutlinedButton(
                            onClick = onStartTimer,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFF9162FA)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9162FA))
                        ) {
                            Text(text = stringResource(R.string.btn_resume), fontSize = 20.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onPauseTimer,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, Color(0xFF9162FA)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9162FA))
                        ) {
                            Text(text = stringResource(R.string.timer_pause), fontSize = 20.sp)
                        }
                    }

                    Button(
                        onClick = onStopTimer,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9162FA))
                    ) {
                        Text(
                            text = stringResource(R.string.timer_stop),
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 结束后的提示弹窗
        if (showFinishPopup) {
            Dialog(
                onDismissRequest = onDismissPopup,
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                // 半透明背景
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    // 弹窗主体
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .wrapContentHeight(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.popup_title_finish2),
                                fontSize = 20.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = popupEventName,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.popup_cost_time),
                                    fontSize = 18.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1fH", popupEventCost),
                                    fontSize = 18.sp,
                                    color = Color.Gray
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.popup_total_cost_time),
                                    fontSize = 18.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1fH", popupEventTotalCost),
                                    fontSize = 18.sp,
                                    color = Color.Gray
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            // 确定按钮
                            Button(
                                onClick = onDismissPopup,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFF9162FA))
                            ) {
                                Text(
                                    text = stringResource(R.string.button_ok),
                                    color = Color(0xFF9162FA),
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreenContext(
        onNavigateToManager = {},
        onNavigateToStatistics = {},
        onNavigateToDetails = {},
        selectedDate = LocalDate.now(),
        dateList = listOf(LocalDate.now()),
        eventRecords = emptyList(),
        currentTimeDisplay = "12 : 59",
        selectedEvent = "I am what I choose to become:",
        isEventSelected = false,
        availableEvents = emptyList(),
        isTimerRunning = false,
        isTimerPaused = false,
        timerSeconds = 0,
        timerDisplay = "00 : 00 : 00",
        showFinishPopup = false,
        popupEventName = "",
        popupEventCost = 0.0,
        popupEventTotalCost = 0.0,
        onDateSelected = {},
        onEventSelected = {},
        onStartTimer = {},
        onPauseTimer = {},
        onStopTimer = {},
        onDismissPopup = {}
    )
}
