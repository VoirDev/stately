package dev.voir.stately.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import dev.voir.stately.collectAsState
import dev.voir.stately.rememberStatelyFetch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Composable
fun <Data> StatelyFetchBoundary(
    fetcher: suspend () -> Data,
    loading: @Composable (() -> Unit)? = null,
    error: @Composable ((Throwable) -> Unit)? = null,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    revalidateInterval: Long? = null,
    content: @Composable (data: Data) -> Unit
) {
    val state by rememberStatelyFetch(
        fetcher = fetcher,
        scope = scope,
        revalidateInterval = revalidateInterval,
    ).collectAsState()

    StatelyFetchContent(
        state = state,
        loading = loading,
        error = error,
        content = content
    )
}
