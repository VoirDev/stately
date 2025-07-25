package dev.voir.stately.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import dev.voir.stately.collectAsState
import dev.voir.stately.rememberStatelyFetch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Composable
fun <Data, Payload : Any?> StatelyFetchBoundary(
    fetcher: suspend (payload: Payload?) -> Data,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    revalidateInterval: Long? = null,
    initiallyLoading: Boolean = true,
    lazy: Boolean = false,
    initialData: Data? = null,
    initialPayload: Payload? = null,
    autoRevalidateOnPayloadChange: Boolean = true,
    loading: @Composable (() -> Unit)? = null,
    error: @Composable ((Throwable) -> Unit)? = null,
    content: @Composable (data: Data) -> Unit
) {
    val state by rememberStatelyFetch(
        fetcher = fetcher,
        scope = scope,
        revalidateInterval = revalidateInterval,
        initialData = initialData,
        initiallyLoading = initiallyLoading,
        initialPayload = initialPayload,
        lazy = lazy,
        autoRevalidateOnPayloadChange = autoRevalidateOnPayloadChange,
    ).collectAsState()

    StatelyFetchContent(
        state = state,
        loading = loading,
        error = error,
        content = content
    )
}
