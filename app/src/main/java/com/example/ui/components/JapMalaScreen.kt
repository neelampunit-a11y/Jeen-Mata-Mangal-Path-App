package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DevotionalAudioHelper
import com.example.data.UserPreferences
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JapMalaScreen(
    userPreferences: UserPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }

    val mantras = listOf(
        "जीण जीण भज बारम्बारा, हर संकट का हो निस्तारा ।",
        "ॐ श्रीं जीण मातायै नमः",
        "जय जय भंवरा वाली माँ",
        "मंगल भवन अमंगल हारी, जीण नाम होता हितकारी ।"
    )
    var selectedMantraIndex by remember { mutableStateOf(0) }

    var currentCount by remember { mutableStateOf(0) }
    var malasCompleted by remember { mutableStateOf(0) }
    var todayCount by remember { mutableStateOf(userPreferences.todayJapCount) }
    var totalCount by remember { mutableStateOf(userPreferences.totalJapCount) }
    var soundEnabled by remember { mutableStateOf(true) }
    var hapticEnabled by remember { mutableStateOf(true) }

    // Touch tap scale animation
    var tapScale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = tapScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "beadTapScale"
    )

    fun performHaptic() {
        if (hapticEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(35)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun incrementCount() {
        tapScale = 0.93f
        performHaptic()
        currentCount++
        todayCount++
        totalCount++
        userPreferences.todayJapCount = todayCount
        userPreferences.totalJapCount = totalCount

        if (currentCount >= 108) {
            malasCompleted++
            currentCount = 0
            if (soundEnabled) {
                coroutineScope.launch {
                    DevotionalAudioHelper.playTempleBell()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "माँ जीण नाम व मंत्र जपमाला",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "पीछे जाएं")
                    }
                },
                actions = {
                    IconButton(onClick = { soundEnabled = !soundEnabled }) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "ध्वनि टॉगल",
                            tint = DevotionalGold
                        )
                    }
                    IconButton(onClick = { hapticEnabled = !hapticEnabled }) {
                        Icon(
                            imageVector = if (hapticEnabled) Icons.Default.Vibration else Icons.Default.Smartphone,
                            contentDescription = "वाइब्रेशन टॉगल",
                            tint = DevotionalGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DevotionalCrimson,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DevotionalParchment
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mantra Selector Carousel / Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selected_mantra_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DevotionalParchmentCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "जप हेतु पावन मंत्र / नाम",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DevotionalSaffron
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mantras[selectedMantraIndex],
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = DevotionalCrimsonDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        mantras.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (index == selectedMantraIndex) 10.dp else 7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == selectedMantraIndex) DevotionalCrimson else DevotionalBorder
                                    )
                                    .clickable { selectedMantraIndex = index }
                            )
                        }
                    }
                }
            }

            // Central Interactive Chanting Bead / Mala Dial
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .scale(animatedScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                DevotionalGoldLight,
                                Color(0xFFFFE0B2),
                                DevotionalParchmentCard
                            )
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        incrementCount()
                        tapScale = 1f
                    }
                    .testTag("tap_jap_bead_button"),
                contentAlignment = Alignment.Center
            ) {
                // 108 Beads Representation Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = (size.minDimension / 2) - 18.dp.toPx()
                    val totalBeads = 108
                    val beadRadius = 3.dp.toPx()

                    // Background continuous track arc
                    drawCircle(
                        color = DevotionalBorder.copy(alpha = 0.5f),
                        radius = radius,
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Active progress glow arc
                    val sweep = (currentCount.toFloat() / totalBeads) * 360f
                    drawArc(
                        brush = Brush.sweepGradient(listOf(DevotionalSaffron, DevotionalCrimson)),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx())
                    )

                    // Beads
                    for (i in 0 until totalBeads) {
                        val angle = (i * 360f / totalBeads - 90) * (Math.PI / 180f)
                        val bx = center.x + radius * cos(angle).toFloat()
                        val by = center.y + radius * sin(angle).toFloat()

                        val isPassed = i < currentCount
                        drawCircle(
                            color = if (isPassed) DevotionalCrimson else DevotionalBorder,
                            radius = if (isPassed) beadRadius * 1.35f else beadRadius,
                            center = Offset(bx, by)
                        )
                    }

                    // Meru Bead (Top Guru Mani)
                    val meruAngle = -90 * (Math.PI / 180f)
                    val mx = center.x + radius * cos(meruAngle).toFloat()
                    val my = center.y + radius * sin(meruAngle).toFloat()
                    drawCircle(
                        color = DevotionalGold,
                        radius = beadRadius * 2.5f,
                        center = Offset(mx, my)
                    )
                }

                // Inner Counter Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$currentCount",
                        fontSize = 46.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DevotionalCrimson
                    )
                    Text(
                        text = "/ 108",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DevotionalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DevotionalCrimson.copy(alpha = 0.1f),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "स्पर्श कर जपें",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = DevotionalCrimson,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Action Chant Button
            Button(
                onClick = { incrementCount() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("primary_jap_counter_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DevotionalCrimson,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = DevotionalGold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "जप करें (+1)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Stats & Controls Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DevotionalParchmentCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "माला पूर्ण", fontSize = 11.sp, color = DevotionalTextSecondary)
                            Text(text = "$malasCompleted", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DevotionalCrimson)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "आज का जप", fontSize = 11.sp, color = DevotionalTextSecondary)
                            Text(text = "$todayCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DevotionalSaffron)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "कुल जप", fontSize = 11.sp, color = DevotionalTextSecondary)
                            Text(text = "$totalCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DevotionalTextDark)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                currentCount = 0
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("माला रीसेट", fontSize = 12.sp, color = DevotionalTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
