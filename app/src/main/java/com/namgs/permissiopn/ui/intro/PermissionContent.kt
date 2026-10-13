package com.namgs.permissiopn.ui.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.namgs.permissiopn.data.PermissionItem

@Composable
fun PermissionContent(
    permissions: List<PermissionItem>,
    refreshKey: Int,
    isGranted: (String) -> Boolean,
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    onContinue: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    // 권한 상태가 갱신될 때마다 전체 권한 상태를 다시 확인합니다.
    val allGranted = androidx.compose.runtime.remember(
        refreshKey,
        permissions
    ) {
        permissions
            .filter { it.required }
            .all { isGranted(it.permission) }
    }

    androidx.compose.runtime.LaunchedEffect(allGranted) {
        if (allGranted) {
            onContinue()
        }
    }

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
            text = "권한 설정",     color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = if (allGranted) {
                "필수 권한이 모두 허용되었습니다."
            } else {
                "앱에서 사용할 필수 권한을 허용해 주세요."
            },
            color = Color.LightGray,fontSize = 15.sp
        )

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            items(permissions, key = { it.permission }) { item ->
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
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(5.dp))

                        Text(
                            text = item.description,
                            color = Color.LightGray,
                            fontSize = 17.sp
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

        }

        Button(
            onClick = {
                if (allGranted) {
                    onNavigateToMain()
                } else {
                    onRequestPermissions()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp)
        ){
            Text(
                text = if (allGranted) "시작하기" else "권한 허용하기",
                fontSize = 16.sp)
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