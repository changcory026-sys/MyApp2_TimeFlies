package com.jetpackcomposeexecise.timeflies.ui.screen.managerstone

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jetpackcomposeexecise.timeflies.R
import com.jetpackcomposeexecise.timeflies.data.local.entity.EventEntity
import com.jetpackcomposeexecise.timeflies.data.local.entity.LifeEventEntity
import com.jetpackcomposeexecise.timeflies.data.local.model.LifeEventWithEvents
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ManagerStoneScreen(
    onBack: () -> Unit,
    onNavigateToAddEvent: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManagerStoneViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val focusManager = LocalFocusManager.current

    // 1. 全局监听：点击空白处清除焦点并收起侧滑图标
    Box(modifier = modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        }
    ) {
        ManagerStoneScreenContext(
            lifeEvents = uiState.lifeEventsWithEvents,
            onBack = onBack,
            onAddEvent = onNavigateToAddEvent,
            onUpdateLifeEvent = viewModel::updateLifeEvent,
            onUpdateEvent = viewModel::updateEvent,
            onDeleteEvent = viewModel::deleteEvent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerStoneScreenContext(
    lifeEvents: List<LifeEventWithEvents>,
    onBack: () -> Unit,
    onAddEvent: () -> Unit,
    onUpdateLifeEvent: (LifeEventEntity, String) -> Unit,
    onUpdateEvent: (EventEntity, String) -> Unit,
    onDeleteEvent: (EventEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_life_event),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(lifeEvents, key = { _, item -> "tab_${item.lifeEvent.id}" }) { index, item ->
                    OutlinedButton(
                        onClick = { scope.launch { listState.animateScrollToItem(index) } },
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(2.dp, Color.Black),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        Text(text = item.lifeEvent.lifeEvent, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(lifeEvents, key = { _, item -> "group_${item.lifeEvent.id}" }) { _, group ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LifeEventHeaderItem(lifeEvent = group.lifeEvent, onUpdate = onUpdateLifeEvent)
                            group.events.forEach { eventEntity ->
                                EventItem(event = eventEntity, onUpdate = onUpdateEvent, onDelete = onDeleteEvent)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddEvent,
                modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFF9162FA))
            ) {
                Text(text = stringResource(R.string.button_add_event), fontSize = 20.sp, color = Color(0xFF9162FA))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LifeEventHeaderItem(lifeEvent: LifeEventEntity, onUpdate: (LifeEventEntity, String) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(TextFieldValue(lifeEvent.lifeEvent)) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX)
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val actionWidth = with(density) { 60.dp.toPx() }

    // 当失去焦点或退出编辑时，重置侧滑位置
    LaunchedEffect(isEditing) {
        if (!isEditing) offsetX = 0f
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = 8.dp)
    ) {
        // 背景操作区
        Box(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(60.dp)
                .background(Color(0xFFE3F2FD), RoundedCornerShape(4.dp))
                .clickable {
                    isEditing = true
                    textValue = TextFieldValue(lifeEvent.lifeEvent, TextRange(lifeEvent.lifeEvent.length))
                    offsetX = 0f // 点击编辑后自动收回背景
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2196F3))
        }

        // 前景内容区
        Box(
            modifier = Modifier.offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .fillMaxWidth().background(MaterialTheme.colorScheme.background)
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = !isEditing,
                    state = rememberDraggableState { delta ->
                        val newOffset = (offsetX + delta).coerceIn(-actionWidth, 0f)
                        offsetX = newOffset
                    },
                    onDragStopped = {
                        // 阈值调整为 1/3
                        offsetX = if (offsetX < -actionWidth / 3) -actionWidth else 0f
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isEditing) {
                BasicTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    textStyle = TextStyle(fontSize = 20.sp, color = Color.Gray, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                    modifier = Modifier.focusRequester(focusRequester)
                        .onFocusChanged { if (!it.isFocused && isEditing) { isEditing = false; onUpdate(lifeEvent, textValue.text) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus(); keyboardController?.show() }
            } else {
                Text(text = "------ ${lifeEvent.lifeEvent} ------", fontSize = 20.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EventItem(event: EventEntity, onUpdate: (EventEntity, String) -> Unit, onDelete: (EventEntity) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(TextFieldValue(event.event)) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX)

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val actionWidth = with(density) { 120.dp.toPx() }

    // 当失去焦点或退出编辑时，重置侧滑位置
    LaunchedEffect(isEditing) {
        if (!isEditing) offsetX = 0f
    }

    Box(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = 4.dp)
    ) {
        // 背景操作区
        Row(modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(120.dp)) {
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFE3F2FD))
                    .clickable { 
                        if (isEditing) {
                            isEditing = false
                            onUpdate(event, textValue.text)
                        } else {
                            isEditing = true
                            textValue = TextFieldValue(event.event, TextRange(event.event.length))
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(if (isEditing) Icons.Default.Check else Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2196F3))
            }
            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFFEBEE))
                    .clickable { onDelete(event); offsetX = 0f },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
            }
        }

        // 前景内容区
        Box(
            modifier = Modifier.offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .fillMaxWidth().background(MaterialTheme.colorScheme.background)
                .border(1.dp, Color.Black).padding(16.dp)
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = !isEditing,
                    state = rememberDraggableState { delta ->
                        val newOffset = (offsetX + delta).coerceIn(-actionWidth, 0f)
                        offsetX = newOffset
                    },
                    onDragStopped = {
                        // 阈值调整为 1/3
                        offsetX = if (offsetX < -actionWidth / 3) -actionWidth else 0f
                    }
                )
        ) {
            if (isEditing) {
                BasicTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    textStyle = TextStyle(fontSize = 18.sp, color = Color.Black),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus(); keyboardController?.show() }
            } else {
                Text(text = event.event, fontSize = 18.sp, color = Color.Black)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManagerStoneScreenPreview() {
    ManagerStoneScreenContext(emptyList(), {}, {}, { _, _ -> }, { _, _ -> }, {})
}
