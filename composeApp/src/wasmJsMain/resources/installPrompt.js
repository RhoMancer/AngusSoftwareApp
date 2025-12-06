// Install Prompt JavaScript functions for Kotlin/WASM
// Handles platform detection, localStorage, and beforeinstallprompt event

// Store the deferred prompt event
let deferredPrompt = null;

// Callback to notify Kotlin when beforeinstallprompt fires
let onBeforeInstallPromptCallback = null;

// Platform detection: check if running on Android browser
function isAndroidPlatform() {
    // Try modern API first (navigator.userAgentData)
    if (navigator.userAgentData && navigator.userAgentData.platform) {
        return navigator.userAgentData.platform === "Android";
    }
    // Fallback to userAgent string
    return navigator.userAgent.indexOf("Android") !== -1;
}

// LocalStorage: get item
function getLocalStorageItem(key) {
    try {
        return localStorage.getItem(key);
    } catch (e) {
        console.log('[DEBUG_LOG] localStorage getItem error:', e.message);
        return null;
    }
}

// LocalStorage: set item
function setLocalStorageItem(key, value) {
    try {
        localStorage.setItem(key, value);
        return true;
    } catch (e) {
        console.log('[DEBUG_LOG] localStorage setItem error:', e.message);
        return false;
    }
}

// Register callback for beforeinstallprompt event
function registerBeforeInstallPromptListener(callback) {
    onBeforeInstallPromptCallback = callback;
    console.log('[DEBUG_LOG] beforeinstallprompt listener registered');
}

// Check if deferred prompt is available
function hasDeferredPrompt() {
    return deferredPrompt !== null;
}

// Trigger the deferred prompt and return user choice
async function triggerInstallPrompt() {
    if (!deferredPrompt) {
        console.log('[DEBUG_LOG] No deferred prompt available');
        return "dismissed";
    }
    
    try {
        console.log('[DEBUG_LOG] Triggering install prompt');
        deferredPrompt.prompt();
        const choiceResult = await deferredPrompt.userChoice;
        console.log('[DEBUG_LOG] User choice:', choiceResult.outcome);
        
        // Clear the deferred prompt - can only be used once
        deferredPrompt = null;
        
        return choiceResult.outcome; // "accepted" or "dismissed"
    } catch (e) {
        console.log('[DEBUG_LOG] Install prompt error:', e.message);
        deferredPrompt = null;
        return "dismissed";
    }
}

// Clear the deferred prompt without using it
function clearDeferredPrompt() {
    deferredPrompt = null;
    console.log('[DEBUG_LOG] Deferred prompt cleared');
}

// Listen for beforeinstallprompt event
window.addEventListener('beforeinstallprompt', (e) => {
    console.log('[DEBUG_LOG] beforeinstallprompt event fired');
    // Prevent the mini-infobar from appearing on mobile
    e.preventDefault();
    // Store the event for later use
    deferredPrompt = e;
    // Notify Kotlin if callback is registered
    if (onBeforeInstallPromptCallback) {
        onBeforeInstallPromptCallback();
    }
});

// Listen for appinstalled event
window.addEventListener('appinstalled', () => {
    console.log('[DEBUG_LOG] PWA was installed');
    deferredPrompt = null;
});

// Make functions globally available for Kotlin/WASM
window.isAndroidPlatform = isAndroidPlatform;
window.getLocalStorageItem = getLocalStorageItem;
window.setLocalStorageItem = setLocalStorageItem;
window.registerBeforeInstallPromptListener = registerBeforeInstallPromptListener;
window.hasDeferredPrompt = hasDeferredPrompt;
window.triggerInstallPrompt = triggerInstallPrompt;
window.clearDeferredPrompt = clearDeferredPrompt;
