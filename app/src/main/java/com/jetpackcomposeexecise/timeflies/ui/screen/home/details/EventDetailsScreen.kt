package com.jetpackcomposeexecise.timeflies.ui.screen.home.details

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jetpackcomposeexecise.timeflies.R
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventEntity
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventDetailsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<DetailedEvent?>(null) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_event_details),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                text = { Text(stringResource(R.string.dialog_title_add)) },
                icon = { Icon(Icons.Default.Add, null) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = uiState.date,
                modifier = Modifier.padding(16.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            if (uiState.events.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.no_data_hint), color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = uiState.events,
                        key = { it.record.record.id }
                    ) { item ->
                        SwipeToEditDeleteEventItem(
                            event = item,
                            onDelete = { viewModel.deleteRecord(item) },
                            onEdit = { editingEvent = item }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }

    val scope = rememberCoroutineScope()
    if (showAddDialog) {
        EventRecordDialog(
            title = stringResource(R.string.dialog_title_add),
            allEvents = uiState.allEvents,
            initialEventName = "",
            initialTimeSlot = null,
            initialCostTime = null,
            onConfirm = { eventName, timeSlot, costTime ->
                scope.launch { viewModel.addRecord(uiState.date, eventName, timeSlot, costTime) }
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editingEvent?.let { event ->
        EventRecordDialog(
            title = stringResource(R.string.dialog_title_edit),
            allEvents = uiState.allEvents,
            initialEventName = event.record.eventDetails.event,
            initialTimeSlot = event.record.record.timeSlot,
            initialCostTime = event.record.record.costTime,
            onConfirm = { eventName, timeSlot, costTime ->
                scope.launch { viewModel.editRecord(event.record.record.id, eventName, timeSlot, costTime) }
                editingEvent = null
            },
            onDismiss = { editingEvent = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventRecordDialog(
    title: String,
    allEvents: List<EventEntity>,
    initialEventName: String,
    initialTimeSlot: Int?,
    initialCostTime: Double?,
    onConfirm: (eventName: String, timeSlot: Int, costTime: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val timeSlotOptions = (0..23).toList()
    val costTimeOptions = (0..10).map { it / 10.0 }

    fun formatTimeSlotLabel(slot: Int): String {
        val end = if (slot == 23) 24 else slot + 1
        return "${slot}:00 - ${end}:00"
    }

    var selectedEventName by remember { mutableStateOf(initialEventName) }
    var selectedTimeSlot by remember { mutableStateOf<Int?>(initialTimeSlot?.coerceIn(0, 23)) }
    var selectedCostTime by remember { mutableStateOf<Double?>(initialCostTime?.coerceIn(0.0, 1.0)) }
    var eventExpanded by remember { mutableStateOf(false) }
    var timeSlotExpanded by remember { mutableStateOf(false) }
    var costTimeExpanded by remember { mutableStateOf(false) }
    // Removed timeSlotListState definition

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 事项名称下拉框
                ExposedDropdownMenuBox(
                    expanded = eventExpanded,
                    onExpandedChange = { eventExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedEventName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.dialog_label_event_name)) },
                        placeholder = { Text(stringResource(R.string.dialog_hint_select_event)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = eventExpanded,
                        onDismissRequest = { eventExpanded = false }
                    ) {
                        allEvents.forEach { event ->
                            DropdownMenuItem(
                                text = { Text(event.event) },
                                onClick = {
                                    selectedEventName = event.event
                                    eventExpanded = false
                                }
                            )
                        }
                    }
                }

                // 时段下拉框
                ExposedDropdownMenuBox(
                    expanded = timeSlotExpanded,
                    onExpandedChange = { timeSlotExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedTimeSlot?.let { formatTimeSlotLabel(it) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.dialog_label_time_slot)) },
                        placeholder = { Text(stringResource(R.string.dialog_hint_input_time_slot)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeSlotExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = timeSlotExpanded,
                        onDismissRequest = { timeSlotExpanded = false }
                    ) {
                        timeSlotOptions.forEach { slot ->
                            DropdownMenuItem(
                                text = { Text(formatTimeSlotLabel(slot)) },
                                onClick = {
                                    selectedTimeSlot = slot
                                    timeSlotExpanded = false
                                }
                            )
                        }
                    }
                }

                // 时长下拉框
                ExposedDropdownMenuBox(
                    expanded = costTimeExpanded,
                    onExpandedChange = { costTimeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCostTime?.let { String.format(Locale.getDefault(), "%.1fH", it) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.dialog_label_cost_time)) },
                        placeholder = { Text(stringResource(R.string.dialog_hint_input_cost_time)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = costTimeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = costTimeExpanded,
                        onDismissRequest = { costTimeExpanded = false }
                    ) {
                        costTimeOptions.forEach { cost ->
                            DropdownMenuItem(
                                text = { Text(String.format(Locale.getDefault(), "%.1fH", cost)) },
                                onClick = {
                                    selectedCostTime = cost
                                    costTimeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val eventName = selectedEventName
                    val timeSlot = selectedTimeSlot
                    val costTime = selectedCostTime
                    if (eventName.isBlank() || timeSlot == null || costTime == null) return@TextButton
                    onConfirm(eventName, timeSlot, costTime)
                }
            ) {
                Text(stringResource(R.string.dialog_button_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_button_cancel))
            }
        }
    )
}

@Composable
fun SwipeToEditDeleteEventItem(
    event: DetailedEvent,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "swipe_offset")
    val density = LocalDensity.current
    val actionWidth = with(density) { 160.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp) // Replaced IntrinsicSize.Min with a fixed height
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .background(Color.Gray.copy(alpha = 0.8f))
                    .clickable {
                        offsetX = 0f
                        onEdit()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, stringResource(R.string.label_edit), tint = Color.White)
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .background(Color.Red.copy(alpha = 0.8f))
                    .clickable {
                        offsetX = 0f
                        onDelete()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, stringResource(R.string.label_delete), tint = Color.White)
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.White)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newOffset = (offsetX + delta).coerceIn(-actionWidth, 0f)
                        offsetX = newOffset
                    },
                    onDragStopped = {
                        offsetX = if (offsetX < -actionWidth / 2) -actionWidth else 0f
                    }
                )
        ) {
            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.White),
                headlineContent = {
                    Text(
                        text = event.record.eventDetails.event,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                supportingContent = {
                    Text(
                        text = event.timeRange,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                trailingContent = {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.1fH", event.record.record.costTime),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF9162FA)
                        )
                        Text(
                            text = "${event.percentage}%",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            )
        }
    }
}
