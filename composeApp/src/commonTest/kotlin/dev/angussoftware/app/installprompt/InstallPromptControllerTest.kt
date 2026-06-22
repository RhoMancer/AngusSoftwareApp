package dev.angussoftware.app.installprompt

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class InstallPromptControllerTest {

    /**
     * Mock platform for testing InstallPromptController.
     */
    private class MockPlatform(
        private val isAndroidPlatform: Boolean = false,
        private var currentTimeMillis: Long = 0L,
        private var deferredPromptAvailable: Boolean = false,
    ) : InstallPromptPlatform {
        val storage = mutableMapOf<String, String>()
        var openedUrls = mutableListOf<String>()
        var promptTriggered = false
        var promptCleared = false
        var beforeInstallPromptCallback: (() -> Unit)? = null

        override fun isAndroid(): Boolean = isAndroidPlatform

        override fun getStorageItem(key: String): String? = storage[key]

        override fun setStorageItem(key: String, value: String) {
            storage[key] = value
        }

        override fun getCurrentTimeMillis(): Long = currentTimeMillis

        override fun hasDeferredPrompt(): Boolean = deferredPromptAvailable

        override suspend fun triggerInstallPrompt(): String {
            promptTriggered = true
            deferredPromptAvailable = false
            return "accepted"
        }

        override fun clearDeferredPrompt() {
            promptCleared = true
            deferredPromptAvailable = false
        }

        override fun registerBeforeInstallPromptListener(callback: () -> Unit) {
            beforeInstallPromptCallback = callback
        }

        override fun openUrl(url: String) {
            openedUrls.add(url)
        }

        fun setDeferredPromptAvailable(available: Boolean) {
            deferredPromptAvailable = available
        }

        fun setCurrentTime(timeMillis: Long) {
            currentTimeMillis = timeMillis
        }

        fun simulateBeforeInstallPrompt() {
            deferredPromptAvailable = true
            beforeInstallPromptCallback?.invoke()
        }
    }

    // ========== Android Play Store Banner Tests ==========

    @Test
    internal fun givenAndroidPlatformWithNoDismissal_whenInitialized_thenPlayStoreBannerVisible() {
        val platform = MockPlatform(isAndroidPlatform = true)
        val controller = InstallPromptController(platform)

        controller.initialize()

        assertTrue(controller.state.isPlayStoreBannerVisible)
        assertFalse(controller.state.isPwaBannerVisible)
        assertTrue(controller.state.isAndroidPlatform)
    }

    @Test
    internal fun givenAndroidPlatformWithRecentDismissal_whenInitialized_thenPlayStoreBannerNotVisible() {
        val platform = MockPlatform(isAndroidPlatform = true, currentTimeMillis = MILLIS_PER_DAY * 3)
        // Dismissed 2 days ago (within 7 day cooldown)
        platform.storage[PLAY_PROMPT_DISMISSED_AT_KEY] = (MILLIS_PER_DAY * 1).toString()
        val controller = InstallPromptController(platform)

        controller.initialize()

        assertFalse(controller.state.isPlayStoreBannerVisible)
    }

    @Test
    internal fun givenAndroidPlatformWithOldDismissal_whenInitialized_thenPlayStoreBannerVisible() {
        val platform = MockPlatform(isAndroidPlatform = true, currentTimeMillis = MILLIS_PER_DAY * 10)
        // Dismissed 10 days ago (outside 7 day cooldown)
        platform.storage[PLAY_PROMPT_DISMISSED_AT_KEY] = "0"
        val controller = InstallPromptController(platform)

        controller.initialize()

        assertTrue(controller.state.isPlayStoreBannerVisible)
    }

    // ========== Non-Android PWA Banner Tests ==========

    @Test
    internal fun givenNonAndroidPlatformWithNoDismissal_whenBeforeInstallPromptFires_thenPwaBannerVisible() {
        val platform = MockPlatform(isAndroidPlatform = false)
        val controller = InstallPromptController(platform)

        controller.initialize()
        platform.simulateBeforeInstallPrompt()

        assertTrue(controller.state.isPwaBannerVisible)
        assertFalse(controller.state.isPlayStoreBannerVisible)
        assertTrue(controller.state.hasDeferredPrompt)
    }

    @Test
    internal fun givenNonAndroidPlatformWithRecentDismissal_whenBeforeInstallPromptFires_thenPwaBannerNotVisible() {
        val platform = MockPlatform(isAndroidPlatform = false, currentTimeMillis = MILLIS_PER_DAY * 3)
        // Dismissed 2 days ago (within 7 day cooldown)
        platform.storage[PWA_PROMPT_DISMISSED_AT_KEY] = (MILLIS_PER_DAY * 1).toString()
        val controller = InstallPromptController(platform)

        controller.initialize()
        platform.simulateBeforeInstallPrompt()

        assertFalse(controller.state.isPwaBannerVisible)
        assertTrue(controller.state.hasDeferredPrompt)
    }

    @Test
    internal fun givenNonAndroidPlatform_whenInitialized_thenPlayStoreBannerNotVisible() {
        val platform = MockPlatform(isAndroidPlatform = false)
        val controller = InstallPromptController(platform)

        controller.initialize()

        assertFalse(controller.state.isPlayStoreBannerVisible)
        assertFalse(controller.state.isAndroidPlatform)
    }

    // ========== Dismissal Behavior Tests ==========

    @Test
    internal fun givenPlayStoreBannerVisible_whenDismissed_thenBannerHiddenAndTimestampStored() {
        val currentTime = MILLIS_PER_DAY * 5
        val platform = MockPlatform(isAndroidPlatform = true, currentTimeMillis = currentTime)
        val controller = InstallPromptController(platform)
        controller.initialize()

        controller.onPlayStoreDismiss()

        assertFalse(controller.state.isPlayStoreBannerVisible)
        assertEquals(currentTime.toString(), platform.storage[PLAY_PROMPT_DISMISSED_AT_KEY])
    }

    @Test
    internal fun givenPwaBannerVisible_whenDismissed_thenBannerHiddenAndTimestampStored() {
        val currentTime = MILLIS_PER_DAY * 5
        val platform = MockPlatform(isAndroidPlatform = false, currentTimeMillis = currentTime)
        val controller = InstallPromptController(platform)
        controller.initialize()
        platform.simulateBeforeInstallPrompt()

        controller.onPwaDismiss()

        assertFalse(controller.state.isPwaBannerVisible)
        assertEquals(currentTime.toString(), platform.storage[PWA_PROMPT_DISMISSED_AT_KEY])
    }

    // ========== Install Click Behavior Tests ==========

    @Test
    internal fun givenPlayStoreBannerVisible_whenInstallClicked_thenPlayStoreUrlOpened() {
        val platform = MockPlatform(isAndroidPlatform = true)
        val controller = InstallPromptController(platform)
        controller.initialize()

        controller.onPlayStoreInstallClick()

        assertEquals(1, platform.openedUrls.size)
        assertEquals(PLAY_STORE_URL, platform.openedUrls[0])
        assertFalse(controller.state.isPlayStoreBannerVisible)
    }

    @Test
    internal fun givenPwaBannerVisibleWithDeferredPrompt_whenInstallClicked_thenPromptTriggeredAndBannerHidden() =
        runTest {
            val platform = MockPlatform(isAndroidPlatform = false)
            val controller = InstallPromptController(platform)
            controller.initialize()
            platform.simulateBeforeInstallPrompt()

            controller.onPwaInstallClick()

            assertTrue(platform.promptTriggered)
            assertFalse(controller.state.isPwaBannerVisible)
            assertFalse(controller.state.hasDeferredPrompt)
        }

    @Test
    internal fun givenNoDeferredPrompt_whenPwaInstallClicked_thenNothingHappens() =
        runTest {
            val platform = MockPlatform(isAndroidPlatform = false)
            val controller = InstallPromptController(platform)
            controller.initialize()
            // No beforeInstallPrompt fired

            controller.onPwaInstallClick()

            assertFalse(platform.promptTriggered)
        }

    // ========== Priority Tests ==========

    @Test
    internal fun givenAndroidPlatform_whenBeforeInstallPromptFires_thenPwaBannerNotShown() {
        val platform = MockPlatform(isAndroidPlatform = true)
        val controller = InstallPromptController(platform)
        controller.initialize()

        // Manually trigger beforeInstallPrompt callback (simulating event on Android)
        controller.onBeforeInstallPrompt()

        // Play Store banner should remain visible, PWA banner should not appear
        assertTrue(controller.state.isPlayStoreBannerVisible)
        assertFalse(controller.state.isPwaBannerVisible)
    }

    // ========== State Change Listener Tests ==========

    @Test
    internal fun givenStateChangeListener_whenStateChanges_thenListenerInvoked() {
        val platform = MockPlatform(isAndroidPlatform = true)
        val controller = InstallPromptController(platform)
        controller.initialize()

        val receivedStates = mutableListOf<InstallPromptState>()
        controller.setStateChangeListener { state -> receivedStates.add(state) }

        // Trigger a state change
        controller.onPlayStoreDismiss()

        assertEquals(1, receivedStates.size)
        assertFalse(receivedStates[0].isPlayStoreBannerVisible)
    }

    // ========== Deferred Prompt at Init Tests ==========

    @Test
    internal fun givenNonAndroidPlatformWithDeferredPromptAtInit_thenPwaBannerVisibleImmediately() {
        val platform = MockPlatform(isAndroidPlatform = false, deferredPromptAvailable = true)
        val controller = InstallPromptController(platform)

        controller.initialize()

        // Should show PWA banner immediately since deferred prompt was already available
        assertTrue(controller.state.hasDeferredPrompt)
        assertTrue(controller.state.isPwaBannerVisible)
    }

    // ========== Invalid Dismissal Timestamp Tests ==========

    @Test
    internal fun givenNonNumericDismissalTimestamp_thenTreatedAsNotDismissed() {
        val platform = MockPlatform(isAndroidPlatform = true)
        platform.storage[PLAY_PROMPT_DISMISSED_AT_KEY] = "not-a-number"
        val controller = InstallPromptController(platform)

        controller.initialize()

        // Invalid timestamp should be treated as not dismissed → banner visible
        assertTrue(controller.state.isPlayStoreBannerVisible)
    }
}
