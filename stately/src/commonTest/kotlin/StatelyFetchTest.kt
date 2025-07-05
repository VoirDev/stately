import dev.voir.stately.StatelyFetch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StatelyFetchTest {

    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)

    @BeforeTest
    fun setup() {
        // Setup phase
    }

    @AfterTest
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun `fetches immediately when not lazy`() = scope.runTest {
        var called = false
        val fetch = StatelyFetch<String, String?>(
            fetcher = {
                called = true
                "Hello"
            },
            scope = this,
            lazy = false
        )

        advanceUntilIdle()

        assertTrue(called)
        assertEquals("Hello", fetch.state.value.data)
    }

    @Test
    fun `does not fetch when lazy until revalidate`() = scope.runTest {
        var called = false
        val fetch = StatelyFetch<String, String?>(
            fetcher = {
                called = true
                "World"
            },
            scope = this,
            lazy = true
        )

        assertFalse(called)

        fetch.revalidate()

        advanceUntilIdle()
        assertTrue(called)
        assertEquals("World", fetch.state.value.data)
    }

    @Test
    fun `sets error on exception`() = scope.runTest {
        val fetch = StatelyFetch<String, String?>(
            fetcher = { throw Exception("Boom") },
            scope = this,
            lazy = false
        )

        advanceUntilIdle()

        assertNotNull(fetch.state.value.error)
        assertEquals("Boom", fetch.state.value.error?.message)
    }

    @Test
    fun `keeps previous data on error`() = scope.runTest {
        var shouldFail = false
        val fetch = StatelyFetch<String, String?>(
            fetcher = {
                if (shouldFail) throw Exception("Oops")
                "Data"
            },
            scope = this,
            lazy = true
        )

        fetch.revalidate()
        advanceUntilIdle()
        assertEquals("Data", fetch.state.value.data)

        shouldFail = true
        fetch.revalidate()
        advanceUntilIdle()

        assertEquals("Data", fetch.state.value.data)
        assertEquals("Oops", fetch.state.value.error?.message)
    }

    @Test
    fun `uses payload in fetch`() = scope.runTest {
        val fetch = StatelyFetch<String, String?>(
            fetcher = { payload -> "Got $payload" },
            scope = this,
            lazy = true
        )

        fetch.revalidate("ABC")
        advanceUntilIdle()

        assertEquals("Got ABC", fetch.state.value.data)
        assertEquals("ABC", fetch.state.value.payload)
    }
}
