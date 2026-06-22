package dev.angussoftware.app.blog

import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class BlogViewModelTest {
    private val testPosts = listOf(
        BlogPost(id = "1", title = "First", url = "https://example.com/1", pubDate = null, summary = null, imageUrl = null, content = null),
        BlogPost(id = "2", title = "Second", url = "https://example.com/2", pubDate = null, summary = null, imageUrl = null, content = null),
    )

    private fun makeViewModel(
        posts: List<BlogPost> = emptyList(),
        throwOnFetch: Throwable? = null,
        testScope: kotlinx.coroutines.test.TestScope,
    ): BlogViewModel {
        val repo = if (throwOnFetch != null) {
            // Subclass BlogRepository to throw from fetchPosts — the real repo
            // catches exceptions internally and returns emptyList(), so we need
            // to override at the repo level to test BlogViewModel's catch block.
            object : BlogRepository(
                feedUrl = "unused",
                networkClient = object : NetworkClient {
                    override suspend fun fetchUrlText(url: String): String = ""
                },
            ) {
                override suspend fun fetchPosts(limit: Int): List<BlogPost> = throw throwOnFetch
            }
        } else {
            BlogRepository(
                feedUrl = "https://test.example.com/feed.xml",
                networkClient = object : NetworkClient {
                    override suspend fun fetchUrlText(url: String): String =
                        posts.joinToString("") { p ->
                            "<item><title>${p.title}</title><link>${p.url}</link></item>"
                        }.let { "<rss><channel>$it</channel></rss>" }
                },
            )
        }
        return BlogViewModel(
            feedUrl = "https://test.example.com/feed.xml",
            repository = repo,
            scope = testScope,
        )
    }

    @Test
    fun initialState_isLoading() = runTest {
        val vm = makeViewModel(testPosts, testScope = this)
        assertTrue(vm.uiState.value is BlogUiState.Loading)
    }

    @Test
    fun afterFetch_successStateWithPosts() = runTest {
        val vm = makeViewModel(testPosts, testScope = this)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertTrue(state is BlogUiState.Success)
        assertEquals(2, state.posts.size)
    }

    @Test
    fun afterFetchError_errorStateWithMessage() = runTest {
        val vm = makeViewModel(throwOnFetch = RuntimeException("Network down"), testScope = this)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertTrue(state is BlogUiState.Error)
        assertEquals("Network down", state.message)
    }

    @Test
    fun afterFetchErrorWithNullMessage_errorStateWithUnknownMessage() = runTest {
        val vm = makeViewModel(throwOnFetch = RuntimeException(), testScope = this)
        advanceUntilIdle()
        val state = vm.uiState.value
        assertTrue(state is BlogUiState.Error)
        assertEquals("Unknown error", state.message)
    }

    @Test
    fun getPostByIndex_validIndex_returnsPost() = runTest {
        val vm = makeViewModel(testPosts, testScope = this)
        advanceUntilIdle()
        val post = vm.getPostByIndex(0)
        assertEquals("First", post?.title)
    }

    @Test
    fun getPostByIndex_outOfBounds_returnsNull() = runTest {
        val vm = makeViewModel(testPosts, testScope = this)
        advanceUntilIdle()
        assertNull(vm.getPostByIndex(99))
    }

    @Test
    fun getPostByIndex_negativeIndex_returnsNull() = runTest {
        val vm = makeViewModel(testPosts, testScope = this)
        advanceUntilIdle()
        assertNull(vm.getPostByIndex(-1))
    }

    @Test
    fun getPostByIndex_whenLoading_returnsNull() = runTest {
        val vm = makeViewModel(testPosts, testScope = this)
        assertNull(vm.getPostByIndex(0))
    }

    @Test
    fun getPosts_whenSuccess_returnsPosts() = runTest {
        val vm = makeViewModel(testPosts, testScope = this)
        advanceUntilIdle()
        assertEquals(2, vm.getPosts().size)
    }

    @Test
    fun getPosts_whenLoading_returnsEmpty() = runTest {
        val vm = makeViewModel(testPosts, testScope = this)
        assertTrue(vm.getPosts().isEmpty())
    }

    @Test
    fun getPosts_whenError_returnsEmpty() = runTest {
        val vm = makeViewModel(throwOnFetch = RuntimeException("fail"), testScope = this)
        advanceUntilIdle()
        assertTrue(vm.getPosts().isEmpty())
    }

    @Test
    fun fetchPosts_calledOnce_doesNotRefetch() = runTest {
        val fetchCount = intArrayOf(0)
        val repo = object : BlogRepository(
            feedUrl = "unused",
            networkClient = object : NetworkClient {
                override suspend fun fetchUrlText(url: String): String = ""
            },
        ) {
            override suspend fun fetchPosts(limit: Int): List<BlogPost> {
                fetchCount[0]++
                return listOf(BlogPost(id = "1", title = "A", url = "https://x.com/a", pubDate = null, summary = null, imageUrl = null, content = null))
            }
        }
        val vm = BlogViewModel(
            feedUrl = "https://test.example.com/feed.xml",
            repository = repo,
            scope = this,
        )
        advanceUntilIdle()
        vm.fetchPosts()
        advanceUntilIdle()
        assertEquals(1, fetchCount[0])
    }
}
