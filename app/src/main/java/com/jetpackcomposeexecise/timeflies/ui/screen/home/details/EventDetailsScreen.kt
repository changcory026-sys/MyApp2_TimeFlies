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
import androidx.compose.material.icons.filled.Delete
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
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 日期显示
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
                        // 修改：使用 EventRecordEntity 的主键 id 作为唯一 key，解决重复 Key 导致的崩溃问题
                        key = { it.record.record.id }
                    ) { item ->
                        SwipeToDeleteEventItem(
                            event = item,
                            onDelete = { viewModel.deleteRecord(item) }
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
}

@Composable
fun SwipeToDeleteEventItem(
    event: DetailedEvent,
    onDelete: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "swipe_offset")
    val density = LocalDensity.current
    val actionWidth = with(density) { 80.dp.toPx() } // 删除按钮的宽度

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(Color.White)
    ) {
        // 底层操作区 (删除按钮)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(80.dp)
                .background(Color.Red.copy(alpha = 0.8f))
                .clickable {
                    offsetX = 0f // 重置位置
                    onDelete()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.label_delete),
                tint = Color.White
            )
        }

        // 上层内容区
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .fillMaxWidth()
                .background(Color.White)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        // 限制滑动范围：只能向左滑出 actionWidth 宽度
                        val newOffset = (offsetX + delta).coerceIn(-actionWidth, 0f)
                        offsetX = newOffset
                    },
                    onDragStopped = {
                        // 超过一半宽度则固定展开，否则收回
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
