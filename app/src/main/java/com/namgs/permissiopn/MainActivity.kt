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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import kotlinx.coroutines.delay

private data class IntroSlide(
    val imageRes: Int,
    val title: String,
    val description: String
)

private data class PermissionItem(
    val permission: String,
    val title: String,
    val description: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK

        setContent {
            MaterialTheme {
                PermissionIntroScreen()
            }
        }
    }
}

@Composable
fun PermissionIntroScreen() {
    val context = LocalContext.current

    val slides = remember {
        listOf(
            IntroSlide(
                R.drawable.intro_1,
                "새로운 경험",
                "앱의 다양한 기능을 만나보세요."
            ),
            IntroSlide(
                R.drawable.intro_2,
                "더 편리하게",
                "필요한 기능을 간편하게 이용하세요."
            ),
            IntroSlide(
                R.drawable.intro_3,
                "시작할 준비",
                "서비스 이용에 필요한 권한을 확인합니다."
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
                    Manifest.permission.ACCESS_FINE_LOCATION,
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

    // 0..slides.lastIndex: 이미지 재생
    // slides.size: 권한 확인 화면
    var page by remember { mutableIntStateOf(0) }

    var permissionResults by remember {
        mutableStateOf<Map<String, Boolean>>(emptyMap())
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            permissionResults = results
        }

    // 현재 권한 상태를 OS 기준으로 확인
    fun isGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    // 이미지 자동 재생
    LaunchedEffect(page) {
        if (page < slides.size) {
            delay(2800L)

            if (page < slides.lastIndex) {
                page++
            } else {
                page = slides.size
            }
        }
    }

    // 권한 요청 목록: 아직 허용되지 않은 권한만 요청
    val missingPermissions = permissions
        .map { it.permission }
        .filterNot { isGranted(it) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AnimatedContent(
            targetState = page,
            transitionSpec = {
                (fadeIn(tween(650)) + scaleIn(
                    initialScale = 1.06f,
                    animationSpec = tween(900)
                )).togetherWith(
                    fadeOut(tween(400))
                )
            },
            label = "intro_transition"
        ) { targetPage ->

            if (targetPage < slides.size) {
                val slide = slides[targetPage]

                var animationStarted by remember(targetPage) {
                    mutableStateOf(false)
                }

                LaunchedEffect(targetPage) {
                    animationStarted = true
                }

                val imageScale by animateFloatAsState(
                    targetValue = if (animationStarted) 1.12f else 1f,
                    animationSpec = tween(
                        durationMillis = 2800,
                        easing = androidx.compose.animation.core.LinearEasing
                    ),
                    label = "image_zoom"
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(slide.imageRes),
                        contentDescription = slide.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(imageScale)
                    )

                    // 아래쪽 텍스트가 잘 보이도록 그라데이션
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.2f),
                                        Color.Black.copy(alpha = 0.85f)
                                    ),
                                    startY = 300f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(
                                start = 28.dp,
                                end = 28.dp,
                                bottom = 76.dp
                            )
                    ) {
                        Text(
                            text = slide.title,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = slide.description,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp
                        )

                        Spacer(Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(slides.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .width(
                                            if (index == targetPage) 28.dp
                                            else 8.dp
                                        )
                                        .height(5.dp)
                                        .background(
                                            if (index == targetPage) {
                                                Color.White
                                            } else {
                                                Color.White.copy(alpha = 0.4f)
                                            },
                                            RoundedCornerShape(8.dp)
                                        )
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        TextButton(
                            onClick = { page = slides.size }
                        ) {
                            Text(
                                "건너뛰기",
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                PermissionContent(
                    permissions = permissions,
                    isGranted = ::isGranted,
                    onRequest = {
                        if (missingPermissions.isNotEmpty()) {
                            permissionLauncher.launch(
                                missingPermissions.toTypedArray()
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
                    }
                )
            }
        }
    }
}

@Composable
private fun PermissionContent(
    permissions: List<PermissionItem>,
    isGranted: (String) -> Boolean,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit
) {
    // 설정 화면에서 돌아왔을 때도 최신 상태를 반영
    var refreshKey by remember { mutableIntStateOf(0) }

    val lifecycleOwner =
        androidx.lifecycle.compose.LocalLifecycleOwner.current

    androidx.lifecycle.compose.LifecycleEventEffect(
        androidx.lifecycle.Lifecycle.Event.ON_RESUME
    ) {
        refreshKey++
    }

    val allGranted = remember(refreshKey, permissions) {
        permissions.all { isGranted(it.permission) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101114))
            .padding(horizontal = 24.dp, vertical = 36.dp),
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
            text = "앱의 기능을 원활하게 이용할 수 있도록\n필요한 권한을 확인해 주세요.",
            color = Color.LightGray,
            fontSize = 15.sp,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(32.dp))

        permissions.forEach { item ->
            val granted = isGranted(item.permission)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.title,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        item.description,
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.width(12.dp))

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

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                if (allGranted) {
                    // TODO: 메인 화면으로 이동
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
                if (allGranted) "시작하기" else "권한 허용하기",
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

        if (allGranted) {
            Spacer(Modifier.height(8.dp))
            Text(
                "필요한 권한이 모두 허용되었습니다.",
                color = Color(0xFF81C784),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}