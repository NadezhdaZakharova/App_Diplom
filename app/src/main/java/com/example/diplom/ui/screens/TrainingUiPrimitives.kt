package com.example.diplom.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diplom.R

@Composable
fun SectionHeader(title: String, icon: ImageVector, modifier: Modifier = Modifier) {
    val sectionIconLabel = stringResource(R.string.a11y_section_header_icon)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = sectionIconLabel,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f, fill = false),
        )
    }
}

@Composable
internal fun GreenButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    minHeight: androidx.compose.ui.unit.Dp = 48.dp
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.outline,
            disabledContentColor = MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .defaultMinSize(minHeight = minHeight)
            .shadow(if (enabled) 4.dp else 0.dp, RoundedCornerShape(12.dp))
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 2,
            softWrap = true,
            style = TextStyle(
                lineBreak = LineBreak.Heading,
                hyphens = Hyphens.None
            )
        )
    }
}

@Composable
internal fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            maxLines = 2,
            softWrap = true,
            style = TextStyle(
                lineBreak = LineBreak.Heading,
                hyphens = Hyphens.None
            )
        )
    }
}

@Composable
internal fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else 3,
    isError: Boolean = false,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp) },
        shape = RoundedCornerShape(12.dp),
        isError = isError,
        supportingText = if (supportingText != null) {
            { Text(supportingText, fontSize = 12.sp, color = MaterialTheme.colorScheme.error) }
        } else {
            null
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = singleLine,
        maxLines = maxLines
    )
}

@Composable
fun AddExerciseToBankCard(
    heading: String,
    titleInput: String,
    onTitleChange: (String) -> Unit,
    descriptionInput: String,
    onDescriptionChange: (String) -> Unit,
    repsInput: String,
    onRepsChange: (String) -> Unit,
    durationSecondsError: String? = null,
    bringIntoViewRequester: BringIntoViewRequester? = null,
    submitLabel: String,
    submitEnabled: Boolean = true,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (bringIntoViewRequester != null) {
                    Modifier.bringIntoViewRequester(bringIntoViewRequester)
                } else {
                    Modifier
                }
            )
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                heading,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
            StyledTextField(
                titleInput,
                onTitleChange,
                stringResource(R.string.exercise_field_title)
            )
            StyledTextField(
                descriptionInput,
                onDescriptionChange,
                stringResource(R.string.exercise_field_description)
            )
            StyledTextField(
                value = repsInput,
                onValueChange = { onRepsChange(it.filter { ch -> ch.isDigit() }) },
                label = stringResource(R.string.exercise_field_duration_seconds),
                singleLine = true,
                isError = durationSecondsError != null,
                supportingText = durationSecondsError
            )
            GreenButton(
                submitLabel,
                onSubmit,
                enabled = submitEnabled,
                icon = Icons.Default.Add,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
