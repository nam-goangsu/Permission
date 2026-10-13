package com.namgs.permissiopn.data

import android.Manifest
import android.os.Build
import com.namgs.permissiopn.R

object IntroConfig {
    val slides = listOf(
        IntroSlide(
            imageRes = R.drawable.intro_1,
            title = "첫 번째 소개",
            description = "앱의 주요 기능을 소개합니다.",
            durationMillis = 4000L
        ),
        IntroSlide(
            imageRes = R.drawable.intro_2,
            title = "두 번째 소개",
            description = "앱을 편리하게 사용해 보세요.",
            durationMillis = 5000L
        ),
        IntroSlide(
            imageRes = R.drawable.intro_3,
            title = "시작하기",
            description = "필요한 권한을 허용해 주세요.",
            durationMillis = 6000L
        )
    )

    val permissions = buildList {
        add(
            PermissionItem(
                permission = Manifest.permission.CAMERA,
                title = "카메라",
                description = "카메라 기능 사용"
            )
        )

        add(
            PermissionItem(
                permission = Manifest.permission.ACCESS_COARSE_LOCATION,
                title = "위치",
                description = "현재 위치 확인"
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(
                PermissionItem(
                    permission = Manifest.permission.POST_NOTIFICATIONS,
                    title = "알림",
                    description = "알림 수신"
                )
            )
        }
    }
}


