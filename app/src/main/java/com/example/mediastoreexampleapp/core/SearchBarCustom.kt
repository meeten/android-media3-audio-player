package com.example.mediastoreexampleapp.core

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mediastoreexampleapp.R

@Composable
fun SearchBarCustom(
    modifier: Modifier = Modifier,
    query: String,
    onSearch: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = {
            onSearch(it)
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        leadingIcon = {
            Image(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null
            )
        },
        placeholder = { Text(text = stringResource(R.string.search_tracks)) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.primary,
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.secondary,
            selectionColors = TextSelectionColors(
                handleColor = MaterialTheme.colorScheme.secondary,
                backgroundColor = MaterialTheme.colorScheme.secondary
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchBarCustomPreview() {
    SearchBarCustom(
        modifier = Modifier.padding(8.dp),
        onSearch = {},
        query = ""
    )
}