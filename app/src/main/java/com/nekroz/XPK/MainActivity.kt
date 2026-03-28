package com.nekroz.xpk

// Project: XPK Browser
// Author: @NekrozDEV

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay

// --- ЦВЕТА И ТЕМА ---
val DarkGray = Color(0xFF1E1E1E)
val DeepPurple = Color(0xFF4A148C)
val PurpleAccent = Color(0xFFAB47BC)
val GlassSurface = Color(0x33FFFFFF)
val Red = Color(0xFFFF4444)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(background = DarkGray, surface = DarkGray)
            ) {
                XPKApp()
            }
        }
    }
}

// --- НАВИГАЦИЯ ---
enum class ScreenType { HOME, BROWSER, TABS }

@Composable
fun XPKApp() {
    var currentScreen by remember { mutableStateOf(ScreenType.HOME) }
    var currentUrl by remember { mutableStateOf("https://simple.art/design") }
    var openedTabs by remember { mutableStateOf(listOf("https://google.com", "https://simple.art/design")) }

    Box(modifier = Modifier.fillMaxSize().background(DarkGray)) {
        when (currentScreen) {
            ScreenType.HOME -> HomeScreen(
                initialUrl = currentUrl,
                onNavigate = { url ->
                    val finalUrl = if (url.startsWith("http")) url else "https://$url"
                    currentUrl = finalUrl
                    if (!openedTabs.contains(finalUrl)) openedTabs = openedTabs + finalUrl
                    currentScreen = ScreenType.BROWSER
                },
                onOpenTabs = { currentScreen = ScreenType.TABS }
            )
            ScreenType.BROWSER -> BrowserScreen(
                url = currentUrl,
                onBackToHome = { currentScreen = ScreenType.HOME },
                onOpenTabs = { currentScreen = ScreenType.TABS }
            )
            ScreenType.TABS -> TabsScreen(
                tabs = openedTabs,
                activeUrl = currentUrl,
                onTabSelect = { url ->
                    currentUrl = url
                    currentScreen = ScreenType.BROWSER
                },
                onClose = { currentScreen = ScreenType.HOME }
            )
        }
    }
}

// --- ГЛАВНЫЙ ЭКРАН ---
@Composable
fun HomeScreen(initialUrl: String, onNavigate: (String) -> Unit, onOpenTabs: () -> Unit) {
    var urlInput by remember { mutableStateOf(if (initialUrl.startsWith("http")) "" else initialUrl) }
    
    val placeholders = listOf("Какая погода?", "simple.art/design", "Новости ИИ", "Гаджет года")
    val animatedPlaceholder = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            for (text in placeholders) {
                for (i in 0..text.length) {
                    animatedPlaceholder.value = text.substring(0, i)
                    delay(80)
                }
                delay(2000)
                for (i in text.length downTo 0) {
                    animatedPlaceholder.value = text.substring(0, i)
                    delay(40)
                }
                delay(500)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Фоновое свечение
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .align(Alignment.Center)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PurpleAccent.copy(alpha = 0.3f), Color.Transparent),
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Поле поиска
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(GlassSurface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    placeholder = { Text(animatedPlaceholder.value, color = Color.Gray) }
                )

                IconButton(
                    onClick = { if (urlInput.isNotEmpty()) onNavigate(urlInput) else onNavigate("https://google.com") },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, RoundedCornerShape(24.dp))
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Go", tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Плитки быстрого доступа
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(3) { index ->
                    val titles = listOf("ИИ в браузере", "Новости дизайна", "Гаджет года")
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(GlassSurface)
                            .padding(16.dp)
                            .width(100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(PurpleAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(titles[index], color = Color.White, fontSize = 12.sp, maxLines = 1)
                    }
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
                .background(GlassSurface)
                .padding(vertical = 12.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray)
            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
            Icon(Icons.Default.Bookmarks, contentDescription = null, tint = Color.White)
            IconButton(onClick = onOpenTabs, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.FilterNone, contentDescription = "Tabs", tint = Color.White)
            }
        }
    }
}

// --- ЭКРАН БРАУЗЕРА ---
@Composable
fun BrowserScreen(url: String, onBackToHome: () -> Unit, onOpenTabs: () -> Unit) {
    var isMinimized by remember { mutableStateOf(false) }
    var isDesktopMode by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    loadUrl(url)
                    webViewRef = this
                }
            },
            update = { view ->
                val userAgent = if (isDesktopMode) 
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36" 
                else WebSettings.getDefaultUserAgent(context)
                view.settings.userAgentString = userAgent
                // Обновляем страницу только при смене User-Agent, чтобы не было вечного цикла
                if (view.url != url && view.url != null) { view.loadUrl(url) }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Плавающая уменьшенная панель
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .animateContentSize(animationSpec = tween(300))
        ) {
            if (isMinimized) {
                IconButton(
                    onClick = { isMinimized = false },
                    modifier = Modifier
                        .size(48.dp)
                        .background(GlassSurface, RoundedCornerShape(24.dp))
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Expand", tint = Color.White)
                }
            } else {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(DarkGray.copy(alpha = 0.9f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isMinimized = true },
                        modifier = Modifier.size(40.dp).background(Red, RoundedCornerShape(20.dp))
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onBackToHome) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White)
                    }
                    IconButton(onClick = { isDesktopMode = !isDesktopMode }) {
                        Icon(if (isDesktopMode) Icons.Default.Phone else Icons.Default.Computer, contentDescription = "PC Mode", tint = Color.White)
                    }
                    IconButton(onClick = onOpenTabs) {
                        Icon(Icons.Default.FilterNone, contentDescription = "Tabs", tint = Color.White)
                    }
                }
            }
        }
    }
}

// --- ЭКРАН ВКЛАДОК ---
@Composable
fun TabsScreen(tabs: List<String>, activeUrl: String, onTabSelect: (String) -> Unit, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Вкладки (${tabs.size})", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White) }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tabs) { tabUrl ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (tabUrl == activeUrl) PurpleAccent.copy(alpha = 0.3f) else GlassSurface)
                        .clickable { onTabSelect(tabUrl) }
                        .padding(16.dp)
                        .aspectRatio(0.8f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(tabUrl, color = Color.White, fontSize = 12.sp, maxLines = 2)
                }
            }
        }
    }
}

// --- ДИАЛОГ РАЗРЕШЕНИЙ (Универсальный) ---
@Composable
fun XPKPermissionDialog(title: String, description: String, onAccept: () -> Unit, onDecline: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDecline,
        confirmButton = { Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)) { Text("Разрешить") } },
        dismissButton = { TextButton(onClick = onDecline) { Text("Отклонить", color = Color.Gray) } },
        title = { Text(title, color = Color.White) },
        text = { Text(description, color = Color.LightGray) },
        containerColor = DarkGray,
        shape = RoundedCornerShape(24.dp)
    )
}