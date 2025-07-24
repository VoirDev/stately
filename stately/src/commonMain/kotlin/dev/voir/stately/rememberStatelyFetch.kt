package dev.voir.stately

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Composable
fun <Data, Payload : Any?> rememberStatelyFetch(
    fetcher: suspend (payload: Payload?) -> Data,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    revalidateInterval: Long? = null,
    initiallyLoading: Boolean = true,
    lazy: Boolean = false,
    initialData: Data? = null,
    initialPayload: Payload? = null,
    autoRevalidateOnPayloadChange: Boolean = true,
) = remember {
    StatelyFetch(
        fetcher = fetcher,
        scope = scope,
        revalidateInterval = revalidateInterval,
        initialData = initialData,
        initiallyLoading = initiallyLoading,
        initialPayload = initialPayload,
        lazy = lazy,
    )
}

@Composable
fun <Data> rememberStatelyFetch(
    fetcher: suspend () -> Data,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    revalidateInterval: Long? = null,
    initiallyLoading: Boolean = true,
    lazy: Boolean = false,
    initialData: Data? = null,
) = remember {
    StatelyFetch(
        fetcher = {
            fetcher()
        },
        scope = scope,
        revalidateInterval = revalidateInterval,
        initialData = initialData,
        initiallyLoading = initiallyLoading,
        initialPayload = null,
        lazy = lazy,
    )
}

@Composable
fun <Data, Payload : Any?> StatelyFetch<Data, Payload>.collectAsState(): State<StatelyFetchResult<Data, Payload>> =
    this.state.collectAsState()
