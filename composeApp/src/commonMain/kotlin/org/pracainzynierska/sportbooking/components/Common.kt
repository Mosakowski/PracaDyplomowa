package org.pracainzynierska.sportbooking.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.pracainzynierska.sportbooking.theme.RacingGreen

@Composable
fun SportFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) RacingGreen else Color.LightGray.copy(alpha = 0.1f),
        contentColor = if (isSelected) Color.White else Color.Black,
        shape = RoundedCornerShape(50),
        border = if (isSelected) null else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
        modifier = Modifier.height(36.dp).clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal)
        }
    }
}