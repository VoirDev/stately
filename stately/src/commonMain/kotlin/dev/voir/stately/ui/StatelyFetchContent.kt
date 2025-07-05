package dev.voir.stately.ui

import androidx.compose.runtime.Composable
import dev.voir.stately.StatelyFetchResult

@Composable
fun <Data, Payload> StatelyFetchContent(
    state: StatelyFetchResult<Data, Payload>,
    loading: @Composable (() -> Unit)? = null,
    error: @Composable ((Throwable) -> Unit)? = null,
    content: @Composable (data: Data) -> Unit
) {
    if (state.loading) {
        if (loading != null) {
            loading()
        }
    } else if (state.error != null) {
        if (error != null) {
            error(state.error)
        }
    } else if (state.data != null) {
        content(state.data)
    }
}
