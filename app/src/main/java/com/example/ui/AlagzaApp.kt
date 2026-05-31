package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ActivityLog
import com.example.data.model.ChatMessage
import com.example.data.model.SavedNote
import com.example.data.model.UserProfile
import kotlin.math.sin

// --- Custom Theme Colors for Alagza Ultra Premium ---
val SpaceDarkBg = Color(0xFF090A1A)
val NeonCyan = Color(0xFF00F0FF)
val NeonViolet = Color(0xFF9D00FF)
val NeonPink = Color(0xFFFF007F)
val GlassWhite = Color(0x15FFFFFF)
val GlassWhiteBorder = Color(0x30FFFFFF)
val MutedSlate = Color(0xFF6B7280)

@Composable
fun AlagzaApp(viewModel: AlagzaViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val isDarkMode = userProfile.isDarkMode
    val bgModifier = Modifier
        .fillMaxSize()
        .background(
            if (isDarkMode) {
                Brush.verticalGradient(
                    colors = listOf(
                        SpaceDarkBg,
                        Color(0xFF0F0C20),
                        Color(0xFF050510)
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE6F3FF),
                        Color(0xFFF3E8FF),
                        Color(0xFFFFFFFF)
                    )
                )
            }
        )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = bgModifier
                .padding(innerPadding)
                .testTag("alagza_root_layout")
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    "welcome" -> WelcomeScreen(viewModel)
                    "home" -> HomeScreen(viewModel)
                    "solver" -> SolverScreen(viewModel)
                    "scanner" -> ScannerScreen(viewModel)
                    "quiz" -> QuizScoreboardScreen(viewModel)
                    "notes" -> NotesScreen(viewModel)
                    "profile" -> ProfileScreen(viewModel)
                }
            }

            // Global floating quick-action AI assistant orb button
            if (currentScreen != "welcome" && currentScreen != "solver") {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 80.dp, end = 20.dp)
                ) {
                    FloatingAssistantOrb(
                        onClick = { viewModel.navigateTo("solver") }
                    )
                }
            }
        }
    }
}

