package dev.voir.stately

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Composable
fun <Payload : Any, Response> rememberStatelyAction(
    action: suspend (payload: Payload) -> Response,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    onError: ((error: Throwable) -> Unit)? = null,
    onSuccess: ((response: Response) -> Unit)? = null
) = remember {
    StatelyAction(
        action = action,
        scope = scope,
        onError = onError,
        onSuccess = onSuccess
    )
}

@Composable
fun <Payload : Any, Response> StatelyAction<Payload, Response>.collectAsState(): State<StatelyActionResult<Response>> =
    this.state.collectAsState()
