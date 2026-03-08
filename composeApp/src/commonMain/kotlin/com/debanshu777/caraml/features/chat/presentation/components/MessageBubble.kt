package com.debanshu777.caraml.features.chat.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.debanshu777.caraml.features.chat.data.ChatMessage
import com.debanshu777.caraml.features.chat.data.MessageRole

@Composable
fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.User
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }
    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(0.8f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                )
                if (!isUser && message.inferenceMetrics != null) {
                    val tpotFormatted = ((message.inferenceMetrics.tpotMs * 10).toInt() / 10.0).toString()
                    Text(
                        text = "$tpotFormatted ms/token",
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
