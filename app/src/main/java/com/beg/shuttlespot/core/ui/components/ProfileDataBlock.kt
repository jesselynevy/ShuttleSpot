package com.beg.shuttlespot.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.beg.shuttlespot.core.ui.theme.ShuttleSpotTheme

enum class ProfileDataBlockActionType {
    None,
    Edit,
    Chevron,
    Switch,
    Button
}

@Composable
fun ProfileDataBlock(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    informationTitle: String,
    informationData: String = "",
    actionType: ProfileDataBlockActionType = ProfileDataBlockActionType.None,
    switchChecked: Boolean = false,
    onSwitchChange: ((Boolean) -> Unit)? = null,
    onActionClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = false,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    dataColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val isRowClickable = enabled && (
        onClick != null ||
        actionType == ProfileDataBlockActionType.Chevron ||
        actionType == ProfileDataBlockActionType.Button
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isRowClickable) {
                        Modifier.clickable { onClick?.invoke() }
                    } else if (enabled && actionType == ProfileDataBlockActionType.Switch && onSwitchChange != null) {
                        Modifier.clickable { onSwitchChange(!switchChecked) }
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = informationTitle,
                    modifier = Modifier.size(24.dp),
                    tint = iconTint
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = informationTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )

                if (informationData.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = informationData,
                        style = MaterialTheme.typography.bodyMedium,
                        color = dataColor
                    )
                }
            }

            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(8.dp))
                trailingContent()
            } else {
                when (actionType) {
                    ProfileDataBlockActionType.Edit -> {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onActionClick?.invoke() ?: onClick?.invoke() },
                            enabled = enabled,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit $informationTitle",
                                modifier = Modifier.size(20.dp),
                                tint = iconTint
                            )
                        }
                    }

                    ProfileDataBlockActionType.Chevron -> {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = "Navigate $informationTitle",
                            modifier = Modifier.size(24.dp),
                            tint = iconTint
                        )
                    }

                    ProfileDataBlockActionType.Switch -> {
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = switchChecked,
                            onCheckedChange = { isChecked ->
                                onSwitchChange?.invoke(isChecked) ?: onActionClick?.invoke()
                            },
                            enabled = enabled
                        )
                    }

                    ProfileDataBlockActionType.Button,
                    ProfileDataBlockActionType.None -> {
                        // No extra trailing icon needed
                    }
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileDataBlockPreview() {
    ShuttleSpotTheme {
        Column {
            ProfileDataBlock(
                icon = Icons.Outlined.Person,
                informationTitle = "Email",
                informationData = "angelabenedictins@gmail.com",
                showDivider = true
            )
            ProfileDataBlock(
                icon = Icons.Outlined.Edit,
                informationTitle = "Nomor telepon",
                informationData = "628888888888888",
                actionType = ProfileDataBlockActionType.Edit,
                showDivider = true
            )
            ProfileDataBlock(
                icon = Icons.Outlined.Person,
                informationTitle = "Notifikasi ETA",
                informationData = "10 menit · Sen Sel Kam",
                actionType = ProfileDataBlockActionType.Chevron,
                showDivider = true
            )
            ProfileDataBlock(
                icon = Icons.Outlined.Person,
                informationTitle = "Dark Mode",
                actionType = ProfileDataBlockActionType.Switch,
                switchChecked = true
            )
        }
    }
}
