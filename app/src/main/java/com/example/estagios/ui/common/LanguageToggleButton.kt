package com.example.estagios.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.estagios.R
import com.example.estagios.utils.LanguageManager

@Composable
fun LanguageToggleButton(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    IconButton(
        onClick = {
            LanguageManager.alternarIdioma(context)
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Outlined.Language,
            contentDescription = stringResource(R.string.change_language)
        )
    }
}