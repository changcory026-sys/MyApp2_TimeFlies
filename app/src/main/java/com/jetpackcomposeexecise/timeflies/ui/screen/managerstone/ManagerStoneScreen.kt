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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    onNavigateToAddEvent: (String?, String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ManagerStoneViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val focusManager = LocalFocusManager.current

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
            onDeleteEvent = viewModel::deleteEvent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerStoneScreenContext(
    lifeEvents: List<LifeEventWithEvents>,
    onBack: () -> Unit,
    onAddEvent: (String?, String?) -> Unit,
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
                            LifeEventHeaderItem(
                                lifeEvent = group.lifeEvent, 
                                onEditClick = { onAddEvent(group.lifeEvent.lifeEvent, null) }
                            )
                            group.events.forEach { eventEntity ->
                                EventItem(
                                    event = eventEntity, 
                                    onEditClick = { onAddEvent(group.lifeEvent.lifeEvent, eventEntity.event) },
                                    onDelete = onDeleteEvent
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onAddEvent(null, null) },
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
fun LifeEventHeaderItem(lifeEvent: LifeEventEntity, onEditClick: () -> Unit) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "header_offset")
    
    val density = LocalDensity.current
    val actionWidthPx = with(density) { 60.dp.toPx() }

    Box(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = 8.dp)
    ) {
        // 1. 前景内容区 (先定义，渲染在底层)
        Box(
            modifier = Modifier.offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .fillMaxWidth().background(MaterialTheme.colorScheme.background)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newOffset = (offsetX + delta).coerceIn(-actionWidthPx, 0f)
                        offsetX = newOffset
                    },
                    onDragStopped = {
                        offsetX = if (offsetX < -actionWidthPx / 3) -actionWidthPx else 0f
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "------ ${lifeEvent.lifeEvent} ------", fontSize = 20.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }

        // 2. 操作区 (后定义，渲染在顶层，确保可点击)
        if (offsetX < 0f) {
            Box(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(60.dp)
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(4.dp))
                    .clickable {
                        offsetX = 0f
                        onEditClick() 
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2196F3))
            }
        }
    }
}

@Composable
fun EventItem(event: EventEntity, onEditClick: () -> Unit, onDelete: (EventEntity) -> Unit) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "event_offset")

    val density = LocalDensity.current
    val actionWidthPx = with(density) { 120.dp.toPx() }

    Box(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(vertical = 4.dp)
    ) {
        // 1. 前景内容区 (底层)
        Box(
            modifier = Modifier.offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .fillMaxWidth().background(MaterialTheme.colorScheme.background)
                .border(1.dp, Color.Black).padding(16.dp)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newOffset = (offsetX + delta).coerceIn(-actionWidthPx, 0f)
                        offsetX = newOffset
                    },
                    onDragStopped = {
                        offsetX = if (offsetX < -actionWidthPx / 3) -actionWidthPx else 0f
                    }
                )
        ) {
            Text(text = event.event, fontSize = 18.sp, color = Color.Black)
        }

        // 2. 操作区 (顶层)
        if (offsetX < 0f) {
            Row(modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight().width(120.dp)) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFE3F2FD))
                        .clickable { 
                            offsetX = 0f
                            onEditClick() 
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF2196F3))
                }
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFFEBEE))
                        .clickable { onDelete(event); offsetX = 0f },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ManagerStoneScreenPreview() {
    ManagerStoneScreenContext(emptyList(), {}, {_,_ ->}, {})
}
