import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.voir.stately.rememberStatelyAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatelyActionExampleScreen() {
    var simulateError by remember { mutableStateOf(false) }
    var simulateDelay by remember { mutableStateOf(true) }
    var payloadInput by remember { mutableStateOf("") }
    var actionVersion by remember { mutableIntStateOf(0) }

    var actionTriggered by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val action = rememberStatelyAction<String, String>(
        action = { payload ->
            if (simulateDelay) delay(2000)
            if (simulateError) throw Exception("Simulated Error")
            "Processed: $payload"
        },
        scope = scope,
        onSuccess = { println("✅ onSuccess: $it") },
        onError = { println("❌ onError: ${it.message}") }
    )

    val state by action.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("StatelyAction Example") })
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
                    Checkbox(checked = simulateDelay, onCheckedChange = { simulateDelay = it })
                    Text("Simulate delay")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = simulateError, onCheckedChange = { simulateError = it })
                    Text("Simulate error")
                }

                OutlinedTextField(
                    value = payloadInput,
                    onValueChange = { payloadInput = it },
                    label = { Text("Payload") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {
                        if (payloadInput.isNotBlank()) {
                            scope.launch {
                                action.execute(payloadInput)
                                actionTriggered = true
                            }
                        }
                    }) {
                        Text("Trigger Action")
                    }

                    Button(onClick = {
                        actionTriggered = false
                        payloadInput = ""
                        simulateError = false
                        simulateDelay = true
                        actionVersion++ // Recreate action
                    }) {
                        Text("Reset")
                    }
                }

                if (actionTriggered) {
                    HorizontalDivider()
                    Text("Action State", style = MaterialTheme.typography.titleMedium)
                    Text("Loading: ${state.loading}")
                    Text("Response: ${state.response ?: "null"}")
                    Text("Error: ${state.error?.message ?: "none"}")
                }
            }
        }
    )
}
