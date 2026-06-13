package com.example

import android.util.Log

/**
 * JNI Bridge to the llama.cpp C++ library.
 * This class handles low-level NDK calls to load, query, and unload a GGUF model.
 */
class LlamaNative {

    companion object {
        private const val TAG = "LlamaNative"
        var isLibraryLoaded = false
            private set

        init {
            try {
                System.loadLibrary("llama_android")
                isLibraryLoaded = true
                Log.d(TAG, "libllama_android.so loaded successfully!")
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "Native library 'llama_android' not found. Fallback simulator will be used.")
                isLibraryLoaded = false
            }
        }
    }

    /**
     * Initializes the static llama_context inside C++ memory space.
     * Returns a long pointer address to the native context, or 0 if initialization fails.
     */
    external fun initModel(
        modelPath: String,
        temperature: Float,
        maxTokens: Int,
        topP: Float,
        topK: Int
    ): Long

    /**
     * Feeds the prompt to the llama.cpp sampling chain.
     * Tokens are yielded dynamically back to Kotlin via a lambda callback to support streaming UI experiences,
     * and the final combined string response is returned.
     */
    external fun generateAnswer(
        contextPointer: Long,
        prompt: String,
        onTokenGenerated: (String) -> Unit
    ): String

    /**
     * Explicitly frees the llama_vocab, llama_model, and llama_context in C++ memory
     * to recover valuable RAM on low-resource devices.
     */
    external fun freeModel(contextPointer: Long)
}
