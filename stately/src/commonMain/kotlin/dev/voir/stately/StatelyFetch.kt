package dev.voir.stately

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Represents the current result state of a fetch operation.
 *
 * @param Data The type of data being fetched.
 * @param Payload The type of payload used to trigger the fetch.
 * @property payload The payload used in the current request.
 * @property data The successfully fetched data (null if failed or not yet loaded).
 * @property loading Whether a fetch is currently in progress.
 * @property error The exception thrown during fetch (if any).
 */
data class StatelyFetchResult<Data, Payload>(
    val payload: Payload?,
    val data: Data?,
    val loading: Boolean,
    val error: Throwable? = null,
)

/**
 * A utility class for managing async data fetching with automatic state tracking,
 * manual and debounced revalidation, and optional interval-based refresh.
 *
 * @param fetcher A suspending function that performs the data fetch, accepting a payload.
 * @param scope The coroutine scope to launch fetch operations in (defaults to IO).
 * @param revalidateInterval If provided, will re-trigger fetch on this interval in ms.
 * @param lazy If true, the fetch will not start automatically on creation.
 * @param initialData Optional initial data to display before fetching.
 * @param initialPayload Optional payload to use for the initial fetch.
 * @param initiallyLoading Whether to mark the state as loading immediately.
 */
class StatelyFetch<Data, Payload>(
    private val fetcher: suspend (payload: Payload?) -> Data,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val revalidateInterval: Long? = null,
    lazy: Boolean = false,
    initialData: Data? = null,
    initialPayload: Payload? = null,
    initiallyLoading: Boolean = true,
) {
    // True if a fetch operation is in progress.
    private var revalidating: Boolean = false

    // The current active fetch coroutine (cancellable).
    private var fetchJob: Job? = null


    // The current scheduled debounce job (used to delay fetch execution).
    private var debounceJob: Job? = null

    // The most recent payload used or queued for fetch.
    private var payload = initialPayload

    // Backing state flow for consumers to observe fetch result updates.
    private val _state = MutableStateFlow(
        StatelyFetchResult(
            payload = initialPayload,
            loading = initiallyLoading,
            data = initialData
        )
    )

    /**
     * Public immutable state flow exposing the fetch result.
     * Observers can collect this to react to loading, error, and data changes.
     */
    val state: StateFlow<StatelyFetchResult<Data, Payload>>
        get() = _state

    init {
        // Trigger initial fetch immediately unless lazy is true.
        if (!lazy) {
            revalidate(initialPayload)
        }

        // If a revalidateInterval is set, start a loop that periodically refetches.
        if (revalidateInterval != null) {
            scope.launch {
                while (true) {
                    delay(revalidateInterval)
                    revalidate(payload)
                }
            }
        }
    }

    /**
     * Immediately revalidates the data with an optional payload override.
     * Cancels any pending debounce job and any in-flight fetch.
     *
     * @param payload The payload to use for this fetch (defaults to last used).
     */
    fun revalidate(payload: Payload? = this.payload) {
        debounceJob?.cancel()

        this.payload = payload
        doRevalidate(payload)
    }


    /**
     * Schedules a debounced revalidation. If called again before the debounce delay,
     * the previous job is cancelled and the timer restarts.
     *
     * Useful for user input scenarios (e.g., text field filters).
     *
     * @param payload The payload to use (defaults to current).
     * @param debounce How long to wait (in milliseconds) before triggering fetch.
     */
    fun revalidateDebounced(payload: Payload? = this.payload, debounce: Long = 1000) {
        this.payload = payload
        debounceJob?.cancel()
        debounceJob = scope.launch {
            if (debounce > 0) {
                delay(debounce)
            }
            doRevalidate(payload)
        }
    }

    /**
     * Launches a fetch coroutine and updates state accordingly.
     * Cancels any currently running fetch.
     *
     * @param payload The payload to pass to the fetcher function.
     */
    private fun doRevalidate(payload: Payload? = null) {
        fetchJob?.cancel() // Ensure only one fetch at a time.
        fetchJob = scope.launch {
            revalidating = true

            // Mark state as loading with new payload.
            _state.value = state.value.copy(loading = true, error = null, payload = payload)

            try {
                // Perform the fetch.
                val data = fetcher(payload)
                // Update state with result.
                _state.value = StatelyFetchResult(payload = payload, data = data, loading = false)
            } catch (e: CancellationException) {
                // cancellation is not an error.
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                // On error, retain previous data and set error field.
                _state.value = _state.value.copy(error = e, loading = false)
            } finally {
                revalidating = false
            }
        }
    }
}
