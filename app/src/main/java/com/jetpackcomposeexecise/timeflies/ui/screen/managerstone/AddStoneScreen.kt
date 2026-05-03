package com.jetpackcomposeexecise.timeflies.ui.screen.managerstone

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jetpackcomposeexecise.timeflies.R

@Composable
fun AddStoneScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddStoneViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    AddStoneScreenContext(
        lifeEventInput = uiState.lifeEventInput,
        eventInput = uiState.eventInput,
        filteredLifeEvents = uiState.filteredLifeEvents,
        isDropdownExpanded = uiState.isDropdownExpanded,
        isSaveEnabled = uiState.isSaveEnabled,
        onLifeEventInputChanged = viewModel::onLifeEventInputChanged,
        onEventInputChanged = viewModel::onEventInputChanged,
        onLifeEventSelected = viewModel::onLifeEventSelected,
        onAddNewLifeEvent = viewModel::addNewLifeEvent,
        onSave = { viewModel.saveStone(onBack) },
        onBack = onBack,
        setDropdownExpanded = viewModel::setDropdownExpanded,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoneScreenContext(
    lifeEventInput: String,
    eventInput: String,
    filteredLifeEvents: List<com.jetpackcomposeexecise.timeflies.data.local.entity.LifeEventEntity>,
    isDropdownExpanded: Boolean,
    isSaveEnabled: Boolean,
    onLifeEventInputChanged: (String) -> Unit,
    onEventInputChanged: (String) -> Unit,
    onLifeEventSelected: (String) -> Unit,
    onAddNewLifeEvent: () -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    setDropdownExpanded: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_add_event),
                        fontSize = 24.sp,
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(100.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. lifeEvent 下拉框 / 输入框
                    Box(modifier = Modifier.weight(0.45f)) {
                        OutlinedTextField(
                            value = lifeEventInput,
                            onValueChange = onLifeEventInputChanged,
                            placeholder = { Text(stringResource(R.string.hint_input_life_event), color = Color.Gray) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        setDropdownExpanded(true)
                                    }
                                },
                            textStyle = TextStyle(fontSize = 18.sp),
                            shape = RoundedCornerShape(4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black
                            ),
                            trailingIcon = {
                                IconButton(onClick = { setDropdownExpanded(!isDropdownExpanded) }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }
                            }
                        )

                        DropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { setDropdownExpanded(false) },
                            modifier = Modifier.fillMaxWidth(0.8f) 
                        ) {
                            if (filteredLifeEvents.isEmpty() && lifeEventInput.isNotBlank()) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.hint_add_life_event)) },
                                    onClick = onAddNewLifeEvent
                                )
                            } else {
                                filteredLifeEvents.forEach { le ->
                                    DropdownMenuItem(
                                        text = { Text(le.lifeEvent) },
                                        onClick = { onLifeEventSelected(le.lifeEvent) }
                                    )
                                }
                            }
                        }
                    }

                    // 2. Event 输入框
                    OutlinedTextField(
                        value = eventInput,
                        onValueChange = onEventInputChanged,
                        placeholder = { Text(stringResource(R.string.hint_input_event), color = Color.Gray) },
                        modifier = Modifier.weight(0.55f),
                        textStyle = TextStyle(fontSize = 18.sp),
                        shape = RoundedCornerShape(4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black
                        )
                    )
                }
            }

            // 3. 【保存】按钮
            Button(
                onClick = onSave,
                enabled = isSaveEnabled,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, if (isSaveEnabled) Color(0xFF9162FA) else Color.LightGray)
            ) {
                Text(
                    text = stringResource(R.string.button_save),
                    fontSize = 20.sp,
                    color = if (isSaveEnabled) Color(0xFF9162FA) else Color.LightGray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddStoneScreenPreview() {
    AddStoneScreenContext(
        lifeEventInput = "",
        eventInput = "",
        filteredLifeEvents = emptyList(),
        isDropdownExpanded = false,
        isSaveEnabled = false,
        onLifeEventInputChanged = {},
        onEventInputChanged = {},
        onLifeEventSelected = {},
        onAddNewLifeEvent = {},
        onSave = {},
        onBack = {},
        setDropdownExpanded = {}
    )
}
