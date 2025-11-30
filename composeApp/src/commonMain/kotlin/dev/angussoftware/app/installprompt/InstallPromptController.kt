package dev.angussoftware.app.installprompt

/**
 * Play Store URL for the Android app.
 * Update the package ID as needed.
 */
internal const val PLAY_STORE_URL: String =
    "https://play.google.com/store/apps/details?id=dev.angussoftware.app"

/**
 * LocalStorage keys for dismissal timestamps.
 */
internal const val PLAY_PROMPT_DISMISSED_AT_KEY: String = "playPromptDismissedAt"
internal const val PWA_PROMPT_DISMISSED_AT_KEY: String = "pwaPromptDismissedAt"

/**
 * Dismissal cooldown period in days.
 */
internal const val DISMISSAL_COOLDOWN_DAYS: Int = 7

/**
 * Milliseconds in a day.
 */
internal const val MILLIS_PER_DAY: Long = 24 * 60 * 60 * 1000L

/**
 * Interface for platform detection and storage operations.
 * Allows for easier testing by mocking these dependencies.
 */
internal interface InstallPromptPlatform {
    fun isAndroid(): Boolean

    fun getStorageItem(key: String): String?

    fun setStorageItem(
        key: String,
        value: String,
    )

    fun getCurrentTimeMillis(): Long

    fun hasDeferredPrompt(): Boolean

    suspend fun triggerInstallPrompt(): String

    fun clearDeferredPrompt()

    fun registerBeforeInstallPromptListener(callback: () -> Unit)

    fun openUrl(url: String)
}

/**
 * State holder for install prompt visibility and status.
 */
internal data class InstallPromptState(
    val isPlayStoreBannerVisible: Boolean = false,
    val isPwaBannerVisible: Boolean = false,
    val isAndroidPlatform: Boolean = false,
    val hasDeferredPrompt: Boolean = false,
)

/**
 * Controller for managing install prompt behavior.
 *
 * On Android browsers: Shows Play Store banner (if not recently dismissed).
 * On non-Android platforms: Shows PWA install banner when beforeinstallprompt fires
 * (if not recently dismissed).
 */
internal class InstallPromptController(
    private val platform: InstallPromptPlatform,
    private val cooldownDays: Int = DISMISSAL_COOLDOWN_DAYS,
) {
    private var _state = InstallPromptState()
    val state: InstallPromptState get() = _state

    private var stateChangeListener: ((InstallPromptState) -> Unit)? = null

    fun setStateChangeListener(listener: (InstallPromptState) -> Unit) {
        stateChangeListener = listener
    }

    /**
     * Initialize the controller. Call this on startup.
     */
    fun initialize() {
        val isAndroid = platform.isAndroid()
        _state = _state.copy(isAndroidPlatform = isAndroid)

        if (isAndroid) {
            // Android: check if Play Store banner should be shown
            val shouldShowPlayStore = !isRecentlyDismissed(PLAY_PROMPT_DISMISSED_AT_KEY)
            _state = _state.copy(isPlayStoreBannerVisible = shouldShowPlayStore)
        } else {
            // Non-Android: register for beforeinstallprompt event
            platform.registerBeforeInstallPromptListener {
                onBeforeInstallPrompt()
            }
            // Check if deferred prompt is already available (unlikely but possible)
            if (platform.hasDeferredPrompt()) {
                onBeforeInstallPrompt()
            }
        }

        notifyStateChange()
    }

    /**
     * Called when beforeinstallprompt event fires.
     * Made internal for testing purposes.
     */
    internal fun onBeforeInstallPrompt() {
        if (_state.isAndroidPlatform) {
            // On Android, we prefer Play Store - don't show PWA banner
            return
        }

        val shouldShowPwa = !isRecentlyDismissed(PWA_PROMPT_DISMISSED_AT_KEY)
        _state =
            _state.copy(
                hasDeferredPrompt = true,
                isPwaBannerVisible = shouldShowPwa,
            )
        notifyStateChange()
    }

    /**
     * Handle Play Store install button click.
     * Opens the Play Store URL.
     */
    fun onPlayStoreInstallClick() {
        platform.openUrl(PLAY_STORE_URL)
        // Optionally hide the banner after clicking
        _state = _state.copy(isPlayStoreBannerVisible = false)
        notifyStateChange()
    }

    /**
     * Handle Play Store banner dismiss.
     * Hides banner and stores dismissal timestamp.
     */
    fun onPlayStoreDismiss() {
        saveDismissalTimestamp(PLAY_PROMPT_DISMISSED_AT_KEY)
        _state = _state.copy(isPlayStoreBannerVisible = false)
        notifyStateChange()
    }

    /**
     * Handle PWA install button click.
     * Triggers the deferred install prompt.
     */
    suspend fun onPwaInstallClick() {
        if (!platform.hasDeferredPrompt()) {
            return
        }

        platform.triggerInstallPrompt()
        // Clear the deferred prompt regardless of outcome (it's single-use)
        _state =
            _state.copy(
                isPwaBannerVisible = false,
                hasDeferredPrompt = false,
            )
        notifyStateChange()
    }

    /**
     * Handle PWA banner dismiss.
     * Hides banner and stores dismissal timestamp.
     */
    fun onPwaDismiss() {
        saveDismissalTimestamp(PWA_PROMPT_DISMISSED_AT_KEY)
        _state = _state.copy(isPwaBannerVisible = false)
        notifyStateChange()
    }

    /**
     * Check if a banner was dismissed within the cooldown period.
     */
    private fun isRecentlyDismissed(key: String): Boolean {
        val dismissedAtStr = platform.getStorageItem(key) ?: return false
        val dismissedAt = dismissedAtStr.toLongOrNull() ?: return false
        val now = platform.getCurrentTimeMillis()
        val cooldownMillis = cooldownDays * MILLIS_PER_DAY
        return (now - dismissedAt) < cooldownMillis
    }

    /**
     * Save the current timestamp as dismissal time.
     */
    private fun saveDismissalTimestamp(key: String) {
        val now = platform.getCurrentTimeMillis()
        platform.setStorageItem(key, now.toString())
    }

    private fun notifyStateChange() {
        stateChangeListener?.invoke(_state)
    }
}
