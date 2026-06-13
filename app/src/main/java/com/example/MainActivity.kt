package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ChatMessage
import com.example.data.InferenceSettings
import com.example.data.MessageSender
import com.example.data.StudentTask
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF070709)
                ) {
                    MainNavigationWrapper(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainNavigationWrapper(viewModel: ChatViewModel) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var showAddTaskDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF0D0D11),
                drawerContentColor = Color.White,
                modifier = Modifier.width(320.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // System Brand Details
                    Text(
                        text = "P² OpAIS CORE",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = Color(0xFF00F5D4),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                    Text(
                        text = "Low-Memory Student AI Agent Hub",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    HorizontalDivider(color = Color(0xFF1E1E26))

                    // WORKSPACE OPERATIONS
                    Text(
                        text = "WORKSPACE OPERATIONS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF9D4EDD),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.createNewSession()
                            scope.launch { drawerState.close() }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C24)),
                        border = BorderStroke(1.dp, Color(0xFF2E2E38)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("new_chat_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New chat session",
                            tint = Color(0xFF00F5D4),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Chat", style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showAddTaskDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C24)),
                        border = BorderStroke(1.dp, Color(0xFF2E2E38)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("new_task_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create new task",
                            tint = Color(0xFF9D4EDD),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Task", style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = Color(0xFF1E1E26))

                    // CHAT SESSION HISTORY
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SAVED CHATS HISTORY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.Gray
                        )
                        Text(
                            text = "${state.sessions.size} sessions",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.DarkGray
                        )
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(state.sessions) { s ->
                                val isSelected = s.id == state.activeSessionId
                                val itemBorder = if (isSelected) BorderStroke(1.dp, Color(0xFF00F5D4)) else null
                                val itemBg = if (isSelected) Color(0xFF13131A) else Color.Transparent

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(itemBg, RoundedCornerShape(6.dp))
                                        .clickable {
                                            viewModel.selectSession(s.id)
                                            scope.launch { drawerState.close() }
                                        }
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = s.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            color = if (isSelected) Color.White else Color.LightGray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${s.messages.size} Messages",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteSession(s.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete chat workspace",
                                            tint = Color.DarkGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TASK LIST COMPRESSION DISPLAY
                    Text(
                        text = "WORKFLOW ASSIGNMENTS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                    )

                    Box(modifier = Modifier.height(110.dp).fillMaxWidth()) {
                        if (state.tasks.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No pending student tasks current", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(state.tasks) { task ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF13131A), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = task.isCompleted,
                                            onCheckedChange = { viewModel.toggleTaskCompleted(task.id) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFF00F5D4),
                                                uncheckedColor = Color.DarkGray
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = task.title,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                                ),
                                                color = if (task.isCompleted) Color.Gray else Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = task.dueDate ?: "No Date",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF9D4EDD)
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteTask(task.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Delete task",
                                                tint = Color.DarkGray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1E1E26), modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))

                    // SYSTEM RAM DISPLAY (Estimated Device Monitor)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF141419), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "RAM ALLOCATION REPORT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.Gray
                            )
                            Text(
                                "${state.ramUsedMb} MB / ${state.ramTotalMb} MB",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (state.ramUsedMb > 1200) Color.Red else Color(0xFF00F5D4)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        val progress = state.ramUsedMb.toFloat() / state.ramTotalMb.toFloat()
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(4.dp),
                            color = if (state.ramUsedMb > 1200) Color.Red else Color(0xFF9D4EDD),
                            trackColor = Color(0xFF1F1F2A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (state.isLocalModelLoaded) "LOCAL MODEL MAPPED IN RAM" else "ONLINE ENGINE ACTIVE - RAM SAFE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = if (state.isLocalModelLoaded) Color(0xFF9D4EDD) else Color.Gray
                        )
                    }
                }
            }
        }
    ) {
        ChatScreen(
            viewModel = viewModel,
            onOpenMenu = { scope.launch { drawerState.open() } }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, date ->
                viewModel.addTask(title, date)
                showAddTaskDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onOpenMenu: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    var rawInputText by remember { mutableStateOf("") }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importGgufModel(uri)
        }
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "P² OpAIS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = if (state.isOnlineMode) "ONLINE • GEMINI ADVOCATE" else if (state.isLocalModelLoaded) "OFFLINE • LOCAL LCPP" else "OFFLINE • CORES UNALLOCATED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.isOnlineMode) Color(0xFF00F5D4) else if (state.isLocalModelLoaded) Color(0xFF9D4EDD) else Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onOpenMenu,
                        modifier = Modifier.testTag("hamburger_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open student console",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Optimize parameters config",
                            tint = if (state.localModelPath == null) Color.Gray else Color(0xFF9D4EDD)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0D0D11),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Column {
                if (state.modelImportProgress != null) {
                    LinearProgressIndicator(
                        progress = { state.modelImportProgress ?: 0f },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                        color = Color(0xFF00F5D4),
                        trackColor = Color(0xFF141419)
                    )
                    Text(
                        text = state.statusText ?: "Analyzing bytes block...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF00F5D4),
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                if (state.isGenerating && state.modelImportProgress == null) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(2.dp),
                        color = if (state.isOnlineMode) Color(0xFF00F5D4) else Color(0xFF9D4EDD),
                        trackColor = Color(0xFF070709)
                    )
                }

                ChatInputSection(
                    inputText = rawInputText,
                    onTextChanged = { rawInputText = it },
                    isGenerating = state.isGenerating,
                    onSendClicked = {
                        viewModel.sendMessage(rawInputText)
                        rawInputText = ""
                    }
                )
            }
        },
        containerColor = Color(0xFF070709)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            EngineToggleSection(
                isOnline = state.isOnlineMode,
                onModeSelected = { isOnline ->
                    viewModel.toggleMode(isOnline)
                }
            )

            // Inline progress alerts
            if (state.statusText != null && state.modelImportProgress == null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)),
                    border = BorderStroke(1.dp, Color(0xFF2E2E38)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF9D4EDD)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = state.statusText ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
                }
            }

            // Central Message Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.messages) { message ->
                    ChatMessageItem(message = message)
                }
            }
        }
    }

    if (showSettingsDialog) {
        LlamaSettingsDialog(
            settings = state.settings,
            currentPath = state.localModelPath,
            currentDir = state.localFolderLocation,
            onDismiss = { showSettingsDialog = false },
            onUpdateSettings = { viewModel.updateSettings(it) },
            onChangeDir = { viewModel.changeFolderLocation(it) },
            onSelectFile = { documentPickerLauncher.launch(arrayOf("*/*")) },
            onClearConversations = { viewModel.clearActiveSessionMessages() },
            onBenchmark = { viewModel.benchmarkLocalModelPerformance() }
        )
    }
}

