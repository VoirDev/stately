package dev.voir.stately

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Composable
fun <Data> rememberStatelyFetch(
    fetcher: suspend () -> Data,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    revalidateInterval: Long? = null,
    initiallyLoading: Boolean = true,
    lazy: Boolean = false,
    initialData: Data? = null,
) = remember {
    StatelyFetch<Data, Unit>(
        fetcher = { fetcher() },
        scope = scope,
        revalidateInterval = revalidateInterval,
        initialData = initialData,
        initiallyLoading = initiallyLoading,
        lazy = lazy
    )
}

@Composable
fun <Data, Payload : Any?> StatelyFetch<Data, Payload>.collectAsState(): State<StatelyFetchResult<Data, Payload>> =
    this.state.collectAsState()
