package com.namgs.permissiopn.ui.intro

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.namgs.permissiopn.data.IntroSlide
import kotlin.collections.lastIndex

@Composable
fun IntroPager(
    slides: List<IntroSlide>,
    pagerState: PagerState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { page -> slides[page].imageRes }
        ) { page ->
            val slide = slides[page]

            Box(Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(slide.imageRes),
                    contentDescription = slide.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.25f),
                                    Color.Black.copy(alpha = 0.85f)
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
                            bottom = 155.dp
                        )
                ) {
                    Text(
                        text = slide.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = slide.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 118.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(slides.size) { index ->
                Box(
                    Modifier
                        .size(
                            width = if (pagerState.currentPage == index) {
                                22.dp
                            } else {
                                8.dp
                            },
                            height = 8.dp
                        )
                        .background(
                            color = if (pagerState.currentPage == index) {
                                Color.White
                            } else {
                                Color.White.copy(alpha = 0.45f)
                            },
                            shape = CircleShape
                        )
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 30.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = pagerState.currentPage > 0,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.4f)
                )
            ) {
                Text("이전")
            }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    if (pagerState.currentPage == slides.lastIndex) {
                        "권한 확인"
                    } else {
                        "다음"
                    }
                )
            }

            TextButton(onClick = onSkip) {
                Text(
                    text = "건너뛰기",
                    color = Color.White
                )
            }
        }
    }
}