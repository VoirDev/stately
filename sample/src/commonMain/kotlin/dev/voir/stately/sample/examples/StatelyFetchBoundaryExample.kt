package dev.voir.stately.sample.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.voir.stately.ui.StatelyFetchBoundary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatelyFetchBoundaryExampleScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("StatelyFetchBoundary Example") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            StatelyFetchBoundary<String, String?>(
                fetcher = { payload ->
                    delay(1500)
                    "Data from StatelyFetchBoundary"
                },
                loading = { Text("⏳ Loading...") },
                error = { error -> Text("❌ Error: ${error.message}") },
                content = { data -> Text("✅ Success: $data") }
            )
        }
    }
}
