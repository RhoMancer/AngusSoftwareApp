// External JavaScript network function for Kotlin/WASM
// Avoids WASM type casting issues with Response objects

async function fetchUrlTextExternal(url) {
    console.log('[DEBUG_LOG] External JavaScript fetch starting for:', url);
    try {
        const response = await fetch(url);
        console.log('[DEBUG_LOG] External fetch completed, status:', response.status);

        if (!response.ok) {
            const errorMsg = `HTTP ${response.status}: ${response.statusText}`;
            console.log('[DEBUG_LOG] Response not ok:', errorMsg);
            throw new Error(errorMsg);
        }

        const text = await response.text();
        console.log('[DEBUG_LOG] Text extracted successfully, length:', text.length);
        console.log('[DEBUG_LOG] First 100 chars:', text.substring(0, 100));
        return text;
    } catch (error) {
        console.log('[DEBUG_LOG] Error in external fetch:', error.message);
        throw error;
    }
}

// Make function globally available for Kotlin/WASM
window.fetchUrlTextExternal = fetchUrlTextExternal;
