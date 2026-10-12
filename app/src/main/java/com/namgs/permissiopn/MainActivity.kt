package com.namgs.permissiopn

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class IntroSlide(
    val imageRes: Int,
    val title: String,
    val description: String,
    val durationMillis: Long = 4000L
)

private data class PermissionItem(
    val permission: String,
    val title: String,
    val description: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                var showMainPage by remember {
                    mutableStateOf(false)
                }

                if (showMainPage) {
                    MainPage()
                } else {
                    PermissionIntroScreen(
                        onNavigateToMain = {
                            showMainPage = true
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionIntroScreen(
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 이미지별 자동 전환 시간을 설정할 수 있습니다.
    val slides = remember {
        listOf(
            IntroSlide(
                imageRes = R.drawable.intro_1,
                title = "새로운 경험",
                description = "앱의 다양한 기능을 만나보세요.",
                durationMillis = 4000L
            ),
            IntroSlide(
                imageRes = R.drawable.intro_2,
                title = "더 편리하게",
                description = "필요한 기능을 간편하게 이용하세요.",
                durationMillis = 5000L
            ),
            IntroSlide(
                imageRes = R.drawable.intro_3,
                title = "시작할 준비",
                description = "서비스 이용에 필요한 권한을 확인합니다.",
                durationMillis = 6000L
            )
        )
    }

    val permissions = remember {
        buildList {
            add(
                PermissionItem(
                    Manifest.permission.CAMERA,
                    "카메라",
                    "사진 촬영 기능을 이용합니다."
                )
            )

            add(
                PermissionItem(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    "위치",
                    "현재 위치 기반 기능을 이용합니다."
                )
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(
                    PermissionItem(
                        Manifest.permission.POST_NOTIFICATIONS,
                        "알림",
                        "중요한 소식을 알림으로 받습니다."
                    )
                )
            }
        }
    }

    var showPermissions by remember {
        mutableStateOf(false)
    }

    var permissionRefresh by remember {
        mutableIntStateOf(0)
    }

    fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun allRequiredGranted(): Boolean {
        return permissions.all { isGranted(it.permission) }
    }

    // 이미 모든 필수 권한이 허용되었다면 인트로를 건너뜁니다.
    LaunchedEffect(Unit) {
        if (allRequiredGranted()) {
            onNavigateToMain()
        }
    }

    // 설정 앱에서 돌아왔을 때 권한을 다시 확인합니다.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        permissionRefresh++

        if (allRequiredGranted()) {
            onNavigateToMain()
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) {
            permissionRefresh++

            if (allRequiredGranted()) {
                onNavigateToMain()
            }
        }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { slides.size }
    )

    // 현재 페이지가 바뀌면 해당 이미지의 시간만큼 기다립니다.
    // 사용자가 스와이프하는 중에는 자동 전환하지 않습니다.
    LaunchedEffect(
        pagerState.currentPage,
        pagerState.isScrollInProgress,
        showPermissions
    ) {
        if (!showPermissions && !pagerState.isScrollInProgress) {
            val currentSlide = slides[pagerState.currentPage]

            delay(currentSlide.durationMillis)

            if (
                !pagerState.isScrollInProgress &&
                pagerState.currentPage == slides.lastIndex
            ) {
                showPermissions = true
            } else if (
                !pagerState.isScrollInProgress &&
                pagerState.currentPage < slides.lastIndex
            ) {
                pagerState.animateScrollToPage(
                    pagerState.currentPage + 1
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AnimatedContent(
            targetState = showPermissions,
            transitionSpec = {
                (
                        fadeIn(tween(500)) +
                                scaleIn(
                                    initialScale = 0.98f,
                                    animationSpec = tween(500)
                                )
                        ).togetherWith(fadeOut(tween(300)))
            },
            label = "intro_permission_transition"
        ) { permissionScreen ->

            if (!permissionScreen) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { page ->
                    val slide = slides[page]

                    // 페이지가 변경될 때 이미지가 천천히 확대됩니다.
                    val zoom by animateFloatAsState(
                        targetValue = if (
                            pagerState.currentPage == page
                        ) 1.10f else 1f,
                        animationSpec = tween(
                            durationMillis = slide.durationMillis
                                .toInt(),
                            easing = LinearEasing
                        ),
                        label = "intro_image_zoom"
                    )

                    Box(Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(slide.imageRes),
                            contentDescription = slide.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(zoom)
                        )

                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.25f),
                                            Color.Black.copy(alpha = 0.90f)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(
                                    start = 24.dp,
                                    end = 24.dp,
                                    bottom = 30.dp
                                )
                        ) {
                            Text(
                                text = slide.title,
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(10.dp))

                            Text(
                                text = slide.description,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 16.sp
                            )

                            Spacer(Modifier.height(24.dp))

                            // 페이지 인디케이터
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                repeat(slides.size) { index ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(4.dp)
                                            .background(
                                                if (
                                                    pagerState.currentPage == index
                                                ) Color.White
                                                else Color.White.copy(
                                                    alpha = 0.35f
                                                ),
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            // 이전 / 다음 버튼
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            if (pagerState.currentPage > 0) {
                                                pagerState.animateScrollToPage(
                                                    pagerState.currentPage - 1
                                                )
                                            }
                                        }
                                    },
                                    enabled = pagerState.currentPage > 0,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults
                                        .outlinedButtonColors(
                                            contentColor = Color.White,
                                            disabledContentColor =
                                                Color.White.copy(alpha = 0.4f)
                                        ),
                                    border = BorderStroke(
                                        1.dp,
                                        Color.White.copy(alpha = 0.8f)
                                    )
                                ) {
                                    Text("이전")
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            if (
                                                pagerState.currentPage <
                                                slides.lastIndex
                                            ) {
                                                pagerState.animateScrollToPage(
                                                    pagerState.currentPage + 1
                                                )
                                            } else {
                                                showPermissions = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Text(
                                        if (
                                            pagerState.currentPage ==
                                            slides.lastIndex
                                        ) "권한 확인" else "다음"
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    showPermissions = true
                                },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    "건너뛰기",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                PermissionContent(
                    permissions = permissions,
                    isGranted = ::isGranted,
                    refreshKey = permissionRefresh,
                    onRequest = {
                        val missing = permissions
                            .map { it.permission }
                            .filterNot { isGranted(it) }

                        if (missing.isEmpty()) {
                            onNavigateToMain()
                        } else {
                            permissionLauncher.launch(
                                missing.toTypedArray()
                            )
                        }
                    },
                    onOpenSettings = {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts(
                                "package",
                                context.packageName,
                                null
                            )
                        )
                        context.startActivity(intent)
                    },
                    onNavigateToMain = onNavigateToMain
                )
            }
        }
    }
}

@Composable
private fun PermissionContent(
    permissions: List<PermissionItem>,
    isGranted: (String) -> Boolean,
    refreshKey: Int,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val allGranted = remember(refreshKey, permissions) {
        permissions.all { isGranted(it.permission) }
    }

    // 권한이 설정 앱 등에서 변경된 경우에도 반영합니다.
    LaunchedEffect(allGranted) {
        if (allGranted) {
            onNavigateToMain()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101114))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "권한 확인",
            color = Color.White,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "서비스 이용에 필요한 권한을 확인해 주세요.",
            color = Color.LightGray,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(28.dp))

        permissions.forEach { item ->
            val granted = isGranted(item.permission)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        text = item.description,
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = if (granted) "허용됨" else "미허용",
                    color = if (granted) {
                        Color(0xFF81C784)
                    } else {
                        Color(0xFFFFB74D)
                    },
                    fontSize = 13.sp
                )
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.12f)
            )
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                if (allGranted) {
                    onNavigateToMain()
                } else {
                    onRequest()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = if (allGranted) "시작하기" else "권한 허용하기",
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onOpenSettings,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                "앱 설정에서 권한 변경",
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun MainPage() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Main Page",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}