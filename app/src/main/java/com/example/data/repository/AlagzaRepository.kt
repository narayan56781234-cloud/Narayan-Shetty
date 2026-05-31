package com.example.data.repository

import com.example.data.api.RetrofitClient
import com.example.data.local.LocalDao
import com.example.data.model.ActivityLog
import com.example.data.model.ChatMessage
import com.example.data.model.QuizHistory
import com.example.data.model.SavedNote
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AlagzaRepository(private val localDao: LocalDao) {

    // --- Notes ---
    val allNotes: Flow<List<SavedNote>> = localDao.getAllNotes()

    suspend fun saveNote(title: String, content: String, subject: String): Long {
        val note = SavedNote(title = title, content = content, subject = subject)
        val noteId = localDao.insertNote(note)
        logActivity("Note", "Created short notes on '$title' for $subject")
        incrementUserPoints(15)
        return noteId
    }

    suspend fun deleteNote(id: Long) {
        localDao.deleteNoteById(id)
        logActivity("Note", "Deleted educational note")
    }

    // --- Quiz Score ---
    val quizHistories: Flow<List<QuizHistory>> = localDao.getAllQuizHistories()

    suspend fun saveQuizResult(subject: String, score: Int, totalQuestions: Int) {
        val history = QuizHistory(subject = subject, score = score, totalQuestions = totalQuestions)
        localDao.insertQuizHistory(history)
        logActivity("Quiz", "Scored $score/$totalQuestions in $subject practice test")
        val ptsReward = score * 10
        incrementUserPoints(ptsReward + 5) // Base pts and points per correct answer
    }

    // --- Chat Messages ---
    val chatMessages: Flow<List<ChatMessage>> = localDao.getChatMessages()

    suspend fun insertChatMessage(message: ChatMessage): Long {
        return localDao.insertChatMessage(message)
    }

    suspend fun clearChat() {
        localDao.clearChatMessages()
        logActivity("Ask", "Cleared interactive tutor chat logs")
    }

    // --- User Profile ---
    val userProfile: Flow<UserProfile?> = localDao.getUserProfile()

    suspend fun ensureProfileExists() {
        val existing = localDao.getUserProfileOnce()
        if (existing == null) {
            val defaultProfile = UserProfile()
            localDao.insertUserProfile(defaultProfile)
            logActivity("Login", "Welcome to Alagza! Activated Guest Student engine.")
        }
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        localDao.insertUserProfile(profile)
    }

    suspend fun incrementUserPoints(points: Int) {
        val current = localDao.getUserProfileOnce() ?: UserProfile()
        val updated = current.copy(totalPoints = current.totalPoints + points)
        localDao.insertUserProfile(updated)
    }

    suspend fun incrementDailyStreak() {
        val current = localDao.getUserProfileOnce() ?: UserProfile()
        val updated = current.copy(streak = current.streak + 1)
        localDao.insertUserProfile(updated)
        logActivity("Streak", "Streaked up! Daily streak is now ${updated.streak} days!")
    }

    // --- Activity Logs ---
    val recentActivities: Flow<List<ActivityLog>> = localDao.getRecentActivities()

    suspend fun logActivity(type: String, description: String) {
        localDao.insertActivityLog(ActivityLog(type = type, description = description))
    }

    // --- Core Solver and Tutor Trigger ---
    suspend fun askTutor(
        query: String,
        subject: String,
        isExpert: Boolean,
        language: String
    ): String {
        val profile = localDao.getUserProfileOnce() ?: UserProfile()
        val tone = profile.tutorTone
        val studentType = profile.studentType

        val systemPrompt = """
            You are "Alagza", an ultra-intelligent, friendly, and highly encouraging AI Educational Assistant for $studentType. 
            The educational tone selected by the student is: '$tone'.
            
            Focus on providing comprehensive step-by-step help, explaining every concept thoroughly yet accessibly.
            The current subject is: $subject.
            
            Current student learning mode: ${if (isExpert) "Expert Mode (Includes strict academic context, formulas, robust definitions, and diagram-solving structures if requested)" else "Beginner Mode (Simplified language, relatable step-by-step descriptions, no complex jargon upfront)"}.
            
            Please outline formulas clearly. If the user asks a question that would benefit from a schematic chart or drawing, suggest and describe a suitable visual diagram or blueprint.
            Always keep explanations child-safe, academic, highly educational, and motivating. Avoid any unrelated topics.
        """.trimIndent()

        logActivity("Ask", "Asked Alagza tutor about $subject: \"${if(query.length > 30) query.take(30) + "..." else query}\"")
        incrementUserPoints(5)

        return RetrofitClient.generateEducationalAnswer(
            systemPrompt = systemPrompt,
            userPrompt = query,
            language = language
        )
    }

    // --- AI Note Generator ---
    suspend fun generateShortNotes(chapterTitle: String, subject: String, language: String): String {
        val systemPrompt = """
            You are "Alagza", a world-class chapter annotator and study guide publisher. 
            Write short, dense revision notes, formula sheets, key concepts, and highlights for the chapter provided by the student.
            Organize the information elegantly with headers, bullet points, and memory mnemonics.
        """.trimIndent()

        val prompt = "Generate master notes, definitions, formula sheets, and 3 quick-recall flashcards for: '$chapterTitle' in $subject."
        logActivity("Note", "Triggered Alagza Note Forge for chapter: $chapterTitle")
        incrementUserPoints(10)

        return RetrofitClient.generateEducationalAnswer(
            systemPrompt = systemPrompt,
            userPrompt = prompt,
            language = language
        )
    }

    // --- MCQ Generator ---
    suspend fun generateFlashQuiz(subject: String, count: Int, language: String): String {
        val systemPrompt = """
            You are "Alagza MCQ Generator". Generate exactly $count multiple-choice questions (MCQs) for the subject '$subject'.
            Return the MCQs formatted as a valid JSON array so that the app can parse them directly, or a custom structured text string that looks like:
            
            [
              {
                "question": "Question text...",
                "options": ["Option A", "Option B", "Option C", "Option D"],
                "answerIndex": 0,
                "explanation": "Why this answer is correct..."
              }
            ]
            
            CRITICAL: Return ONLY raw, valid JSON with that exact structure. Avoid other text, code blocks, or formatting like ```json.
        """.trimIndent()

        val prompt = "Generate $count high-yield multiple choice questions on the subject of '$subject'."
        return RetrofitClient.generateEducationalAnswer(
            systemPrompt = systemPrompt,
            userPrompt = prompt,
            language = language
        )
    }
}
