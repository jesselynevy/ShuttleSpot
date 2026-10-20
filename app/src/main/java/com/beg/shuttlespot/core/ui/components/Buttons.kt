package com.beg.shuttlespot.core.ui.components

import android.view.RoundedCorner
import android.widget.Button
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ButtonVariant {
    Primary,
    Secondary
}

@Composable
fun AppButton(
    text : String,
    variant: ButtonVariant = ButtonVariant.Primary,
    modifier: Modifier = Modifier,
    onClick : () -> Unit
){
    when(variant){
        ButtonVariant.Primary -> {
            Button(
                modifier = modifier.defaultMinSize(minHeight = 48.dp),
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                shape = MaterialTheme.shapes.small
            ){
                Text(text)
            }
        }
        ButtonVariant.Secondary ->{
            OutlinedButton(
                modifier = modifier.defaultMinSize(minHeight = 48.dp),
                onClick = onClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                shape = MaterialTheme.shapes.small
            ) {
                Text(text)
            }
        }
    }
}
