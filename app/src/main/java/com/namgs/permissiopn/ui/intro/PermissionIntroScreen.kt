package com.namgs.permissiopn.ui.intro

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.namgs.permissiopn.data.IntroSlide
import com.namgs.permissiopn.data.PermissionItem
import com.namgs.permissiopn.viewmodel.IntroViewModel
import com.namgs.permissiopn.viewmodel.IntroViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.collections.isNotEmpty
import kotlin.collections.lastIndex

@Composable
fun PermissionIntroScreen(
    onNavigateToMain: () -> Unit,
    slides: List<IntroSlide>,
    permissions: List<PermissionItem>
) {
    require(slides.isNotEmpty()) {
        "slides must contain at least one item."
    }

    val context = LocalContext.current
    val application = context.applicationContext as Application

    val factory = remember(application, slides, permissions) {
        IntroViewModelFactory(
            application = application,
            slides = slides,
            permissions = permissions
        )
    }

    val viewModel: IntroViewModel = viewModel(
        key = "PermissionIntroViewModel-${slides.hashCode()}-${permissions.hashCode()}",
        factory = factory
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { slides.size }
    )
    val coroutineScope = rememberCoroutineScope()

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) {
            viewModel.onPermissionResult()
        }
    // 설정 앱에서 돌아왔을 때 권한을 다시 확인합니다.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshPermissions()
    }

    LaunchedEffect(uiState.navigateToMain) {
        if (uiState.navigateToMain) {
            onNavigateToMain()
        }
    }
    LaunchedEffect(
        pagerState,
        uiState.showPermissions
    ) {
        while (!uiState.showPermissions) {
            // 사용자가 스와이프 중이면 완료될 때까지 기다립니다.
            if (pagerState.isScrollInProgress) {
                snapshotFlow { pagerState.isScrollInProgress }
                    .first { isScrolling -> !isScrolling }
            }

            val currentPage = pagerState.currentPage
            val duration = slides[currentPage].durationMillis

            // 현재 이미지에 설정된 시간만큼 대기합니다.
            delay(duration.coerceAtLeast(0L))

            // 대기 중 사용자가 페이지를 바꿨다면 새 페이지부터 다시 계산합니다.
            if (
                pagerState.currentPage != currentPage ||
                pagerState.isScrollInProgress
            ) {
                continue
            }

            if (currentPage < slides.lastIndex) {
                pagerState.animateScrollToPage(currentPage + 1)
            } else {
                viewModel.showPermissionScreen()
                break
            }
        }
    }


    Surface(modifier = Modifier.fillMaxSize()) {
        if (uiState.showPermissions) {

            PermissionContent(
                permissions = permissions,
                refreshKey = uiState.permissionRefresh,
                isGranted = viewModel::isPermissionGranted,
                onRequestPermissions = {
                    val denied = viewModel.requestPermissionNames()

                    if (denied.isEmpty()) {
                        viewModel.refreshPermissions()
                    } else {
                        permissionLauncher.launch(denied.toTypedArray())
                    }
                },
                onOpenSettings = {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                },
                onContinue = {
                    viewModel.refreshPermissions()
                },
                onNavigateToMain = onNavigateToMain
            )
        } else {
            IntroPager(
                slides = slides,
                pagerState = pagerState,
                onPrevious = {
                    coroutineScope.launch {
                        if (pagerState.currentPage > 0) {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage - 1
                            )
                        }
                    }
                },
                onNext = {
                    coroutineScope.launch {
                        if (pagerState.currentPage < slides.lastIndex) {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        } else {
                            viewModel.showPermissionScreen()
                        }
                    }
                },
                onSkip = viewModel::showPermissionScreen
            )
        }
    }
}