@Composable
fun EngineToggleSection(
    isOnline: Boolean,
    onModeSelected: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0D11))
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val onlineColor = if (isOnline) Color(0xFF00F5D4) else Color(0xFF1C1C24)
        val onlineTextColor = if (isOnline) Color.Black else Color.Gray

        val offlineColor = if (!isOnline) Color(0xFF9D4EDD) else Color(0xFF1C1C24)
        val offlineTextColor = if (!isOnline) Color.White else Color.Gray

        Button(
            onClick = { onModeSelected(true) },
            colors = ButtonDefaults.buttonColors(
                containerColor = onlineColor,
                contentColor = onlineTextColor
            ),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .testTag("online_toggle_button"),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                "ONLINE (GEMINI)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Button(
            onClick = { onModeSelected(false) },
            colors = ButtonDefaults.buttonColors(
                containerColor = offlineColor,
                contentColor = offlineTextColor
            ),
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .testTag("offline_toggle_button"),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                "OFFLINE (LOCAL LCPP)",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER
    val isSystem = message.sender == MessageSender.SYSTEM

    val contentAlignment = when {
        isUser -> Alignment.End
        isSystem -> Alignment.CenterHorizontally
        else -> Alignment.Start
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = contentAlignment
    ) {
        if (isSystem) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E0E14)),
                border = BorderStroke(0.5.dp, Color(0xFF26262F)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    color = Color(0xFF00F5D4),
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            val bubbleColor = when (message.sender) {
                MessageSender.USER -> Color(0xFF13131A)
                MessageSender.AI_ONLINE -> Color(0xFF0E1A1A)
                MessageSender.AI_OFFLINE -> Color(0xFF130E1C)
                else -> Color.Transparent
            }

            val strokeColor = when (message.sender) {
                MessageSender.USER -> Color(0xFF2D2D3D)
                MessageSender.AI_ONLINE -> Color(0xFF1A3837)
                MessageSender.AI_OFFLINE -> Color(0xFF261D38)
                else -> Color.Transparent
            }

            val indicatorColor = when (message.sender) {
                MessageSender.USER -> Color.White
                MessageSender.AI_ONLINE -> Color(0xFF00F5D4)
                MessageSender.AI_OFFLINE -> Color(0xFF9D4EDD)
                else -> Color.Transparent
            }

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                if (!isUser) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(24.dp)
                            .background(indicatorColor, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = bubbleColor),
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isUser) 12.dp else 2.dp,
                        bottomEnd = if (isUser) 2.dp else 12.dp
                    ),
                    border = BorderStroke(1.dp, strokeColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = when (message.sender) {
                                MessageSender.USER -> "STUDENT PORTAL"
                                MessageSender.AI_ONLINE -> "CORE ADVISOR (GEMINI)"
                                MessageSender.AI_OFFLINE -> "LLAMA CORE (LOCAL CPU)"
                                else -> "SYSTEM TELEMETRY"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = indicatorColor.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        MessageContent(
                            text = message.text,
                            color = Color.White
                        )
                    }
                }

                if (isUser) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(24.dp)
                            .background(indicatorColor, RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun MessageContent(text: String, color: Color) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val parts = remember(text) { text.split("```") }

    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                val lines = part.trim().split("\n")
                val language = if (lines.isNotEmpty() && lines.first().length < 15 && !lines.first().contains(" ")) lines.first() else "CODE"
                val codeContent = if (language == "CODE") part.trim() else lines.drop(1).joinToString("\n").trim()

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070709)),
                    border = BorderStroke(1.dp, Color(0xFF26262F)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0E0E14))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = language.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = Color(0xFF00F5D4),
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(codeContent))
                                    Toast.makeText(context, "Code block copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Copy code snippet",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Text(
                            text = codeContent,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color(0xFFD4D4D8),
                            modifier = Modifier
                                .padding(12.dp)
                                .horizontalScroll(rememberScrollState())
                        )
                    }
                }
            } else {
                if (part.isNotBlank()) {
                    MarkdownText(text = part, color = color)
                }
            }
        }
    }
}

