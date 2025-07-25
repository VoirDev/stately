package dev.voir.stately.sample.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.voir.stately.rememberStatelyFetch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatelyFetchExampleScreen() {
    var revalidateIntervalInput by remember { mutableStateOf<Long?>(null) }
    var simulateError by remember { mutableStateOf(false) }
    var lazy by remember { mutableStateOf(false) }
    var initiallyLoading by remember { mutableStateOf(false) }
    var initialData by remember { mutableStateOf<String?>(null) }

    var currentConfig by remember { mutableStateOf<FetchConfig?>(null) }
    var simulationVersion by remember { mutableIntStateOf(0) }

    var payloadInput by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("StatelyFetch Example") })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Configuration", style = MaterialTheme.typography.titleMedium)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = lazy, onCheckedChange = { lazy = it })
                    Text("Lazy")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = initiallyLoading,
                        onCheckedChange = { initiallyLoading = it })
                    Text("Initially loading")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = simulateError, onCheckedChange = { simulateError = it })
                    Text("Simulate error")
                }

                OutlinedTextField(
                    value = revalidateIntervalInput?.toString() ?: "",
                    onValueChange = { revalidateIntervalInput = it.toLongOrNull() },
                    label = { Text("Revalidate Interval (ms)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = initialData ?: "",
                    onValueChange = {
                        initialData = it.ifBlank { null }
                    },
                    label = { Text("Initial data") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = payloadInput.orEmpty(),
                    onValueChange = {
                        payloadInput = it.ifBlank { null }
                    },
                    label = { Text("Initial payload value") },
                    keyboardOptions = KeyboardOptions.Default,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(onClick = {
                    currentConfig = FetchConfig(
                        simulateError = simulateError,
                        lazy = lazy,
                        initiallyLoading = initiallyLoading,
                        revalidateInterval = revalidateIntervalInput,
                        initialData = initialData,
                        payload = payloadInput
                    )
                    simulationVersion++
                }) {
                    Text("Start / Reset Simulation")
                }

                HorizontalDivider()

                currentConfig?.let { config ->
                    key(simulationVersion) {
                        FetchSimulation(config = config)
                    }
                }
            }
        }
    )
}

@Composable
private fun FetchSimulation(config: FetchConfig) {
    val scope = rememberCoroutineScope()

    var currentPayload by remember(config) { mutableStateOf(config.payload) }

    val statelyFetch = rememberStatelyFetch<String, String>(
        fetcher = { payload ->
            delay(3000) // simulate network delay
            if (config.simulateError) throw Exception("Simulated Error")
            "Fetched Data (payload=${payload ?: "none"})"
        },
        scope = scope,
        revalidateInterval = config.revalidateInterval,
        initiallyLoading = config.initiallyLoading,
        initialData = config.initialData,
        lazy = config.lazy,
        initialPayload = config.payload
    )

    val state by statelyFetch.state.collectAsState()

    Text("Fetch State", style = MaterialTheme.typography.titleMedium)
    Text("Loading: ${state.loading}")
    Text("Data: ${state.data ?: "null"}")
    Text("Error: ${state.error?.message ?: "none"}")

    HorizontalDivider()

    Button(onClick = {
        statelyFetch.revalidate()
    }) {
        Text("Fetch/revalidate")
    }

    Button(onClick = {
        statelyFetch.revalidateDebounced(debounce = 3000)
    }) {
        Text("Fetch/revalidate with debounce")
    }

    HorizontalDivider()


    OutlinedTextField(
        value = currentPayload.orEmpty(),
        onValueChange = {
            currentPayload = it.ifBlank { null }
            statelyFetch.revalidate(it.ifBlank { null }) // triggers fetch + tracks new payload
        },
        label = { Text("Change Payload") },
        modifier = Modifier.fillMaxWidth()
    )
}

private data class FetchConfig(
    val simulateError: Boolean,
    val lazy: Boolean,
    val initiallyLoading: Boolean,
    val revalidateInterval: Long?,
    val initialData: String?,
    val payload: String?
)
