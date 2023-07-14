package com.christidischristidis.passkeys.screen.passcode

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.christidischristidis.passkeys.ui.theme.Purple40
import com.christidischristidis.passkeys.ui.theme.PurpleGrey40

@Composable
fun PasscodeBoxes(
    passcode: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = passcode,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword
        ),
        decorationBox = {
            Row(horizontalArrangement = Arrangement.Center) {
                repeat(6) { index ->
                    val char = when {
                        index >= passcode.length -> ""
                        else -> passcode[index].toString()
                    }
                    val isFocused = passcode.length == index
                    Text(
                        modifier = Modifier
                            .width(40.dp)
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) Purple40 else PurpleGrey40,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(2.dp),
                        text = char,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    )
}
