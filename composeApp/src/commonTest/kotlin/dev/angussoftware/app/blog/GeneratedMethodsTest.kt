package dev.angussoftware.app.blog

import dev.angussoftware.app.installprompt.InstallPromptState
import dev.angussoftware.app.ui.utils.WindowAdaptiveInfo
import dev.angussoftware.app.ui.utils.WindowWidthSizeClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests for compiler-generated data class methods (equals, hashCode, copy, toString)
 * on data classes used in production state management.
 */
internal class GeneratedMethodsTest {

    // === InstallPromptState — .copy() is called on every state transition ===

    @Test
    fun installPromptState_equals_sameValues() {
        val a = InstallPromptState(isPlayStoreBannerVisible = true, isPwaBannerVisible = false, isAndroidPlatform = true, hasDeferredPrompt = false)
        val b = InstallPromptState(isPlayStoreBannerVisible = true, isPwaBannerVisible = false, isAndroidPlatform = true, hasDeferredPrompt = false)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun installPromptState_equals_differentValues() {
        val a = InstallPromptState(isPlayStoreBannerVisible = true)
        val b = InstallPromptState(isPlayStoreBannerVisible = false)
        assertNotEquals(a, b)
    }

    @Test
    fun installPromptState_copy_createsEqual() {
        val original = InstallPromptState(isPlayStoreBannerVisible = true, isPwaBannerVisible = true, isAndroidPlatform = false, hasDeferredPrompt = true)
        val copy = original.copy()
        assertEquals(original, copy)
    }

    @Test
    fun installPromptState_copy_withChanges() {
        val original = InstallPromptState(isPlayStoreBannerVisible = true)
        val modified = original.copy(isPlayStoreBannerVisible = false)
        assertEquals(false, modified.isPlayStoreBannerVisible)
        assertEquals(original.isAndroidPlatform, modified.isAndroidPlatform)
    }

    @Test
    fun installPromptState_toString_containsFields() {
        val state = InstallPromptState(isPlayStoreBannerVisible = true)
        val str = state.toString()
        assertTrue(str.contains("isPlayStoreBannerVisible"))
        assertTrue(str.contains("true"))
    }

    @Test
    fun installPromptState_componentN_destructures() {
        val state = InstallPromptState(isPlayStoreBannerVisible = true, isPwaBannerVisible = false, isAndroidPlatform = true, hasDeferredPrompt = false)
        val (playStore, pwa, android, deferred) = state
        assertEquals(true, playStore)
        assertEquals(false, pwa)
        assertEquals(true, android)
        assertEquals(false, deferred)
    }

    // === BlogUiState.Success ===

    @Test
    fun blogUiStateSuccess_equals_samePosts() {
        val posts = listOf(BlogPost(id = "1", title = "T", url = "U", pubDate = null, summary = null, imageUrl = null, content = null))
        val a = BlogUiState.Success(posts)
        val b = BlogUiState.Success(posts)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun blogUiStateSuccess_copy() {
        val posts = listOf(BlogPost(id = "1", title = "T", url = "U", pubDate = null, summary = null, imageUrl = null, content = null))
        val state = BlogUiState.Success(posts)
        val copy = state.copy(posts = emptyList())
        assertEquals(0, copy.posts.size)
    }

    @Test
    fun blogUiStateSuccess_toString() {
        val state = BlogUiState.Success(emptyList())
        assertTrue(state.toString().contains("Success"))
    }

    @Test
    fun blogUiStateSuccess_component1() {
        val posts = listOf(BlogPost(id = "1", title = "T", url = "U", pubDate = null, summary = null, imageUrl = null, content = null))
        val state = BlogUiState.Success(posts)
        val (extracted) = state
        assertEquals(1, extracted.size)
    }

    // === BlogUiState.Error ===

    @Test
    fun blogUiStateError_equals_sameMessage() {
        val a = BlogUiState.Error("fail")
        val b = BlogUiState.Error("fail")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun blogUiStateError_copy() {
        val state = BlogUiState.Error("original")
        val copy = state.copy(message = "changed")
        assertEquals("changed", copy.message)
    }

    @Test
    fun blogUiStateError_toString() {
        val state = BlogUiState.Error("boom")
        assertTrue(state.toString().contains("boom"))
    }

    @Test
    fun blogUiStateError_component1() {
        val state = BlogUiState.Error("msg")
        val (msg) = state
        assertEquals("msg", msg)
    }

    // === WindowAdaptiveInfo ===

    @Test
    fun windowAdaptiveInfo_equals_sameSizeClass() {
        val a = WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT)
        val b = WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun windowAdaptiveInfo_copy() {
        val original = WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT)
        val copy = original.copy(sizeClass = WindowWidthSizeClass.EXPANDED)
        assertEquals(WindowWidthSizeClass.EXPANDED, copy.sizeClass)
    }

    @Test
    fun windowAdaptiveInfo_toString() {
        val info = WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT)
        assertTrue(info.toString().contains("COMPACT"))
    }
}