// --- SCREEN 1: WELCOME / ENTRANCE ---
@Composable
fun WelcomeScreen(viewModel: AlagzaViewModel) {
    val isMusicPlaying by viewModel.isMusicPlaying.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    // Pulsing Animation for Futuristic AI Assistant Orb
    val infiniteTransition = rememberInfiniteTransition(label = "OrbPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbScale"
    )
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Glow"
    )

    var inputName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Science Student (PU)") }
    var showRegisterState by remember { mutableStateOf(false) }

    val studentTypes = listOf(
        "School Student",
        "11th & 12th Science",
        "Commerce Student",
        "Arts Student",
        "College Undergraduate",
        "Competitive Aspirant (JEE/NEET/UPSC)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Futuristic Ambient Soundwave Visualizer Control
        IconButton(
            onClick = { viewModel.toggleMusic() },
            modifier = Modifier
                .align(Alignment.End)
                .background(GlassWhite, RoundedCornerShape(12.dp))
                .border(1.dp, GlassWhiteBorder, RoundedCornerShape(12.dp))
                .testTag("welcome_music_toggle")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isMusicPlaying) Icons.Filled.GraphicEq else Icons.Filled.VolumeOff,
                    contentDescription = "Cosmic Background Core Drone",
                    tint = NeonCyan
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isMusicPlaying) "Mute Drone" else "Drone ON",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Floating Animated Neon AI Orb Assistant Representation
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(170.dp)
                .drawBehind {
                    drawCircle(
                        Brush.radialGradient(
                            colors = listOf(NeonCyan.copy(alpha = 0.5f), Color.Transparent),
                            radius = glowIntensity * 5f
                        )
                    )
                }
        ) {
            // Inner Core pulsing
            Box(
                modifier = Modifier
                    .size(100.dp * scale)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(NeonCyan, NeonViolet, NeonPink)
                        )
                    )
                    .border(
                        2.dp,
                        Color.White.copy(alpha = (scale - 0.2f).coerceIn(0.1f, 0.9f)),
                        CircleShape
                    )
            )

            // Outer floating orbit ring
            Canvas(modifier = Modifier.size(140.dp)) {
                drawCircle(
                    color = NeonPink,
                    radius = size.width / 2f + (scale * 8f),
                    style = Stroke(width = 4f),
                    alpha = 0.4f
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Title with elegant tracking and weight styling
        Text(
            text = "ALAGZA",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Your Intelligent AI Learning Universe",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                color = NeonCyan,
                fontWeight = FontWeight.Medium
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (!showRegisterState) {
            Text(
                text = "Welcome back! Access step-by-step math solutions, instant OCR scanning, voice teacher discussions, and bilingual translations.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(50.dp))

            // Premium Neon Action Button
            FuturisticNeonButton(
                text = "ENTER ECOSYSTEM",
                onClick = {
                    if (userProfile.name == "Guest Student") {
                        showRegisterState = true
                    } else {
                        viewModel.navigateTo("home")
                    }
                },
                modifier = Modifier
                    .testTag("enter_ecosystem_or_register")
                    .fillMaxWidth()
            )
        } else {
            // Register flow inside frosted glass card
            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Initialize Student Profile",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(15.dp))

                    TextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Enter Your Name", color = Color.White.copy(alpha = 0.6f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0x22FFFFFF),
                            unfocusedContainerColor = Color(0x11FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = NeonCyan,
                            unfocusedIndicatorColor = GlassWhiteBorder
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_name_input")
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    Text(
                        text = "Select Course Level",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // Scrollable level tags
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(studentTypes) { type ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selectedType == type) NeonViolet else GlassWhite,
                                        RoundedCornerShape(30.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (selectedType == type) NeonCyan else GlassWhiteBorder,
                                        RoundedCornerShape(30.dp)
                                    )
                                    .clickable { selectedType = type }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = type,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(25.dp))

                    Button(
                        onClick = {
                            val name = if (inputName.trim().isEmpty()) "Student Explorer" else inputName
                            viewModel.registerStudentProfile(name, selectedType)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("confirm_register_btn")
                    ) {
                        Text(
                            text = "START KNOWLEDGE STREAM",
                            color = SpaceDarkBg,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// --- SCREEN 2: STUDENT HOME DASHBOARD ---
@Composable
fun HomeScreen(viewModel: AlagzaViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val recentActivities by viewModel.recentActivities.collectAsStateWithLifecycle()
    var showQuickAskInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val subjects = listOf(
        Pair("Mathematics", Icons.Filled.Timeline),
        Pair("Physics", Icons.Filled.Lightbulb),
        Pair("Chemistry", Icons.Filled.GraphicEq),
        Pair("Biology", Icons.Filled.AutoAwesome),
        Pair("Accountancy", Icons.Filled.MenuBook),
        Pair("Economics", Icons.Filled.Timeline)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_layout")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App header with settings navigators
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Space Station : Alagza",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Hello, ${userProfile.name}",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    IconButton(
                        onClick = { viewModel.toggleAppTheme() },
                        modifier = Modifier.background(GlassWhite, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (userProfile.isDarkMode) Icons.Filled.WbSunny else Icons.Filled.NightsStay,
                            contentDescription = "Toggle Light Mode",
                            tint = if (userProfile.isDarkMode) Color.Yellow else NeonViolet
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateTo("profile") },
                        modifier = Modifier.background(GlassWhite, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Configure Persona",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Daily Motivational Quotes & Streak stats (Duolingo gamification)
        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "“The best way to predict your future is to program it.”",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.9f),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 12.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "- Alagza AI Oracle",
                            color = NeonPink,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .background(NeonPink.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, NeonPink, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Streak",
                                tint = NeonPink,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${userProfile.streak} Day Streak",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Interactive Stats Hub (Gamified experience)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Point counters
                FrostedGlassCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("KNOWLEDGE XP", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("${userProfile.totalPoints}", color = NeonCyan, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Rank: Quantum Cadet", color = Color.White, fontSize = 10.sp)
                    }
                }

                // Selected Language Indicator
                FrostedGlassCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("AI TRANSLATOR", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(userProfile.selectedLanguage, color = NeonViolet, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Auto translate replies", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }

        // Quick Input Ask AI Doubt Tool
        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Quick Ask AI Solver",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TextField(
                        value = showQuickAskInput,
                        onValueChange = { showQuickAskInput = it },
                        placeholder = { Text("Ask math step, physics law, account double-entry...", color = Color.LightGray, fontSize = 12.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0x11FFFFFF),
                            unfocusedContainerColor = Color(0x0aFFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = NeonCyan
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_quick_assistant_input"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (showQuickAskInput.trim().isNotEmpty()) {
                                keyboardController?.hide()
                                viewModel.setSolverSubject("Mathematics")
                                viewModel.askAlagzaDoubt(showQuickAskInput)
                                showQuickAskInput = ""
                                viewModel.navigateTo("solver")
                            }
                        })
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.triggerVoiceListening()
                                viewModel.navigateTo("solver")
                            },
                            modifier = Modifier
                                .background(GlassWhite, RoundedCornerShape(8.dp))
                                .border(1.dp, GlassWhiteBorder, RoundedCornerShape(8.dp))
                                .weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Mic, "Mic Doubt", tint = NeonCyan)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ask Vocal", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        IconButton(
                            onClick = { viewModel.navigateTo("scanner") },
                            modifier = Modifier
                                .background(GlassWhite, RoundedCornerShape(8.dp))
                                .border(1.dp, GlassWhiteBorder, RoundedCornerShape(8.dp))
                                .weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.PhotoCamera, "Scan Doubt", tint = NeonPink)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scan Book", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Category Courses Grid
        item {
            Text(
                text = "Academic Knowledge Orbits",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(subjects) { subj ->
                    Box(
                        modifier = Modifier
                            .size(115.dp)
                            .background(GlassWhite, RoundedCornerShape(16.dp))
                            .border(1.dp, GlassWhiteBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                viewModel.setSolverSubject(subj.first)
                                viewModel.navigateTo("solver")
                            }
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = subj.second,
                                contentDescription = subj.first,
                                tint = NeonCyan,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = subj.first,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Gamified Activities Menu
        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Gamified AI Portals",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CardItemQuick(
                            label = "Chapter Notes",
                            icon = Icons.Filled.MenuBook,
                            tint = NeonViolet,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo("notes") }
                        )

                        CardItemQuick(
                            label = "MCQ Arena",
                            icon = Icons.Filled.AutoAwesome,
                            tint = NeonCyan,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.navigateTo("quiz") }
                        )
                    }
                }
            }
        }

        // Offline activity logs (stored locally in Room Database!)
        item {
            Text(
                text = "Recent Activity Log (Offline Secured)",
                color = Color.LightGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (recentActivities.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No activities logged. Tap options to start learning with Alagza!", color = MutedSlate, fontSize = 11.sp)
                }
            }
        } else {
            items(recentActivities.take(5)) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x05FFFFFF), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                when (log.type) {
                                    "Note" -> NeonViolet
                                    "Quiz" -> NeonCyan
                                    "Scan" -> NeonPink
                                    "Ask" -> Color.Green
                                    else -> Color.White
                                },
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = log.description,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Timetable summary
        item {
            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🤖 Smart Timetable Reminder", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Next Goal: Study physics board diagrams and complete CET formula sheets (2 hrs remaining)", color = Color.White, fontSize = 11.sp)
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A L A G Z A   A C A D E M Y   V 1 . 0",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray
                )
            }
        }
    }
}

// --- SCREEN 3: SOLVER / CHATBOT SCREEN ---
@Composable
fun SolverScreen(viewModel: AlagzaViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val solverSubject by viewModel.solverSubject.collectAsStateWithLifecycle()
    val isExpertMode by viewModel.isExpertMode.collectAsStateWithLifecycle()
    val isAILoading by viewModel.isAILoading.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val subjects = listOf("Mathematics", "Physics", "Chemistry", "Biology", "Accountancy", "Economics")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("solver_screen_layout")
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    viewModel.stopSpeaking()
                    viewModel.navigateTo("home")
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Alagza Chat Tutor", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Subject: $solverSubject - ${userProfile.tutorTone}",
                    color = NeonCyan,
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = { viewModel.clearTutorChat() }
            ) {
                Icon(Icons.Filled.DeleteSweep, "Clear Chat", tint = NeonPink)
            }
        }

        // Control parameters (Subject list & Mode toggle)
        FrostedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Select subject
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(subjects) { s ->
                        Box(
                            modifier = Modifier
                                .background(
                                    if (solverSubject == s) NeonCyan.copy(alpha = 0.2f) else GlassWhite,
                                    RoundedCornerShape(30.dp)
                                )
                                .border(
                                    1.dp,
                                    if (solverSubject == s) NeonCyan else GlassWhiteBorder,
                                    RoundedCornerShape(30.dp)
                                )
                                .clickable { viewModel.setSolverSubject(s) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(s, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Beginner vs. Expert Explanation Depth:", color = Color.White, fontSize = 11.sp)
                    Switch(
                        checked = isExpertMode,
                        onCheckedChange = { viewModel.toggleExpertMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonCyan.copy(alpha = 0.4f),
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier
                            .scale(0.8f)
                            .testTag("expert_mode_switch")
                    )
                }
            }
        }

        // Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✨ Explain like a teacher mode ACTIVE. Feel free to speak your integration or organic chemistry query.",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            items(chatMessages) { msg ->
                ChatBubble(message = msg, onSpeak = { viewModel.speakText(msg.text) })
            }

            if (isAILoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = NeonCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Alagza compiling pedagogical blueprint...",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Vocal input pulsing notifier if active
        if (isListening) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF8B0000).copy(alpha = 0.6f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.GraphicEq, "Listening Node", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ALAGZA LISTENING VOCALLY... SPEAK NOW", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Input Tray
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Mic Button
            IconButton(
                onClick = { viewModel.triggerVoiceListening() },
                modifier = Modifier
                    .size(46.dp)
                    .background(NeonCyan.copy(alpha = 0.15f), CircleShape)
                    .border(1.dp, NeonCyan, CircleShape)
                    .testTag("solver_vocal_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = "Speak Doubts",
                    tint = NeonCyan
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text input
            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask equation, textbook chapter summary, notes...", color = Color.LightGray, fontSize = 12.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0x1aFFFFFF),
                    unfocusedContainerColor = Color(0x0aFFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = NeonCyan,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("solver_text_input"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (textInput.trim().isNotEmpty()) {
                        keyboardController?.hide()
                        viewModel.askAlagzaDoubt(textInput)
                        textInput = ""
                    }
                })
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Send Button
            IconButton(
                onClick = {
                    if (textInput.trim().isNotEmpty()) {
                        keyboardController?.hide()
                        viewModel.askAlagzaDoubt(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .background(NeonViolet, CircleShape)
                    .testTag("solver_text_send_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Send Doubt",
                    tint = Color.White
                )
            }
        }
    }
}

// --- SCREEN 4: OCR TEXTBOOK SCAN SCREEN ---
@Composable
fun ScannerScreen(viewModel: AlagzaViewModel) {
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanSolution by viewModel.scanSolution.collectAsStateWithLifecycle()
    val scanSubject by viewModel.scanSubject.collectAsStateWithLifecycle()

    val physicsProblem = "Calculate the magnetic field B at the center of a circular coil of 100 turns and 8cm radius carrying a current of 0.4A."
    val chemistryProblem = "Balance the oxidation-reduction reaction: MnO4- + Fe2+ + H+ -> Mn2+ + Fe3+ + H2O."
    val mathProblem = "Evaluate: Integral of x * sin(x) dx using Integration by Parts."

    val scanSubjects = listOf("Physics", "Chemistry", "Mathematics")

    // Laser Animation line generator
    val infiniteTransition = rememberInfiniteTransition(label = "LaserGlow")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("scanner_screen_layout")
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("home") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text("AI Smart Scan Assistant", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Scanner category
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            scanSubjects.forEach { s ->
                Box(
                    modifier = Modifier
                        .background(
                            if (scanSubject == s) NeonPink.copy(alpha = 0.2f) else GlassWhite,
                            RoundedCornerShape(30.dp)
                        )
                        .border(
                            1.dp,
                            if (scanSubject == s) NeonPink else GlassWhiteBorder,
                            RoundedCornerShape(30.dp)
                        )
                        .clickable { viewModel.setScanSubject(s) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(s, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large simulated camera viewfinder box with laser scanning line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.Black, RoundedCornerShape(16.dp))
                .border(2.dp, NeonPink, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                // Futuristic grid representation
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 30.dp.toPx()
                    for (x in 0..size.width.toInt() step step.toInt()) {
                        drawLine(Color(0x1E00F0FF), Offset(x.toFloat(), 0f), Offset(x.toFloat(), size.height))
                    }
                    for (y in 0..size.height.toInt() step step.toInt()) {
                        drawLine(Color(0x1E00F0FF), Offset(0f, y.toFloat()), Offset(size.width, y.toFloat()))
                    }

                    // Draw laser line
                    drawLine(
                        color = NeonPink,
                        start = Offset(0f, laserOffset.dp.toPx()),
                        end = Offset(size.width, laserOffset.dp.toPx()),
                        strokeWidth = 3.dp.toPx()
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonPink)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ALAGZA COMPILING MULTIMODAL HOLOGRAPH...", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            } else {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.CameraAlt, "Viewfinder Camera", tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Camera Viewfinder - $scanSubject Mode",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Select a simulated textbook problem below to snap & solve instantly with OCR multimodal intelligence:",
                        color = Color.White,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Simulated scanning problems buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val preloadedToAsk = when (scanSubject) {
                "Physics" -> physicsProblem
                "Chemistry" -> chemistryProblem
                else -> mathProblem
            }

            FrostedGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.simulateCameraScan(preloadedToAsk) }
                    .testTag("snap_trigger_button")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Lightbulb, "Physics Problem", tint = NeonCyan, modifier = Modifier.size(30.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("SNAP SELECTED QUESTION", color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(preloadedToAsk, color = Color.White, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Solution Box
        if (scanSolution.isNotEmpty()) {
            Text(
                text = "Holographic OCR Explanations Output:",
                color = NeonCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0x22110022), RoundedCornerShape(12.dp))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                item {
                    Text(text = scanSolution, color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

// --- SCREEN 5: MCQ ARENA / LEADERBOARD ---
@Composable
fun QuizScoreboardScreen(viewModel: AlagzaViewModel) {
    val quizzes by viewModel.quizzes.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentQuizIndex.collectAsStateWithLifecycle()
    val score by viewModel.quizScore.collectAsStateWithLifecycle()
    val selectedAnswerIndex by viewModel.quizSelectedAnswerIndex.collectAsStateWithLifecycle()
    val isAnswered by viewModel.isQuizAnswered.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.quizSubject.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.quizTimerSeconds.collectAsStateWithLifecycle()
    val isFinished by viewModel.isQuizFinished.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isQuizGenerating.collectAsStateWithLifecycle()

    val subjects = listOf("Mathematics", "Physics", "Chemistry", "Biology")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quiz_screen_layout")
            .padding(16.dp)
    ) {
        // Core Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("home") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text("AI MCQ Olympiad Arena", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(15.dp))

        if (quizzes.isEmpty() && !isGenerating) {
            // Setup quiz phase
            Text("Select Arena Subject Field:", color = Color.LightGray, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subjects.take(2).forEach { s ->
                    Box(
                        modifier = Modifier
                            .background(
                                if (selectedSubject == s) NeonCyan.copy(alpha = 0.2f) else GlassWhite,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (selectedSubject == s) NeonCyan else GlassWhiteBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.selectQuizSubject(s) }
                            .padding(16.dp)
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(s, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subjects.drop(2).forEach { s ->
                    Box(
                        modifier = Modifier
                            .background(
                                if (selectedSubject == s) NeonCyan.copy(alpha = 0.2f) else GlassWhite,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (selectedSubject == s) NeonCyan else GlassWhiteBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.selectQuizSubject(s) }
                            .padding(16.dp)
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(s, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Competitive Focus: JEE, NEET & CET Board Prep Mode", color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Each question contains a live 20-second cosmic atomic timer. Complete questions precisely to score maximum Knowledge Tokens!", color = Color.White, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            FuturisticNeonButton(
                text = "ENTER QUIZ PORTAL",
                onClick = { viewModel.startSubjectQuiz() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("launch_quiz_button")
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Mock Leaderboard
            Text("Global Alagza Leaderboard", color = Color.LightGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    LeaderboardRow("1st", "Aravind (NEET Prep)", "940 pts", NeonCyan)
                    LeaderboardRow("2nd", "Meera Hegde (UPSC basics)", "810 pts", NeonViolet)
                    LeaderboardRow("3rd", "Rohan S (JEE aspirant)", "760 pts", NeonPink)
                }
            }
        } else if (isGenerating) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonCyan)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Alagza generating custom MCQ set...", color = Color.White, fontSize = 14.sp)
                }
            }
        } else if (isFinished) {
            // Results screen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.AutoAwesome, "Success", tint = NeonCyan, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(15.dp))
                    Text("Learning Quiz Completed!", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your Score: $score / ${quizzes.size}",
                        color = NeonPink,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "You earned +${score * 10} knowledge variables. Keep pushing consistency daily!",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(25.dp))

                    Button(
                        onClick = { viewModel.navigateTo("home") },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Return to Dashboard", color = SpaceDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Active quiz game Question
            val currentQ = quizzes.getOrNull(currentIndex)
            if (currentQ != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${currentIndex + 1} of ${quizzes.size}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Timer gauge
                    Box(
                        modifier = Modifier
                            .background(
                                if (timerSeconds < 6) Color.Red.copy(alpha = 0.2f) else NeonViolet.copy(alpha = 0.2f),
                                RoundedCornerShape(30.dp)
                            )
                            .border(
                                1.dp,
                                if (timerSeconds < 6) Color.Red else NeonViolet,
                                RoundedCornerShape(30.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Timer: ${timerSeconds}s",
                            color = if (timerSeconds < 6) Color.Red else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Question Text in luxury frosted card
                FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = currentQ.question,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Options list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    currentQ.options.forEachIndexed { optIdx, op ->
                        val isThisSelected = selectedAnswerIndex == optIdx
                        val isCorrect = currentQ.answerIndex == optIdx

                        val optionBg = when {
                            !isAnswered -> if (isThisSelected) NeonCyan.copy(alpha = 0.15f) else GlassWhite
                            isCorrect -> Color.Green.copy(alpha = 0.2f)
                            isThisSelected -> Color.Red.copy(alpha = 0.2f)
                            else -> GlassWhite
                        }

                        val optionBorder = when {
                            !isAnswered -> if (isThisSelected) NeonCyan else GlassWhiteBorder
                            isCorrect -> Color.Green
                            isThisSelected -> Color.Red
                            else -> GlassWhiteBorder
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(optionBg, RoundedCornerShape(12.dp))
                                .border(1.dp, optionBorder, RoundedCornerShape(12.dp))
                                .clickable { viewModel.submitQuizAnswer(optIdx) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${'A' + optIdx}. $op",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Explanation & Next Button
                if (isAnswered) {
                    FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Explanation:", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(currentQ.explanation, color = Color.White, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.nextQuizQuestion() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("quiz_next_question_btn")
                    ) {
                        Text("Next Question Orbit", color = SpaceDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(rank: String, name: String, score: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(rank, color = color, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.width(36.dp))
            Text(name, color = Color.White, fontSize = 12.sp)
        }
        Text(score, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// --- SCREEN 6: REVISION NOTES & FLASHCARDS ---
@Composable
fun NotesScreen(viewModel: AlagzaViewModel) {
    val allNotes by viewModel.allNotes.collectAsStateWithLifecycle()
    val isGeneratingNote by viewModel.isGeneratingNote.collectAsStateWithLifecycle()
    val inputChapter by viewModel.notesInputChapter.collectAsStateWithLifecycle()
    val inputSubject by viewModel.notesInputSubject.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("saved") } // "create" or "saved" or "flashcard"
    val subjects = listOf("Mathematics", "Physics", "Chemistry", "Biology", "Accountancy", "Economics")

    // Flashcard local flip engine
    var isFlipped by remember { mutableStateOf(false) }
    var currentFlashIndex by remember { mutableStateOf(0) }

    val localFlashcards = listOf(
        Pair("What is the derivative of e^x?", "It is e^x. The function remains invariant under both integration and differentiation!"),
        Pair("What is the chemical formula of Benzene?", "C6H6, consisting of a planar ring with six carbon atoms bonded together in a hexagonal pattern."),
        Pair("State Ohm's Law formula.", "V = I * R, stating that current through a conductor is proportional to voltage.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("notes_screen_layout")
            .padding(16.dp)
    ) {
        // Core Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("home") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text("AI Scholar Worksheet", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Tabs Row (Saved vs Forge)
        TabRow(
            selectedTabIndex = when(activeTab) { "saved" -> 0; "create" -> 1; else -> 2 },
            containerColor = Color.Transparent,
            contentColor = NeonCyan
        ) {
            Tab(selected = activeTab == "saved", onClick = { activeTab = "saved" }) {
                Text("Saved Notes", color = Color.White, modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeTab == "create", onClick = { activeTab = "create" }) {
                Text("AI Generator", color = Color.White, modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Tab(selected = activeTab == "flashcard", onClick = { activeTab = "flashcard" }) {
                Text("Flashcards Deck", color = Color.White, modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        when (activeTab) {
            "saved" -> {
                if (allNotes.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.MenuBook, "Notes empty", tint = MutedSlate, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("No study notes saved offline. Use the AI page generator tag above to structure complete outlines!", color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(allNotes) { note ->
                            FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(note.title, color = NeonCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        IconButton(onClick = { viewModel.removeNote(note.id) }) {
                                            Icon(Icons.Filled.Delete, "Delete", tint = NeonPink)
                                        }
                                    }
                                    Text(
                                        text = "Category: ${note.subject}",
                                        color = NeonViolet,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(note.content, color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
            "create" -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Select Subject Domain:", color = Color.LightGray, fontSize = 12.sp)

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(subjects) { s ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (inputSubject == s) NeonViolet.copy(alpha = 0.2f) else GlassWhite,
                                        RoundedCornerShape(30.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (inputSubject == s) NeonViolet else GlassWhiteBorder,
                                        RoundedCornerShape(30.dp)
                                    )
                                    .clickable { viewModel.setNotesInputSubject(s) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(s, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Chapter Name / Subject Subsection:", color = Color.LightGray, fontSize = 12.sp)

                    TextField(
                        value = inputChapter,
                        onValueChange = { viewModel.setNotesInputChapter(it) },
                        placeholder = { Text("e.g. Periodic Classification, Quantum Kinetics, Integration Sheets", color = Color.LightGray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0x1aFFFFFF),
                            unfocusedContainerColor = Color(0x0aFFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = NeonCyan
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chapter_notes_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (isGeneratingNote) {
                        Row(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(color = NeonCyan)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Alagza Note Engine processing concepts...", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        FuturisticNeonButton(
                            text = "STRUCTURE CHAPTER NOTES",
                            onClick = { viewModel.generateChapterNotesAndSave() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_chapter_notes_btn")
                        )
                    }
                }
            }
            "flashcard" -> {
                // Interactive study flashcard component with simple card flip mechanics
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Interactive Flashcards Portal", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(15.dp))

                    val activeCard = localFlashcards.getOrNull(currentFlashIndex)
                    if (activeCard != null) {
                        AnimatedCardFlippedStyle(
                            question = activeCard.first,
                            answer = activeCard.second,
                            isFlipped = isFlipped,
                            onFlip = { isFlipped = !isFlipped }
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Button(
                                onClick = {
                                    isFlipped = false
                                    currentFlashIndex = if (currentFlashIndex > 0) currentFlashIndex - 1 else localFlashcards.size - 1
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GlassWhite)
                            ) {
                                Text("Previous", color = Color.White)
                            }

                            Button(
                                onClick = {
                                    isFlipped = false
                                    currentFlashIndex = (currentFlashIndex + 1) % localFlashcards.size
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                            ) {
                                Text("Next Deck", color = SpaceDarkBg, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedCardFlippedStyle(
    question: String,
    answer: String,
    isFlipped: Boolean,
    onFlip: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onFlip() },
        colors = CardDefaults.cardColors(
            containerColor = if (isFlipped) Color(0xFF1E1B4B) else Color(0x33CCCCCC)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (isFlipped) NeonPink else NeonCyan)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isFlipped) "AI SOLUTION ARCHITECTURE" else "ACADEMIC QUESTION SHEET",
                    color = if (isFlipped) NeonPink else NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isFlipped) answer else question,
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = if (isFlipped) FontWeight.Normal else FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "(Tap card to flip)",
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

// --- SCREEN 7: PROFILE & SETTINGS ---
@Composable
fun ProfileScreen(viewModel: AlagzaViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var textInputName by remember { mutableStateOf(userProfile.name) }
    var selectedLevel by remember { mutableStateOf(userProfile.studentType) }
    var selectedTone by remember { mutableStateOf(userProfile.tutorTone) }

    val languages = listOf("English", "Hindi", "Kannada")
    val studyTones = listOf("Friendly Teacher", "Expert Scientist", "Motivational")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_screen_layout")
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.navigateTo("home") }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Text("Station Control Command", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Profile Detail frosted segment
        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Edit Student Credentials", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                TextField(
                    value = textInputName,
                    onValueChange = { textInputName = it },
                    label = { Text("Display Name", color = Color.White.copy(alpha = 0.6f)) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0x1aFFFFFF),
                        unfocusedContainerColor = Color(0x0aFFFFFF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_profile_name_edit")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Academic Core Segment:", color = Color.White, fontSize = 12.sp)
                val options = listOf("School Student", "Science Student (PU)", "Commerce Student", "Competitive Aspirant")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(options) { o ->
                        Box(
                            modifier = Modifier
                                .background(
                                    if (selectedLevel == o) NeonViolet else GlassWhite,
                                    RoundedCornerShape(30.dp)
                                )
                                .border(
                                    1.dp,
                                    if (selectedLevel == o) NeonCyan else GlassWhiteBorder,
                                    RoundedCornerShape(30.dp)
                                )
                                .clickable { selectedLevel = o }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(o, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Language toggle
        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Primary Language Portal (Eng / हिंदी / ಕನ್ನಡ)", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Text("Alagza translates all responses automatically into your chosen tongue.", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    languages.forEach { l ->
                        val isSel = userProfile.selectedLanguage == l
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSel) NeonPink.copy(alpha = 0.2f) else GlassWhite,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSel) NeonPink else GlassWhiteBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.updateLanguage(l) }
                                .padding(16.dp)
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(l, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tutor personality setting
        FrostedGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("AI Assistant Tutor Persona", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                studyTones.forEach { t ->
                    val isToneSel = selectedTone == t
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTone = t }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isToneSel,
                            onClick = { selectedTone = t },
                            colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(t, color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                val up = userProfile.copy(
                    name = textInputName,
                    studentType = selectedLevel,
                    tutorTone = selectedTone
                )
                viewModel.registerStudentProfile(up.name, up.studentType)
                viewModel.updateTutorTone(up.tutorTone)
                viewModel.navigateTo("home")
            },
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_profile_btn")
        ) {
            Text("COMMIT SYSTEM PARAMETERS", color = SpaceDarkBg, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// --- UTILITY REUSABLE PREMIUM COMPONENTS ---

@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = GlassWhite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GlassWhiteBorder)
    ) {
        content()
    }
}

@Composable
fun FuturisticNeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.horizontalGradient(listOf(NeonCyan, NeonViolet)),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun FloatingAssistantOrb(
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(60.dp)
            .background(
                Brush.radialGradient(listOf(NeonCyan, NeonViolet)),
                CircleShape
            )
            .border(2.dp, Color.White, CircleShape)
            .testTag("floating_quick_orb_button")
    ) {
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = "Quick Tutor Ask Orb",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun CardItemQuick(
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(GlassWhite, RoundedCornerShape(12.dp))
            .border(1.dp, GlassWhiteBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ChatBubble(message: ChatMessage, onSpeak: () -> Unit) {
    val isUser = message.sender == "user"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .border(
                    1.dp,
                    if (isUser) NeonViolet.copy(alpha = 0.5f) else NeonCyan.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) Color(0xFF2E1065) else Color(0xFF0F172A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isUser) "You" else "Alagza Tutor Engine",
                    color = if (isUser) NeonPink else NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                // Voice assist triggers TTS speaker reads answers out loud
                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier
                            .align(Alignment.End)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VolumeUp,
                            contentDescription = "Read Doubt Out Loud",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
