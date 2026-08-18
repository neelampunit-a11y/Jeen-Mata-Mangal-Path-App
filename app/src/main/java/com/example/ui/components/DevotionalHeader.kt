package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DevotionalAudioHelper
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DevotionalHeader(
    title: String = "माँ जीण शक्ति मंगल पाठ",
    subtitle: String = "सिद्धपीठ काजल शिखर धाम • सीकर (राज.)",
    onBellClick: (() -> Unit)? = null,
    onShankhClick: (() -> Unit)? = null,
    onFlowerClick: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    var bellRinging by remember { mutableStateOf(false) }
    var shankhBlowing by remember { mutableStateOf(false) }

    val bellRotation by animateFloatAsState(
        targetValue = if (bellRinging) 25f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
        finishedListener = { bellRinging = false },
        label = "bellRot"
    )

    val shankhScale by animateFloatAsState(
        targetValue = if (shankhBlowing) 1.25f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        finishedListener = { shankhBlowing = false },
        label = "shankhScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("devotional_header_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DevotionalCrimson,
                            DevotionalSaffron
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sacred Om & Trishul Symbol Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Text(
                        text = "卐",
                        color = DevotionalGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ॐ श्रीं जीण मातायै नमः",
                        color = DevotionalGoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "卐",
                        color = DevotionalGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                Text(
                    text = subtitle,
                    color = DevotionalGoldLight.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Quick Pooja Action Bar (Bell, Shankh, Flowers, Diya)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Temple Bell
                    IconButton(
                        onClick = {
                            bellRinging = true
                            coroutineScope.launch {
                                DevotionalAudioHelper.playTempleBell()
                            }
                            onBellClick?.invoke()
                        },
                        modifier = Modifier
                            .rotate(bellRotation)
                            .testTag("action_temple_bell")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "घंटी बजाएं",
                                tint = DevotionalGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "घंटी",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Sacred Shankh
                    IconButton(
                        onClick = {
                            shankhBlowing = true
                            coroutineScope.launch {
                                DevotionalAudioHelper.playShankhSound()
                            }
                            onShankhClick?.invoke()
                        },
                        modifier = Modifier
                            .scale(shankhScale)
                            .testTag("action_sacred_shankh")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "शंख ध्वनि",
                                tint = DevotionalGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "शंख",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Pushpanjali Flower Shower
                    IconButton(
                        onClick = {
                            onFlowerClick?.invoke()
                        },
                        modifier = Modifier.testTag("action_flower_shower")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = "पुष्पांजलि",
                                tint = DevotionalGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "पुष्पांजलि",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
