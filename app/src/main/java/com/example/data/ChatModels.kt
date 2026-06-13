package com.example.data

import java.util.UUID

/**
 * Represents the actor who sent the message.
 */
enum class MessageSender {
    USER,
    AI_ONLINE,
    AI_OFFLINE,
    SYSTEM
}

/**
 * Data classification for an individual chat event.
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Offline llama.cpp inference configuration settings. Contains advanced granular handles.
 */
data class InferenceSettings(
    val systemPrompt: String = "You are Gemini, an elite, highly intelligent, and academically brilliant AI assistant built specifically to optimize student workflows.",
    val temperature: Float = 0.7f,
    val minP: Float = 0.05f,
    val topP: Float = 0.9f,
    val contextSize: Int = 1024,
    val numThreads: Int = 4,
    val useMmap: Boolean = true,
    val useMlock: Boolean = false,
    val contextHistoryLength: Int = 10
)

/**
 * Represents a session history record.
 */
data class ChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val messages: List<ChatMessage> = emptyList(),
    val lastActive: Long = System.currentTimeMillis()
)

/**
 * Represents a student workflow item.
 */
data class StudentTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false,
    val dueDate: String? = null
)
