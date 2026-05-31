package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_notes")
data class SavedNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val subject: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_histories")
data class QuizHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "ai" or "system"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "English",
    val type: String = "text" // "text" or "voice" or "scanner"
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Guest Student",
    val studentType: String = "Science Student (PU)", // School, PU Science, Commerce, Arts, Competitive
    val streak: Int = 3,
    val totalPoints: Int = 120,
    val selectedLanguage: String = "English",
    val isDarkMode: Boolean = true,
    val tutorTone: String = "Friendly Teacher" // Friendly Teacher, Expert Scientist, Motivational
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "Note", "Quiz", "Scan", "Ask", "Login"
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