@Composable
fun MarkdownText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val finalAnnotatedString = remember(text) {
        buildAnnotatedString {
            var activeIdx = 0
            val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
            val matches = boldRegex.findAll(text)

            for (match in matches) {
                if (match.range.first > activeIdx) {
                    append(text.substring(activeIdx, match.range.first))
                }
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF9E92FF))) {
                    append(match.groupValues[1])
                }
                activeIdx = match.range.last + 1
            }

            if (activeIdx < text.length) {
                append(text.substring(activeIdx))
            }
        }
    }

    Text(
        text = finalAnnotatedString,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
        color = color
    )
}

@Composable
fun ChatInputSection(
    inputText: String,
    onTextChanged: (String) -> Unit,
    isGenerating: Boolean,
    onSendClicked: () -> Unit
) {
    Surface(
        color = Color(0xFF0D0D11),
        border = BorderStroke(1.dp, Color(0xFF1F1F2A)),
        modifier = Modifier.fillMaxWidth().navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        "Ask student workspace assistant...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF050507),
                    unfocusedContainerColor = Color(0xFF050507),
                    focusedBorderColor = Color(0xFF00F5D4),
                    unfocusedBorderColor = Color(0xFF26262F)
                ),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank() && !isGenerating) {
                            onSendClicked()
                        }
                    }
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSendClicked,
                enabled = inputText.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (inputText.isNotBlank() && !isGenerating) Color(0xFF26262F) else Color(0xFF070709),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .testTag("send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Submit student query",
                    tint = if (inputText.isNotBlank() && !isGenerating) Color(0xFF00F5D4) else Color.DarkGray
                )
            }
        }
    }
}

