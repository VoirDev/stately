package dev.voir.stately

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StatelyActionResult<Response>(
    val response: Response? = null,
    val loading: Boolean = false,
    val error: Throwable? = null,
)

class StatelyAction<Payload : Any, Response>(
    private val action: suspend (payload: Payload) -> Response,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val onError: ((error: Throwable) -> Unit)? = null,
    private val onSuccess: ((response: Response) -> Unit)? = null
) {
    private var sending: Boolean = false
    private val _state = MutableStateFlow(StatelyActionResult<Response>())
    val state: StateFlow<StatelyActionResult<Response>>
        get() = _state

    fun execute(payload: Payload) {
        if (!sending) {
            doExecute(payload = payload)
        }
    }

    private fun doExecute(payload: Payload) {
        sending = true

        scope.launch {
            _state.value = state.value.copy(loading = true, error = null)
            try {
                val response = action(payload)
                _state.value = StatelyActionResult(response = response, loading = false)

                onSuccess?.let { it(response) }
            } catch (e: CancellationException) {
                // cancellation is not an error.
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = StatelyActionResult(error = e, loading = false)

                onError?.let { it(e) }
            } finally {
                sending = false
            }
        }
    }
}
