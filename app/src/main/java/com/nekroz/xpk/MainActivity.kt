package com.nekroz.xpk

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

val BgBlack = Color(0xFF0A0A0F)
val GlowPurple = Color(0xFF6B21A8)
val SoftPurple = Color(0xFFAB47BC)
val GlassWhite = Color(0x1AFFFFFF)
val RedBtn = Color(0xFFFF4444)

fun smartUrl(input: String): String {
    val t = input.trim()
    return when {
        t.startsWith("http://") || t.startsWith("https://") -> t
        t.contains(" ") || (!t.contains(".")) -> "https://www.google.com/search?q=${t.replace(" ", "+")}"
        else -> "https://$t"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = BgBlack, surface = BgBlack)) {
                XPKApp()
            }
        }
    }
}

enum class Screen { SPLASH, HOME, BROWSER, TABS }

@Composable
fun XPKApp() {
    var screen by remember { mutableStateOf(Screen.SPLASH) }
    var currentUrl by remember { mutableStateOf("") }
    var tabs by remember { mutableStateOf(listOf<String>()) }

    when (screen) {
        Screen.SPLASH -> SplashScreen { screen = Screen.HOME }
        Screen.HOME -> HomeScreen(
            onNavigate = { url ->
                val final = smartUrl(url)
                currentUrl = final
                if (!tabs.contains(final)) tabs = tabs + final
                screen = Screen.BROWSER
            },
            onOpenTabs = { screen = Screen.TABS }
        )
        Screen.BROWSER -> BrowserScreen(
            url = currentUrl,
            onHome = { screen = Screen.HOME },
            onOpenTabs = { screen = Screen.TABS }
        )
        Screen.TABS -> TabsScreen(
            tabs = tabs,
            activeUrl = currentUrl,
            onSelect = { url -> currentUrl = url; screen = Screen.BROWSER },
            onClose = { screen = Screen.HOME }
        )
    }
}

// --- СПЛЭШ ---
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(600))
        scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        delay(1200)
        alpha.animateTo(0f, animationSpec = tween(400))
        onDone()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(BgBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(
                        Brush.radialGradient(listOf(SoftPurple, GlowPurple)),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Language, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("XPK Browser", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("by @NekrozDEV", color = SoftPurple, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(color = SoftPurple, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
        }
    }
}

// --- ГЛАВНЫЙ ЭКРАН ---
@Composable
fun HomeScreen(onNavigate: (String) -> Unit, onOpenTabs: () -> Unit) {
    var urlInput by remember { mutableStateOf("") }
    val placeholders = listOf("Поиск в Google...", "youtube.com", "Новости ИИ", "github.com")
    val placeholder = remember { mutableStateOf(placeholders[0]) }

    LaunchedEffect(Unit) {
        var i = 0
        while (true) {
            val text = placeholders[i % placeholders.size]
            for (j in 0..text.length) { placeholder.value = text.substring(0, j); delay(70) }
            delay(1800)
            for (j in text.length downTo 0) { placeholder.value = text.substring(0, j); delay(35) }
            delay(300)
            i++
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(BgBlack)
    ) {
        // Фоновое свечение
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.Center)
                .offset(y = (-60).dp)
                .background(
                    Brush.radialGradient(listOf(GlowPurple.copy(alpha = 0.4f), Color.Transparent)),
                )
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Text("XPK Browser", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(60.dp))

            // Поле поиска
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(GlassWhite)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    placeholder = { Text(placeholder.value, color = Color.Gray, fontSize = 15.sp) }
                )
                IconButton(
                    onClick = { onNavigate(if (urlInput.isNotBlank()) urlInput else "https://google.com") },
                    modifier = Modifier.size(40.dp).background(SoftPurple, RoundedCornerShape(20.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Нижняя панель
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(GlassWhite)
                .padding(vertical = 10.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.Gray)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray)
            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
            Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = Color.White)
            IconButton(onClick = onOpenTabs, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.FilterNone, contentDescription = null, tint = Color.White)
            }
        }
    }
}

// --- БРАУЗЕР ---
@Composable
fun BrowserScreen(url: String, onHome: () -> Unit, onOpenTabs: () -> Unit) {
    var isMinimized by remember { mutableStateOf(false) }
    var isDesktopMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(BgBlack)) {
        AndroidView(
            factory = {
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            isLoading = true
                        }
                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Индикатор загрузки
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter)
                    .background(Brush.horizontalGradient(listOf(GlowPurple, SoftPurple)))
            )
        }

        // Панель управления
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            if (isMinimized) {
                IconButton(
                    onClick = { isMinimized = false },
                    modifier = Modifier.size(48.dp).background(GlassWhite, RoundedCornerShape(24.dp))
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Color.White)
                }
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(BgBlack.copy(alpha = 0.95f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isMinimized = true },
                        modifier = Modifier.size(40.dp).background(RedBtn, RoundedCornerShape(20.dp))
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onHome) { Icon(Icons.Default.Home, contentDescription = null, tint = Color.White) }
                    IconButton(onClick = { isDesktopMode = !isDesktopMode }) {
                        Icon(if (isDesktopMode) Icons.Default.PhoneAndroid else Icons.Default.Computer, contentDescription = null, tint = Color.White)
                    }
                    IconButton(onClick = onOpenTabs) { Icon(Icons.Default.FilterNone, contentDescription = null, tint = Color.White) }
                }
            }
        }
    }
}

// --- ВКЛАДКИ ---
@Composable
fun TabsScreen(tabs: List<String>, activeUrl: String, onSelect: (String) -> Unit, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().background(BgBlack).padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Вкладки (${tabs.size})", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.White) }
        }

        if (tabs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Нет открытых вкладок", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tabs) { tabUrl ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (tabUrl == activeUrl) GlowPurple.copy(alpha = 0.4f) else GlassWhite)
                            .clickable { onSelect(tabUrl) }
                            .padding(16.dp)
                            .aspectRatio(0.9f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(tabUrl, color = Color.White, fontSize = 11.sp, maxLines = 3)
                    }
                }
            }
        }
