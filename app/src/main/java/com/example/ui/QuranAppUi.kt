package com.example.ui

import android.widget.Toast
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.*
import com.example.viewmodel.QuranViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranAppUi(viewModel: QuranViewModel) {
    val navController = rememberNavController()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.darkMode.collectAsStateWithLifecycle()

    val startDestination = if (onboardingCompleted) "main" else "splash"

    MyApplicationTheme(darkTheme = isDarkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(navController = navController, startDestination = startDestination) {
                composable("splash") {
                    SplashScreen(viewModel, navController)
                }
                composable("onboarding") {
                    OnboardingScreen(viewModel, navController)
                }
                composable("setup") {
                    SetupScreen(viewModel, navController)
                }
                composable("main") {
                    MainScreen(viewModel, navController)
                }
                composable(
                    route = "surah_detail/{surahNumber}",
                    arguments = listOf(navArgument("surahNumber") { type = NavType.IntType })
                ) { backStackEntry ->
                    val surahNumber = backStackEntry.arguments?.getInt("surahNumber") ?: 1
                    SurahDetailScreen(viewModel, surahNumber, navController)
                }
            }
        }
    }
}

// ---------------- SPLASH SCREEN ----------------
@Composable
fun SplashScreen(viewModel: QuranViewModel, navController: NavHostController) {
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = taylorSpring(3000),
        label = "splashAlpha"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        kotlinx.coroutines.delay(3500)
        if (onboardingCompleted) {
            navController.navigate("main") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("onboarding") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ElegantDarkBackground,
                        ElegantDarkSurface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer(alpha = alphaAnim)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_splash_logo),
                contentDescription = "Infinity Quran logo",
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(32.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Infinity quran",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Read • Listen • Learn • Memorize",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "The Holy Quran Offline",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Custom spring configuration helper
fun <T> taylorSpring(duration: Int) = spring<T>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)

// ---------------- ONBOARDING SCREEN ----------------
@Composable
fun OnboardingScreen(viewModel: QuranViewModel, navController: NavHostController) {
    var activeStep by remember { mutableStateOf(0) }
    val steps = listOf(
        OnboardingStep(
            title = "📖 Read the Holy Quran",
            descBangla = "সম্পূর্ণ আল-কুরআন অফলাইনে পড়ুন বাংলা এবং ইংরেজি অনুবাদ সহ।",
            descEnglish = "Read the Holy Quran with offline Bangla and English translations, word meanings, and customized scripts."
        ),
        OnboardingStep(
            title = "🎧 Listen to Recitations",
            descBangla = "মনোমুগ্ধকর অডিও তেলাওয়াত শুনুন এবং অফলাইনে শোনার জন্য ডাউনলোড করুন।",
            descEnglish = "Listen to beautiful and serene recitations. Support background playback, loop repeats, and offline files."
        ),
        OnboardingStep(
            title = "🕌 Islamic Toolset",
            descBangla = "নামাজের সময়সূচী, কিবলা কম্পাস, ডিজিটাল তাসবীহ এবং দৈনিক দোয়া।",
            descEnglish = "Access daily prayer times with countdowns, dynamic Qibla compass, Tasbih counters, and authentic Duas."
        ),
        OnboardingStep(
            title = "⭐ Bookmark & Notes",
            descBangla = "আপনার পড়ার অগ্রগতি এবং আয়াতগুলোতে নিজের প্রয়োজনীয় নোট সংরক্ষণ করুন।",
            descEnglish = "Keep track of your memorization progress, save readings instantly, and write notes on any Ayah offline."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .navigationBarsPadding()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Skip Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { navController.navigate("setup") }) {
                Text("Skip", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
            }
        }

        // Illustration Center Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // Background image center
                Image(
                    painter = painterResource(id = R.drawable.img_splash_logo),
                    contentDescription = "Onboarding background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.3f
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = when (activeStep) {
                            0 -> Icons.Default.MenuBook
                            1 -> Icons.Default.Headset
                            2 -> Icons.Default.CompassCalibration
                            else -> Icons.Default.Bookmark
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = steps[activeStep].title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Translation Cards
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = steps[activeStep].descBangla,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = steps[activeStep].descEnglish,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal,
                lineHeight = 20.sp
            )
        }

        // Indicator & Bottom Button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.Center) {
                steps.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (index == activeStep) 16.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == activeStep) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (activeStep < steps.size - 1) {
                        activeStep++
                    } else {
                        navController.navigate("setup")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (activeStep < steps.size - 1) "Next" else "Continue Setup",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class OnboardingStep(
    val title: String,
    val descBangla: String,
    val descEnglish: String
)

// ---------------- SETUP & PREFERENCES SCREEN ----------------
@Composable
fun SetupScreen(viewModel: QuranViewModel, navController: NavHostController) {
    val currentLang by viewModel.appLanguage.collectAsStateWithLifecycle()
    val translationOpt by viewModel.translationOption.collectAsStateWithLifecycle()

    var showPermissionsRequestedAlert by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Preferences Setup",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "আপনার পছন্দসমূহ নির্বাচন করুন (Choose preferences)",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Language Selection
            Text(
                text = "১. App Language (ভাষা)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Pair("bn", "বাংলা"),
                    Pair("en", "English"),
                    Pair("ar", "العربية")
                ).forEach { (code, label) ->
                    val isSelected = currentLang == code
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setAppLanguage(code) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                            Text(label, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Translation preferences
            Text(
                text = "২. Translations (অনুবাদসমূহ)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            listOf(
                Pair("both", "Arabic + Bangla + English"),
                Pair("ar_bn", "Arabic + Bangla Translation"),
                Pair("ar_en", "Arabic + English Translation"),
                Pair("ar_only", "Arabic Only (শুধু আরবি)")
            ).forEach { (code, label) ->
                val isSelected = translationOpt == code
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { viewModel.setTranslationOption(code) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { viewModel.setTranslationOption(code) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Optional Audio Download Selection
            Text(
                text = "৩. Audio Offline Download (অডিও ডাউনলোড)",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "অডিও তেলাওয়াত ডাউনলোড করতে চান?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "আপনি চাইলে পরবর্তীতে সূরা ডিটেইল থেকেও প্রতিটি আয়াতের অডিও প্লে করতে পারবেন।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                showPermissionsRequestedAlert = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Download Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                showPermissionsRequestedAlert = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Download Later", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Complete Button
        Button(
            onClick = {
                viewModel.completeOnboarding()
                navController.navigate("main") {
                    popUpTo("setup") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Complete & Start Reading", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showPermissionsRequestedAlert) {
        AlertDialog(
            onDismissRequest = { showPermissionsRequestedAlert = false },
            title = { Text("Grant Access Permissions") },
            text = {
                Text("Infinity Quran requires Notification, Storage (for offline audios), and Location (for exact Prayer Times & Qibla compass) permissions. Please accept the system triggers.")
            },
            confirmButton = {
                TextButton(onClick = { showPermissionsRequestedAlert = false }) {
                    Text("Grant")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionsRequestedAlert = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ---------------- MAIN CONTAINER SCREEN ----------------
@Composable
fun MainScreen(viewModel: QuranViewModel, navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }
    val isDarkTheme by viewModel.darkMode.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                listOf(
                    Triple(0, "Home", Icons.Default.Home),
                    Triple(1, "Quran", Icons.Default.Book),
                    Triple(2, "Search", Icons.Default.Search),
                    Triple(3, "Library", Icons.Default.Bookmark),
                    Triple(4, "Settings", Icons.Default.Settings)
                ).forEach { (idx, label, icon) ->
                    NavigationBarItem(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(viewModel, navController)
                1 -> QuranTabScreen(viewModel, navController)
                2 -> SearchTabScreen(viewModel, navController)
                3 -> LibraryTabScreen(viewModel, navController)
                4 -> SettingsTabScreen(viewModel)
            }
        }
    }
}

// ---------------- HOME TAB SCREEN ----------------
@Composable
fun HomeScreen(viewModel: QuranViewModel, navController: NavHostController) {
    val dailyAyahPair by viewModel.dailyAyah.collectAsStateWithLifecycle()
    val dailyDuaObj by viewModel.dailyDua.collectAsStateWithLifecycle()
    val prayerTimesMap by viewModel.prayerTimes.collectAsStateWithLifecycle()
    val nextPrName by viewModel.nextPrayerName.collectAsStateWithLifecycle()
    val nextPrCountdown by viewModel.nextPrayerCountdown.collectAsStateWithLifecycle()
    val lastReadHist by viewModel.readingHistory.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showQiblaModal by remember { mutableStateOf(false) }
    var showTasbihModal by remember { mutableStateOf(false) }
    var showNamesModal by remember { mutableStateOf(false) }
    var showDuaModal by remember { mutableStateOf(false) }
    var showHadithModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Infinity quran",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "আলোকিত হোক আপনার দিনটি (Have a blessed day)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            IconButton(
                onClick = { viewModel.toggleDarkMode() },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(
                    imageVector = if (viewModel.darkMode.collectAsState().value) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Dark Mode",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Elegant Dark Hero / Continue Reading Card
        val hasHistory = lastReadHist != null
        val targetSurahNumber = if (hasHistory) lastReadHist?.surahNumber ?: 18 else 18
        val labelText = if (hasHistory) "Continue Reading" else "Featured Surah"
        val badgeText = if (hasHistory) "History" else "Juz 15"
        val cardTitle = if (hasHistory) lastReadHist?.surahName ?: "Al-Kahf" else "Al-Kahf"
        val cardSubtitle = if (hasHistory) "Ayah ${lastReadHist?.ayahNumber}" else "Ayah 10 • Page 293"
        val buttonText = if (hasHistory) "Resume Now" else "Read Now"

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navController.navigate("surah_detail/$targetSurahNumber")
                },
            colors = CardDefaults.cardColors(containerColor = ElegantDarkGreen),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Background big icon aligned bottom-right
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier
                        .size(130.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 12.dp, y = 12.dp)
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = labelText.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantLightGreen.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(ElegantSage)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantDarkTextOnSage
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = cardTitle,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = cardSubtitle,
                        fontSize = 14.sp,
                        color = ElegantLightGreen.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress bar similar to HTML
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (hasHistory) 0.45f else 0.65f)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.White)
                            .clickable {
                                navController.navigate("surah_detail/$targetSurahNumber")
                            }
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = buttonText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElegantDarkTextOnSage
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Access Tool Grid (Prayer Qibla, Tasbih etc)
        Text(
            text = "Quick Tools (ইসলামিক সেবা)",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("Qibla Compass", Icons.Default.CompassCalibration, { showQiblaModal = true }),
                Triple("Digital Tasbih", Icons.Default.Add, { showTasbihModal = true }),
                Triple("Names of Allah", Icons.Default.Stars, { showNamesModal = true })
            ).forEach { (label, icon, onClick) ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onClick() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("Daily Duas", Icons.Default.MenuBook, { showDuaModal = true }),
                Triple("Hadith Corner", Icons.Default.VolunteerActivism, { showHadithModal = true })
            ).forEach { (label, icon, onClick) ->
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onClick() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Prayer times list card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Clock", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Prayer Times (নামাজের সময়সূচী)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Text(
                        text = "$nextPrName in $nextPrCountdown",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    prayerTimesMap.forEach { (name, time) ->
                        val isActive = name == nextPrName
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 6.dp)
                        ) {
                            Text(name, fontSize = 11.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium)
                            Text(time, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Daily Ayah card
        dailyAyahPair?.let { (surah, ayah) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Ayah (আজকের আয়াত)",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp
                        )
                        Row {
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString("${ayah.text}\n${ayah.bangla}\n${ayah.english}\n[Surah ${surah.englishName} : Ayah ${ayah.number}]"))
                                Toast.makeText(context, "Ayah Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Ayah", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = ayah.text,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 32.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = ayah.bangla,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = ayah.english,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "— Surah ${surah.englishName} (${surah.banglaName}) : Ayah ${ayah.number}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // ---------------- MODALS / TOOL OVERLAYS ----------------

    // 1. Qibla Compass Modal
    if (showQiblaModal) {
        val qAngle by viewModel.qiblaAngle.collectAsStateWithLifecycle()
        Dialog(onDismissRequest = { showQiblaModal = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Qibla Compass (কিবলা নির্দেশক)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(contentAlignment = Alignment.Center) {
                        // Outer compass plate
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // Dial markings
                            Text("N", modifier = Modifier.align(Alignment.TopCenter).padding(8.dp), fontWeight = FontWeight.Bold, color = Color.Red)
                            Text("S", modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp), fontWeight = FontWeight.Bold)
                            Text("E", modifier = Modifier.align(Alignment.CenterEnd).padding(8.dp), fontWeight = FontWeight.Bold)
                            Text("W", modifier = Modifier.align(Alignment.CenterStart).padding(8.dp), fontWeight = FontWeight.Bold)
                        }
                        // Rotating needle
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Needle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(84.dp)
                                .rotate(qAngle)
                        )
                        // Center dot representing Kaba
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Kaba Direction: ${qAngle.toInt()}°",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "কম্পাসটি আপনার ফোনের সেন্সরের সাহায্যে কিবলাহ নির্দেশ করে।",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showQiblaModal = false }) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // 2. Tasbih Counter Modal
    if (showTasbihModal) {
        val tCount by viewModel.tasbihCount.collectAsStateWithLifecycle()
        val tTarget by viewModel.tasbihTargetName.collectAsStateWithLifecycle()
        val tasbihHistoryList by viewModel.tasbihHistory.collectAsStateWithLifecycle()

        Dialog(onDismissRequest = { showTasbihModal = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Digital Tasbih (ডিজিটাল তাসবীহ)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Phrase Selection Dropdown simulation
                    listOf(
                        "SubhanAllah (সুবহানাল্লাহ)",
                        "Alhamdulillah (আলহামদুলিল্লাহ)",
                        "Allahu Akbar (আল্লাহু আকবার)",
                        "La ilaha illallah (লা ইলাহা ইল্লাল্লাহ)"
                    ).forEach { phrase ->
                        val isSelected = tTarget == phrase
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.changeTasbihPhrase(phrase) }
                                .padding(12.dp)
                        ) {
                            Text(phrase, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Circular Tap Area
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { viewModel.incrementTasbih() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(tCount.toString(), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("TAP", fontSize = 12.sp, color = Color.Black.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(onClick = { viewModel.resetTasbih() }) {
                            Text("Reset")
                        }
                        Button(onClick = {
                            if (tCount > 0) {
                                viewModel.saveTasbihProgress()
                                Toast.makeText(context, "Tasbih Saved to History!", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("Save Count")
                        }
                    }

                    // Simple History List
                    if (tasbihHistoryList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Tasbih History", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        tasbihHistoryList.take(3).forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.targetName, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text("${item.count} counts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showTasbihModal = false }) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // 3. Names of Allah Modal
    if (showNamesModal) {
        Dialog(onDismissRequest = { showNamesModal = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("99 Names of Allah (আল্লাহর পবিত্র নাম)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 12.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(StaticIslamicData.namesOfAllah) { name ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "${name.number}. ${name.pronunciation}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(text = "অর্থ: ${name.banglaMeaning}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text(text = name.explanation, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), modifier = Modifier.padding(top = 4.dp))
                                    }
                                    Text(text = name.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    Button(onClick = { showNamesModal = false }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp)) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // 4. Daily Duas Modal
    if (showDuaModal) {
        Dialog(onDismissRequest = { showDuaModal = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Daily Duas (প্রতিদিনের প্রয়োজনীয় দোয়া)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 12.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(StaticIslamicData.dailyDuas) { dua ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(dua.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(dua.arabic, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(dua.pronunciation, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("বাংলা অর্থ: ${dua.meaningBangla}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("English: ${dua.meaningEnglish}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("সূত্র: ${dua.source}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Button(onClick = { showDuaModal = false }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp)) {
                        Text("Close")
                    }
                }
            }
        }
    }

    // 5. Hadith Corner Modal
    if (showHadithModal) {
        Dialog(onDismissRequest = { showHadithModal = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Hadith Corner (নির্বাচিত হাদিস)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 12.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(StaticIslamicData.hadiths) { hadith ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("বর্ণনাকারী: ${hadith.narrator}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("“ ${hadith.textBangla} ”", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("“ ${hadith.textEnglish} ”", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("সূত্র: ${hadith.source}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                    Button(onClick = { showHadithModal = false }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp)) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

// ---------------- QURAN TAB SCREEN ----------------
@Composable
fun QuranTabScreen(viewModel: QuranViewModel, navController: NavHostController) {
    val surahs by viewModel.surahs.collectAsStateWithLifecycle()
    val isLoading by viewModel.loadingSurahs.collectAsStateWithLifecycle()
    var searchQLive by remember { mutableStateOf("") }

    val filteredSurahs = remember(surahs, searchQLive) {
        if (searchQLive.isBlank()) surahs
        else {
            surahs.filter {
                it.englishName.lowercase().contains(searchQLive.lowercase()) ||
                        it.banglaName.contains(searchQLive) ||
                        it.name.contains(searchQLive) ||
                        it.number.toString() == searchQLive
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Al-Quran (আল-কুরআন)", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        Text("১১৪টি পূর্ণাঙ্গ সূরা এবং পারা ভিত্তিক সূচী (114 Surahs offline database)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQLive,
            onValueChange = { searchQLive = it },
            placeholder = { Text("Search Surah (সূরার নাম দিয়ে খুঁজুন)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
            trailingIcon = {
                if (searchQLive.isNotEmpty()) {
                    IconButton(onClick = { searchQLive = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredSurahs, key = { it.number }) { surah ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("surah_detail/${surah.number}")
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Rounded Index Badge
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = surah.number.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = surah.englishName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = surah.banglaName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = " • ${surah.revelationType} • ${surah.numberOfAyahs} Ayahs",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = surah.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                if (filteredSurahs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Surahs Found! (কোনো সূরা খুঁজে পাওয়া যায়নি)")
                        }
                    }
                }
            }
        }
    }
}

// ---------------- GLOBAL SEARCH TAB SCREEN ----------------
@Composable
fun SearchTabScreen(viewModel: QuranViewModel, navController: NavHostController) {
    val searchQ by viewModel.searchQuery.collectAsStateWithLifecycle()
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val searching by viewModel.searching.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Global Search (কুরআনের অনুসন্ধান)", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        Text("শব্দ, অনুবাদ অথবা আয়াত নম্বর দিয়ে সার্চ করুন (Search words, translations or ayah)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchQ,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Type keyword (যেমন: আল-হামদু, Praise)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
            trailingIcon = {
                if (searchQ.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results) { res ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("surah_detail/${res.surah.number}")
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Surah ${res.surah.englishName} (${res.surah.banglaName})",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (res.isSurahMatch) "Surah Level Match" else "Ayah ${res.ayah?.number}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            if (!res.isSurahMatch && res.ayah != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = res.ayah.text,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth(),
                                    lineHeight = 28.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = res.ayah.bangla,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = res.ayah.english,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
                if (results.isEmpty() && searchQ.isNotBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No match found! (কোনো ফলাফল পাওয়া যায়নি)")
                        }
                    }
                } else if (searchQ.isBlank()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Search dynamically across translations!", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

// ---------------- LIBRARY / BOOKMARKS TAB SCREEN ----------------
@Composable
fun LibraryTabScreen(viewModel: QuranViewModel, navController: NavHostController) {
    val bookmarksList by viewModel.bookmarks.collectAsStateWithLifecycle()
    val notesList by viewModel.allNotes.collectAsStateWithLifecycle()
    val tasbihHist by viewModel.tasbihHistory.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Bookmarks, 1: Notes, 2: Tasbih Log

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("My Library (সংরক্ষণাগার)", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        Text("বুকমার্ক, গুরুত্বপূর্ণ চিরকুট এবং তাসবীহ লগ (Saved bookmarks & reading journals)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))

        Spacer(modifier = Modifier.height(12.dp))

        // Custom library segments selector
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            listOf("Bookmarks", "My Notes", "Tasbih Log").forEachIndexed { idx, label ->
                Tab(
                    selected = activeSubTab == idx,
                    onClick = { activeSubTab = idx },
                    text = { Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeSubTab) {
            0 -> { // Bookmarks
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(bookmarksList) { bookmark ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("surah_detail/${bookmark.surahNumber}")
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bookmark, contentDescription = "Bookmark", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = "Surah ${bookmark.surahName}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(text = "Ayah Number ${bookmark.ayahNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    }
                                }
                                IconButton(onClick = {
                                    viewModel.toggleBookmark(bookmark.surahNumber, bookmark.ayahNumber, bookmark.surahName)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    }
                    if (bookmarksList.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                Text("No Saved Bookmarks! (কোনো বুকমার্ক নেই)")
                            }
                        }
                    }
                }
            }
            1 -> { // Notes
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notesList) { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("surah_detail/${note.surahNumber}")
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Surah ${note.surahName} [Ayah ${note.ayahNumber}]",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 14.sp
                                    )
                                    IconButton(onClick = { viewModel.deleteNote(note.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Note", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                                Text(
                                    text = note.noteText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = SimpleDateFormat("dd MMM, yyyy - hh:mm a", Locale.getDefault()).format(Date(note.timestamp)),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 8.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (notesList.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                Text("No Saved Notes! (কোনো চিরকুট নেই)")
                            }
                        }
                    }
                }
            }
            2 -> { // Tasbih history log
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tasbihHist) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(item.targetName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = SimpleDateFormat("dd MMM - hh:mm a", Locale.getDefault()).format(Date(item.timestamp)),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${item.count} counts",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    IconButton(onClick = { viewModel.deleteTasbihCount(item.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete log", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (tasbihHist.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                                Text("No Tasbih logs yet! (তাসবীহ লগের ইতিহাস খালি)")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- SETTINGS TAB SCREEN ----------------
@Composable
fun SettingsTabScreen(viewModel: QuranViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isDarkTheme by viewModel.darkMode.collectAsStateWithLifecycle()
    val fontSizeVal by viewModel.fontSize.collectAsStateWithLifecycle()
    val arabicFontVal by viewModel.arabicFont.collectAsStateWithLifecycle()
    val translationOpt by viewModel.translationOption.collectAsStateWithLifecycle()
    val currentLang by viewModel.appLanguage.collectAsStateWithLifecycle()

    fun openLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            clipboardManager.setText(AnnotatedString(url))
            Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(number: String) {
        try {
            // Clean number and build wa.me link
            val cleanNumber = number.replace("+", "").replace(" ", "")
            val formattedNumber = if (cleanNumber.startsWith("0")) "88$cleanNumber" else cleanNumber
            val url = "https://wa.me/$formattedNumber"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            clipboardManager.setText(AnnotatedString(number))
            Toast.makeText(context, "WhatsApp number copied to clipboard!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Settings (সেটিংসসমূহ)", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        Text("Configure settings, view developer and corporate profiles", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic dark mode
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Brightness4, contentDescription = "Dark mode", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Dark Mode (ডার্ক থিম)", fontWeight = FontWeight.Bold)
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { viewModel.toggleDarkMode() },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }
        }

        // Font size settings slider
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FormatSize, contentDescription = "Text Size", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Arabic Font Size: ${fontSizeVal.toInt()}sp", fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = fontSizeVal,
                    onValueChange = { viewModel.setFontSize(it) },
                    valueRange = 14f..36f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                // Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّহِيمِ",
                        fontSize = fontSizeVal.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Arabic font script style selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FontDownload, contentDescription = "Font Script", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Arabic Script Style", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Pair("uthmani", "Uthmani (মদীনা প্রিন্ট)"),
                        Pair("indopak", "IndoPak (নূরাণী প্রিন্ট)")
                    ).forEach { (code, label) ->
                        val isSelected = arabicFontVal == code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewModel.setArabicFont(code) }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ABOUT DEVELOPER CARD
        Text(
            text = "Developer Profile",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Developer",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Prince AR Abdur Rahman",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Independent App Developer",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Connect & Chat",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // WhatsApp Numbers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { openWhatsApp("01707424006") }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Phone, contentDescription = "WhatsApp", tint = ElegantSage, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WA: 01707424006", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { openWhatsApp("01796951709") }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Phone, contentDescription = "WhatsApp", tint = ElegantSage, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WA: 01796951709", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Social Media Connections
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable { openLink("https://www.facebook.com/share/1BNn32qoJo/") }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Link, contentDescription = "Facebook", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Facebook", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable { openLink("https://www.instagram.com/ur___abdur____rahman__2008") }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Link, contentDescription = "Instagram", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Instagram", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ABOUT COMPANY CARD
        Text(
            text = "Company Profile",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Company",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "NexVora Lab's Ofc",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Software Innovation Hub",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "NexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "OUR MISSION",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // TECHNICAL INFO & CREDITS CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Technical Information",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("App Version", fontSize = 12.sp)
                    Text("1.0.0", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Database Version", fontSize = 12.sp)
                    Text("v1.0.4 - Premium Offline", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Offline Quran Mode", fontSize = 12.sp)
                    Text("Enabled (১১৪ সূরা)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Credits",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Developed by Prince AR Abdur Rahman",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Published by NexVora Lab's Ofc",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "© 2026 NexVora Lab's Ofc. All Rights Reserved.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ---------------- SURAH DETAIL SCREEN ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahDetailScreen(viewModel: QuranViewModel, surahNumber: Int, navController: NavHostController) {
    val surahObj by viewModel.selectedSurah.collectAsStateWithLifecycle()
    val fontSizeVal by viewModel.fontSize.collectAsStateWithLifecycle()
    val translationOpt by viewModel.translationOption.collectAsStateWithLifecycle()

    // Audio Playback
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPlayingAyahNum by viewModel.currentPlayingAyah.collectAsStateWithLifecycle()

    // Hifz Memorization Modes
    val hifzHideArabic by viewModel.hifzHideArabic.collectAsStateWithLifecycle()
    val hifzHideTranslation by viewModel.hifzHideTranslation.collectAsStateWithLifecycle()

    // Notes adding dialog state
    var showNoteDialogForAyah by remember { mutableStateOf<Ayah?>(null) }
    var noteInputStr by remember { mutableStateOf("") }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val bookmarksList by viewModel.bookmarks.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Load selected surah metadata
    LaunchedEffect(surahNumber) {
        viewModel.selectSurah(surahNumber)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        surahObj?.englishName ?: "Surah Details",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.toggleAudioPlayback()
                    }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                            contentDescription = "Play/Pause Audio",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Panel Card
                surahObj?.let { surah ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = surah.name,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${surah.englishName} (${surah.banglaName})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(surah.revelationType) }
                                )
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("${surah.numberOfAyahs} Verses") }
                                )
                            }

                            // Hifz Mode Controllers Row
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Hifz Memorization Tools:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.toggleHifzHideArabic() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (hifzHideArabic) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                                        ),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(if (hifzHideArabic) "Show Arabic" else "Hide Arabic", fontSize = 10.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.toggleHifzHideTranslation() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (hifzHideTranslation) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                                        ),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(if (hifzHideTranslation) "Show Translation" else "Hide Translation", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Verses list view
                surahObj?.let { surah ->
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(surah.ayahs) { ayah ->
                            val isCurrentPlaying = currentPlayingAyahNum == ayah.number
                            val isBookmarked = bookmarksList.any { it.surahNumber == surah.number && it.ayahNumber == ayah.number }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isCurrentPlaying) 2.dp else 0.dp,
                                        color = if (isCurrentPlaying) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrentPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                    else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Verse Top Action row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                ayah.number.toString(),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Row {
                                            IconButton(onClick = { viewModel.toggleAudioPlayback(ayah.number) }) {
                                                Icon(
                                                    imageVector = if (isCurrentPlaying && isPlaying) Icons.Default.PauseCircleOutline
                                                    else Icons.Default.PlayCircleOutline,
                                                    contentDescription = "Play Verse Audio",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(onClick = {
                                                viewModel.toggleBookmark(surah.number, ayah.number, surah.englishName)
                                            }) {
                                                Icon(
                                                    imageVector = if (isBookmarked) Icons.Default.Bookmark
                                                    else Icons.Default.BookmarkBorder,
                                                    contentDescription = "Bookmark",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(onClick = { showNoteDialogForAyah = ayah }) {
                                                Icon(
                                                    imageVector = Icons.Default.NoteAdd,
                                                    contentDescription = "Add Note"
                                                )
                                            }
                                            IconButton(onClick = {
                                                clipboardManager.setText(AnnotatedString("${ayah.text}\n${ayah.bangla}\n${ayah.english}\n[Surah ${surah.englishName} : Ayah ${ayah.number}]"))
                                                Toast.makeText(context, "Copied Verse!", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy"
                                                )
                                            }
                                        }
                                    }

                                    // Arabic text
                                    if (!hifzHideArabic) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = ayah.text,
                                            fontSize = fontSizeVal.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Right,
                                            modifier = Modifier.fillMaxWidth(),
                                            lineHeight = (fontSizeVal * 1.6f).sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // Translations
                                    if (!hifzHideTranslation) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        if (translationOpt == "both" || translationOpt == "ar_bn") {
                                            Text(
                                                text = ayah.bangla,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Medium,
                                                lineHeight = 22.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                        if (translationOpt == "both" || translationOpt == "ar_en") {
                                            Text(
                                                text = ayah.english,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Audio Player Controls floating bar at bottom
            if (currentPlayingAyahNum != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Volume", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Playing: Surah ${surahObj?.englishName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Ayah Number: $currentPlayingAyahNum",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.toggleAudioPlayback(null) }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                                    contentDescription = "Play/Pause",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.playPrevAyah() }) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev")
                            }
                            // Simulated Repeat status
                            IconButton(onClick = {
                                Toast.makeText(context, "Repeat Mode Enabled for this Verse!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Repeat, contentDescription = "Repeat", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.playNextAyah() }) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Next")
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Note Dialog
    showNoteDialogForAyah?.let { ayah ->
        AlertDialog(
            onDismissRequest = { showNoteDialogForAyah = null },
            title = { Text("Add Note (আয়াতে চিরকুট যুক্ত করুন)") },
            text = {
                Column {
                    Text("Ayah: ${ayah.number}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteInputStr,
                        onValueChange = { noteInputStr = it },
                        placeholder = { Text("এখানে আপনার মন্তব্য/নোট লিখুন...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (noteInputStr.isNotBlank()) {
                        viewModel.addNote(
                            surahNumber = surahNumber,
                            ayahNumber = ayah.number,
                            surahName = surahObj?.englishName ?: "",
                            noteText = noteInputStr
                        )
                        noteInputStr = ""
                        showNoteDialogForAyah = null
                        Toast.makeText(context, "Note saved successfully!", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialogForAyah = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
