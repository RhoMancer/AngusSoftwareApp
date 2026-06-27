package dev.angussoftware.app.installprompt

import kotlinx.coroutines.await
import kotlin.js.Promise

// External JavaScript functions from installPrompt.js
@JsName("isAndroidPlatform")
private external fun jsIsAndroidPlatform(): Boolean

@JsName("getLocalStorageItem")
private external fun jsGetLocalStorageItem(key: String): String?

@JsName("setLocalStorageItem")
private external fun jsSetLocalStorageItem(key: String, value: String): Boolean

@JsName("registerBeforeInstallPromptListener")
private external fun jsRegisterBeforeInstallPromptListener(callback: () -> Unit)

@JsName("hasDeferredPrompt")
private external fun jsHasDeferredPrompt(): Boolean

@JsName("triggerInstallPrompt")
private external fun jsTriggerInstallPrompt(): Promise<JsAny>

@JsName("clearDeferredPrompt")
private external fun jsClearDeferredPrompt()

@JsName("Date")
@Suppress("UtilityClassWithPublicConstructor") // JS external class mirrors browser Date API
private external class JsDate {
    companion object {
        fun now(): Double
    }
}

/**
 * Default implementation using actual browser APIs.
 */
internal class DefaultInstallPromptPlatform(
    private val urlOpener: (String) -> Unit,
) : InstallPromptPlatform {
    override fun isAndroid(): Boolean = jsIsAndroidPlatform()

    override fun getStorageItem(key: String): String? = jsGetLocalStorageItem(key)

    override fun setStorageItem(key: String, value: String) {
        jsSetLocalStorageItem(key, value)
    }

    override fun getCurrentTimeMillis(): Long = JsDate.now().toLong()

    override fun hasDeferredPrompt(): Boolean = jsHasDeferredPrompt()

    override suspend fun triggerInstallPrompt(): String {
        val result: JsAny = jsTriggerInstallPrompt().await<JsAny>()
        return result.toString()
    }

    override fun clearDeferredPrompt() = jsClearDeferredPrompt()

    override fun registerBeforeInstallPromptListener(callback: () -> Unit) {
        jsRegisterBeforeInstallPromptListener(callback)
    }

    override fun openUrl(url: String) = urlOpener(url)
}
