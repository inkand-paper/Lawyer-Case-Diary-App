package com.lawyercasediary.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lawyercasediary.models.*
import com.lawyercasediary.ui.theme.*

@Composable
fun CaseCard(case: Case, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { 20 }),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Subtle accent line
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(getStatusColor(case.status))
                        .align(Alignment.CenterStart)
                )

                Column(modifier = Modifier.padding(16.dp).padding(start = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            case.caseNumber,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        StatusBadge(case.status)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        case.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 22.sp
                        ),
                        maxLines = 2
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            case.client?.name ?: "No Client Attached",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Gavel,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                case.courtName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        
                        Text(
                            case.createdAt?.take(10) ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HearingCard(hearing: Hearing, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + expandVertically(),
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(MaterialTheme.colorScheme.outlineVariant, Color.Transparent))
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date Box
                Column(
                    modifier = Modifier
                        .width(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val dateParts = hearing.hearingDate.take(10).split("-")
                    if (dateParts.size >= 3) {
                        Text(
                            dateParts[2],
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            getMonthAbbreviation(dateParts[1]),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    } else {
                        Icon(Icons.Default.Event, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        hearing.case?.caseNumber ?: "Ref: Unknown",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        hearing.case?.title ?: "Scheduled Hearing",
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                    if (!hearing.notes.isNullOrBlank()) {
                        Text(
                            hearing.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1
                        )
                    }
                }

                IconButton(onClick = onClick) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Detail",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = getStatusColor(status)
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

private fun getStatusColor(status: String) = when (status.uppercase()) {
    "ACTIVE" -> StatusActive
    "DISPOSED", "CLOSED" -> StatusClosed
    "STAYED" -> Color(0xFFFFA500)
    "URGENT" -> StatusUrgent
    else -> Color.Gray
}

private fun getMonthAbbreviation(month: String): String = when (month) {
    "01" -> "JAN"
    "02" -> "FEB"
    "03" -> "MAR"
    "04" -> "APR"
    "05" -> "MAY"
    "06" -> "JUN"
    "07" -> "JUL"
    "08" -> "AUG"
    "09" -> "SEP"
    "10" -> "OCT"
    "11" -> "NOV"
    "12" -> "DEC"
    else -> "???"
}
