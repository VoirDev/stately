import dev.voir.stately.StatelyAction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StatelyActionTest {

    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)

    @AfterTest
    fun tearDown() {
        scope.cancel()
    }


    @Test
    fun `executes action and updates state`() = scope.runTest {
        val action = StatelyAction<String, String>(
            action = { payload -> "Echo: $payload" },
            scope = this
        )

        action.execute("Test")
        advanceUntilIdle()

        val result = action.state.value
        assertFalse(result.loading)
        assertEquals("Echo: Test", result.response)
        assertNull(result.error)
    }

    @Test
    fun `calls onSuccess callback`() = scope.runTest {
        var successCalled = false
        var resultFromCallback: String? = null

        val action = StatelyAction<String, String>(
            action = { "OK" },
            scope = this,
            onSuccess = {
                successCalled = true
                resultFromCallback = it
            }
        )

        action.execute("payload")
        advanceUntilIdle()

        assertTrue(successCalled)
        assertEquals("OK", resultFromCallback)
    }

    @Test
    fun `calls onError callback`() = scope.runTest {
        var errorCalled = false
        var errorMessage: String? = null

        val action = StatelyAction<String, String>(
            action = { throw RuntimeException("Fail!") },
            scope = this,
            onError = {
                errorCalled = true
                errorMessage = it.message
            }
        )

        action.execute("payload")
        advanceUntilIdle()

        assertTrue(errorCalled)
        assertEquals("Fail!", errorMessage)
    }

    @Test
    fun `does not execute if already sending`() = scope.runTest {
        var count = 0

        val action = StatelyAction<String, String>(
            action = {
                count++
                delay(1000)
                "done"
            },
            scope = this
        )

        action.execute("1")
        action.execute("2") // should be ignored
        advanceUntilIdle()

        assertEquals(1, count)
        assertEquals("done", action.state.value.response)
    }

    @Test
    fun `resets loading after completion`() = scope.runTest {
        val action = StatelyAction<String, String>(
            action = {
                delay(500)
                "completed"
            },
            scope = this
        )

        action.execute("payload")

        advanceUntilIdle()

        // Check loading = false after
        assertFalse(action.state.value.loading)
        assertEquals("completed", action.state.value.response)
    }
}