// Dialog to support creating beautiful workspace tasks
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("Today") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF121216),
            border = BorderStroke(1.dp, Color(0xFF32323D)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "CREATE ACADEMIC TASK",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Description", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00F5D4),
                        unfocusedBorderColor = Color(0xFF2E2E38)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("DUE DEADLINE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Today", "Tomorrow2", "Next Week").forEach { opt ->
                        val selected = dueDate == opt
                        Button(
                            onClick = { dueDate = opt },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) Color(0xFF9D4EDD) else Color(0xFF1C1C24)
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text(opt, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAddTask(title, dueDate)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4), contentColor = Color.Black),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("SAVE TASK")
                    }
                }
            }
        }
    }
}

// Dialog to configure advanced Offline Configuration Sheet requirements
@Composable
fun LlamaSettingsDialog(
    settings: InferenceSettings,
    currentPath: String?,
    currentDir: String,
    onDismiss: () -> Unit,
    onUpdateSettings: (InferenceSettings) -> Unit,
    onChangeDir: (String) -> Unit,
    onSelectFile: () -> Unit,
    onClearConversations: () -> Unit,
    onBenchmark: () -> Unit
) {
    var rawPrompt by remember { mutableStateOf(settings.systemPrompt) }
    var temperature by remember { mutableFloatStateOf(settings.temperature) }
    var minP by remember { mutableFloatStateOf(settings.minP) }
    var contextSize by remember { mutableIntStateOf(settings.contextSize) }
    var numThreads by remember { mutableIntStateOf(settings.numThreads) }
    var useMmap by remember { mutableStateOf(settings.useMmap) }
    var useMlock by remember { mutableStateOf(settings.useMlock) }
    var historyLimit by remember { mutableFloatStateOf(settings.contextHistoryLength.toFloat()) }

    var showDirDialog by remember { mutableStateOf(false) }
    var tempNewDir by remember { mutableStateOf(currentDir) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF121216),
            border = BorderStroke(1.dp, Color(0xFF32323D)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "ADVANCED OFFLINE LAUNCH PANEL",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                HorizontalDivider(color = Color(0xFF2C2C35), modifier = Modifier.padding(bottom = 12.dp))

                // Model weights path allocation status
                Text("MODEL SPECIFICATIONS", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9D4EDD))
                Spacer(modifier = Modifier.height(4.dp))
                if (currentPath != null) {
                    val file = remember(currentPath) { java.io.File(currentPath) }
                    Text(
                        text = "File: ${file.name}\nSize: ${(file.length() / (1024 * 1024))} MB",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        modifier = Modifier
                            .background(Color(0xFF0A0A0C), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "No GGUF file mounted offline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier
                            .background(Color(0xFF0A0A0C), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                            .fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // SYSTEM PROMPT TEXT FIELD
                Text("SYSTEM INSTRUCTION SCENE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = rawPrompt,
                    onValueChange = { rawPrompt = it },
                    textStyle = MaterialTheme.typography.bodySmall,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00F5D4),
                        unfocusedBorderColor = Color(0xFF2E2E38)
                    ),
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // TEMPERATURE SLIDER
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Temperature Selector", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(String.format("%.2f", temperature), style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
                Slider(
                    value = temperature,
                    onValueChange = { temperature = it },
                    valueRange = 0.0f..2.0f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFF9D4EDD),
                        thumbColor = Color(0xFF00F5D4)
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // MIN-P SAMPLING SLIDER
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Min-P Sampling Ratio", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(String.format("%.2f", minP), style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
                Slider(
                    value = minP,
                    onValueChange = { minP = it },
                    valueRange = 0.0f..1.0f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFF9D4EDD),
                        thumbColor = Color(0xFF00F5D4)
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // CONTEXT SIZE MULTI-BUTTON TOGGLE ROW
                Text("GPU CONTEXT LIMIT", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(512, 1024, 2048, 4048).forEach { size ->
                        val selected = contextSize == size
                        Button(
                            onClick = { contextSize = size },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) Color(0xFF9D4EDD) else Color(0xFF1C1C24)
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("$size", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // THREADS ALLOCATION STEPPER
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Active Thread Count", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("Matching hardware constraints", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.DarkGray)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { if (numThreads > 1) numThreads-- },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C24)),
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("-", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "$numThreads",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Button(
                            onClick = { if (numThreads < 12) numThreads++ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C24)),
                            modifier = Modifier.size(36.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // MMAP & MLOCK SWITCH TOGGLES
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Virtual Memory Map (use_mmap)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("Accelerates dynamic weight loads", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.DarkGray)
                    }
                    Switch(
                        checked = useMmap,
                        onCheckedChange = { useMmap = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00F5D4),
                            checkedTrackColor = Color(0xFF9D4EDD)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Memory Lock Pointer (use_mlock)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("Prevents OS recycling swapping", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.DarkGray)
                    }
                    Switch(
                        checked = useMlock,
                        onCheckedChange = { useMlock = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00F5D4),
                            checkedTrackColor = Color(0xFF9D4EDD)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // MESSAGE HISTORY TURN DEPTH LIMIT
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Inference Memory Turn Buffer", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("${historyLimit.toInt()} messages", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
                Slider(
                    value = historyLimit,
                    onValueChange = { historyLimit = it },
                    valueRange = 2f..20f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color(0xFF9D4EDD),
                        thumbColor = Color(0xFF00F5D4)
                    )
                )

                HorizontalDivider(color = Color(0xFF2C2C35), modifier = Modifier.padding(vertical = 12.dp))

                // DIRECT CORE UTILITIES
                Text("HARDWARE & MATRIX UTILITIES", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onSelectFile()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D4EDD)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("📁 SELECT OFFLINE MODEL (.GGUF)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = { showDirDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C24)),
                    border = BorderStroke(1.dp, Color(0xFF2E2E38)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("📂 CHANGE DIRECTORY LOCATION", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onBenchmark()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C24)),
                    border = BorderStroke(1.dp, Color(0xFF2E2E38)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("⚡ BENCHMARK CPU MATRIX WORK", style = MaterialTheme.typography.labelMedium, color = Color(0xFF00F5D4))
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        onDismiss()
                        onClearConversations()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C24)),
                    border = BorderStroke(1.dp, Color(0xFFD62828)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("🗑️ CLEAR SESSION MESSAGES (FREE RAM)", style = MaterialTheme.typography.labelMedium, color = Color(0xFFD62828))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onUpdateSettings(
                                InferenceSettings(
                                    systemPrompt = rawPrompt,
                                    temperature = temperature,
                                    minP = minP,
                                    contextSize = contextSize,
                                    numThreads = numThreads,
                                    useMmap = useMmap,
                                    useMlock = useMlock,
                                    contextHistoryLength = historyLimit.toInt()
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4), contentColor = Color.Black),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("SAVE CORES")
                    }
                }
            }
        }
    }

    if (showDirDialog) {
        Dialog(onDismissRequest = { showDirDialog = false }) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF1A1A1E),
                border = BorderStroke(1.dp, Color(0xFF3A3A4A)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("SET SEARCH DIRECTORY", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempNewDir,
                        onValueChange = { tempNewDir = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00F5D4),
                            unfocusedBorderColor = Color.DarkGray
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDirDialog = false }) {
                            Text("CANCEL", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onChangeDir(tempNewDir)
                                showDirDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4), contentColor = Color.Black)
                        ) {
                            Text("APPLY")
                        }
                    }
                }
            }
        }
    }
}
