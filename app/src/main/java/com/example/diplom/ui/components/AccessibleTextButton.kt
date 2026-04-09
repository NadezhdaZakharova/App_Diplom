package com.example.diplom.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Минимальная зона касания ~48dp (рекомендации Material / доступности).
 */
@Composable
fun AccessibleTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                }
            ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),

        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )
    ) {
        content()
    }
}
