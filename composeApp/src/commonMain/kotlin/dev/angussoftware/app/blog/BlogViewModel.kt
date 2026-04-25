package dev.angussoftware.app.blog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.angussoftware.app.navigation.RSS_FEED_URL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal sealed class BlogUiState {
    data object Loading : BlogUiState()
    data class Success(val posts: List<BlogPost>) : BlogUiState()
    data class Error(val message: String) : BlogUiState()
}

internal class BlogViewModel(
    private val feedUrl: String = RSS_FEED_URL,
    private val repository: BlogRepository = BlogRepository(feedUrl),
) : ViewModel() {
    private val _uiState = MutableStateFlow<BlogUiState>(BlogUiState.Loading)
    val uiState: StateFlow<BlogUiState> = _uiState.asStateFlow()

    private var fetchStarted = false

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        if (fetchStarted) return
        fetchStarted = true

        viewModelScope.launch {
            _uiState.value = BlogUiState.Loading
            try {
                val posts = repository.fetchPosts(limit = Int.MAX_VALUE)
                _uiState.value = BlogUiState.Success(posts)
            } catch (t: Throwable) {
                _uiState.value = BlogUiState.Error(t.message ?: "Unknown error")
            }
        }
    }

    fun getPostByIndex(index: Int): BlogPost? {
        val state = _uiState.value
        return if (state is BlogUiState.Success && index in state.posts.indices) {
            state.posts[index]
        } else {
            null
        }
    }

    fun getPosts(): List<BlogPost> {
        val state = _uiState.value
        return if (state is BlogUiState.Success) state.posts else emptyList()
    }
}
