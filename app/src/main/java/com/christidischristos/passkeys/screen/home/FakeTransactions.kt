package com.christidischristos.passkeys.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.christidischristos.passkeys.R
import com.christidischristos.passkeys.ui.theme.Blue

// Just some data to show on home-screen
private data class Transaction(
    val title: String,
    val date: String,
    val money: String
)

private val transactions = listOf(
    Transaction(
        title = "☕️ Flocafe",
        date = "19th July at 08:30",
        money = "$6.50"
    ),
    Transaction(
        title = "🍻 Eric's Beerhouse",
        date = "19th July at 18:00",
        money = "$10.00"
    ),
    Transaction(
        title = "🍕Pizza Fan",
        date = "19th July at 21:00",
        money = "$13.90"
    )
)

@Composable
fun FakeTransactions() {
    Text(
        "Your last transactions",
        style = MaterialTheme.typography.headlineMedium,
    )
    Divider(thickness = 1.dp)
    transactions.forEach { tr ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(
                painterResource(R.drawable.shopping_cart),
                tint = Blue,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tr.title, fontWeight = FontWeight.Bold)
                Text(text = tr.date, color = Color.Gray)
            }
            Text(text = tr.money)
        }
        Divider(thickness = 1.dp)
    }
}

