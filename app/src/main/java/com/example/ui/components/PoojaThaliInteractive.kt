package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DevotionalAudioHelper
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

data class FlowerParticle(
    val id: Int,
    var x: Float,
    var y: Float,
    val speed: Float,
    val size: Float,
    val rotation: Float,
    val color: Color
)

@Composable
fun InteractivePoojaThaliView(
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isAartiRotating by remember { mutableStateOf(true) }
    var flowerTrigger by remember { mutableStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "aartiAnim")
    val aartiRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thaliRotate"
    )

    val flameFlicker by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )

    // Flower Shower Particles
    val flowerParticles = remember { mutableStateListOf<FlowerParticle>() }

    LaunchedEffect(flowerTrigger) {
        if (flowerTrigger > 0) {
            val colors = listOf(Color(0xFFFF9800), Color(0xFFFFEB3B), Color(0xFFE91E63), Color(0xFFFF5722))
            for (i in 0..25) {
                flowerParticles.add(
                    FlowerParticle(
                        id = (flowerParticles.size + i),
                        x = (0..350).random().toFloat(),
                        y = -20f - (0..80).random(),
                        speed = (3..8).random().toFloat(),
                        size = (12..22).random().toFloat(),
                        rotation = (0..360).random().toFloat(),
                        color = colors.random()
                    )
                )
            }
        }
    }

    // Particle update ticker
    LaunchedEffect(flowerParticles.size) {
        while (flowerParticles.isNotEmpty()) {
            delay(16)
            val iterator = flowerParticles.iterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                p.y += p.speed
                if (p.y > 600f) {
                    iterator.remove()
                }
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("interactive_pooja_thali"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DevotionalNightBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "माँ जीण दिव्य आरती व थाल",
                    color = DevotionalGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "बंद करें",
                        tint = DevotionalGoldLight
                    )
                }
            }

            Text(
                text = "थाल स्पर्श कर आरती करें • घंटी व शंख ध्वनि से वातावरण पावन करें",
                color = DevotionalGoldLight.copy(alpha = 0.8f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Central Thali Canvas
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                DevotionalCrimsonDark,
                                Color(0xFF100709)
                            )
                        )
                    )
                    .clickable {
                        isAartiRotating = !isAartiRotating
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background sacred glowing rings
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    // Golden outer thali rim
                    drawCircle(
                        color = DevotionalGold,
                        radius = radius - 8.dp.toPx(),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5.dp.toPx())
                    )
                    drawCircle(
                        color = DevotionalAmber,
                        radius = radius - 20.dp.toPx(),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                    )

                    // Draw Decorative Floral Petals on rim
                    val numPetals = 8
                    for (i in 0 until numPetals) {
                        val angle = (i * 360f / numPetals) * (Math.PI / 180f)
                        val px = center.x + (radius - 14.dp.toPx()) * cos(angle).toFloat()
                        val py = center.y + (radius - 14.dp.toPx()) * sin(angle).toFloat()
                        drawCircle(
                            color = Color(0xFFFF5252),
                            radius = 4.dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }

                // Rotating Aarti Diya Holder
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .rotate(if (isAartiRotating) aartiRotation else 0f),
                    contentAlignment = Alignment.Center
                ) {
                    // 5 Sacred Aarti Diyas (Panch-Pradeep)
                    for (i in 0..4) {
                        val angle = i * 72f
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .rotate(angle),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                // Flame
                                Box(
                                    modifier = Modifier
                                        .size(16.dp, 24.dp)
                                        .scale(flameFlicker)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFFFFFDE7),
                                                    Color(0xFFFFD54F),
                                                    Color(0xFFFF6D00),
                                                    Color(0xFFD50000)
                                                )
                                            )
                                        )
                                )
                                // Golden Clay Diya
                                Box(
                                    modifier = Modifier
                                        .size(24.dp, 10.dp)
                                        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                                        .background(DevotionalAmber)
                                )
                            }
                        }
                    }

                    // Center Holy Akhand Jyoti
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp, 36.dp)
                                .scale(flameFlicker * 1.15f)
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.White,
                                            Color(0xFFFFEB3B),
                                            Color(0xFFFF9800),
                                            Color(0xFFE65100)
                                        )
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(34.dp, 14.dp)
                                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                                .background(DevotionalGold)
                        )
                    }
                }

                // Flower Shower Particle Canvas Layer
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (p in flowerParticles) {
                        drawCircle(
                            color = p.color,
                            radius = p.size / 2,
                            center = Offset(p.x, p.y)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons (Bell, Shankh, Flower Shower)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            DevotionalAudioHelper.playTempleBell()
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DevotionalGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("घंटी नाद", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            DevotionalAudioHelper.playShankhSound()
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DevotionalGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("शंख ध्वनि", fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        flowerTrigger++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevotionalSaffron, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Spa, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("पुष्प वर्षा", fontSize = 13.sp)
                }
            }
        }
    }
}
