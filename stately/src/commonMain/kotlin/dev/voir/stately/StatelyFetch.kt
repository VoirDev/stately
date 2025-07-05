package dev.voir.stately

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StatelyFetchResult<Data, Payload>(
    val payload: Payload?,
    val data: Data?,
    val loading: Boolean,
    val error: Throwable? = null,
)

class StatelyFetch<Data, Payload>(
    private val fetcher: suspend (payload: Payload?) -> Data,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val revalidateInterval: Long? = null,
    lazy: Boolean = false,
    initialData: Data? = null,
    initialPayload: Payload? = null,
    initiallyLoading: Boolean = true,
) {
    private var revalidating: Boolean = false
    private val _state = MutableStateFlow(
        StatelyFetchResult(
            payload = initialPayload,
            loading = initiallyLoading,
            data = initialData
        )
    )
    val state: StateFlow<StatelyFetchResult<Data, Payload>>
        get() = _state

    init {
        if (!lazy) {
            revalidate()
        }

        if (revalidateInterval != null) {
            scope.launch {
                while (true) {
                    delay(revalidateInterval)
                    revalidate()
                }
            }
        }
    }

    fun revalidate(payload: Payload? = null) {
        if (!revalidating) {
            doRevalidate(payload)
        }
    }

    private fun doRevalidate(payload: Payload? = null) {
        revalidating = true

        scope.launch {
            _state.value = state.value.copy(loading = true, error = null, payload = payload)
            try {
                val data = fetcher(payload)
                _state.value = StatelyFetchResult(payload = payload, data = data, loading = false)
            } catch (e: Exception) {
                e.printStackTrace()
                // Keep previous data and payload on error
                _state.value = _state.value.copy(error = e, loading = false)
            } finally {
                revalidating = false
            }
        }
    }
}
