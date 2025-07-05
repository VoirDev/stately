package dev.voir.stately.sample.examples

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.voir.stately.rememberStatelyFetch
import dev.voir.stately.ui.StatelyFetchContent
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatelyFetchContentExampleScreen() {
    val scope = rememberCoroutineScope()
    val statelyFetch = rememberStatelyFetch(
        fetcher = {
            delay(2000)
            "This is data from StatelyFetchContent"
        },
        scope = scope
    )

    val state by statelyFetch.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("StatelyFetchContent Example") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            StatelyFetchContent(
                state = state,
                loading = { Text("Loading...") },
                error = { error -> Text("Error: ${error.message}") },
                content = { data -> Text("✅ Data: $data") }
            )
        }
    }
}
