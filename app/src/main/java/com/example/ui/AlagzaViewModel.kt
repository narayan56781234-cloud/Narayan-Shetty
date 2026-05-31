package com.example.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ActivityLog
import com.example.data.model.ChatMessage
import com.example.data.model.QuizHistory
import com.example.data.model.SavedNote
import com.example.data.model.UserProfile
import com.example.data.repository.AlagzaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class AlagzaViewModel(
    application: Application,
    private val repository: AlagzaRepository
) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    // --- Navigation ---
    private val _currentScreen = MutableStateFlow("welcome") // "welcome", "home", "solver", "scanner", "quiz", "notes", "profile"
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
        viewModelScope.launch {
            repository.logActivity("Nav", "Navigated to $screen screen")
        }
    }

    // --- State flows from Room DB ---
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .combine(MutableStateFlow(UserProfile())) { dbProfile, default ->
            dbProfile ?: default
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val allNotes: StateFlow<List<SavedNote>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizHistories: StateFlow<List<QuizHistory>> = repository.quizHistories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentActivities: StateFlow<List<ActivityLog>> = repository.recentActivities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- App Custom States ---
    private val _isMusicPlaying = MutableStateFlow(false)
    val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

    fun toggleMusic() {
        _isMusicPlaying.value = !_isMusicPlaying.value
        viewModelScope.launch {
            repository.logActivity("Audio", "Toggled futuristic ambient music: ${_isMusicPlaying.value}")
        }
    }

    // AI Solver Screen States
    private val _solverSubject = MutableStateFlow("Mathematics")
    val solverSubject: StateFlow<String> = _solverSubject.asStateFlow()

    private val _isExpertMode = MutableStateFlow(false)
    val isExpertMode: StateFlow<Boolean> = _isExpertMode.asStateFlow()

    private val _isAILoading = MutableStateFlow(false)
    val isAILoading: StateFlow<Boolean> = _isAILoading.asStateFlow()

    private val _lastAIResponse = MutableStateFlow("")
    val lastAIResponse: StateFlow<String> = _lastAIResponse.asStateFlow()

    fun setSolverSubject(subject: String) {
        _solverSubject.value = subject
    }

    fun toggleExpertMode() {
        _isExpertMode.value = !_isExpertMode.value
    }

    // --- Ask AI solver ---
    fun askAlagzaDoubt(query: String) {
        if (query.trim().isEmpty()) return
        viewModelScope.launch {
            _isAILoading.value = true
            _lastAIResponse.value = ""

            // Save user message in chat table
            val userMsg = ChatMessage(sender = "user", text = query, language = userProfile.value.selectedLanguage, type = "text")
            repository.insertChatMessage(userMsg)

            // Query Alagza Brain
            val answer = repository.askTutor(
                query = query,
                subject = _solverSubject.value,
                isExpert = _isExpertMode.value,
                language = userProfile.value.selectedLanguage
            )

            // Save AI reply in chat table
            val aiMsg = ChatMessage(sender = "ai", text = answer, language = userProfile.value.selectedLanguage, type = "text")
            repository.insertChatMessage(aiMsg)

            _lastAIResponse.value = answer
            _isAILoading.value = false

            // Auto read aloud if voice option or TTS is enabled
            if (_isTtsEnabled.value) {
                speakText(answer)
            }
        }
    }

    fun clearTutorChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // --- Voice Assistant Custom Mock States ---
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    fun triggerVoiceListening() {
        _isListening.value = true
        viewModelScope.launch {
            delay(2500) // Simulate voice acquisition delay
            _isListening.value = false
            // Send a typical spoken query based on subject
            val spokenQuery = when(_solverSubject.value) {
                "Mathematics" -> "Explain the solution step of Euler's formula e^(i*pi) + 1 = 0"
                "Physics" -> "What are Kepler's laws of planetary motion?"
                "Chemistry" -> "Explain the bond formation in a water molecule with diagrams"
                "Biology" -> "What is the function of Mitochondria in animal cells?"
                "Accountancy" -> "Define Double Entry system of accounting with example transaction"
                "Economics" -> "Describe the demand-supply curve equilibrium point"
                else -> "Give me a quick mock quiz question for board exam revision"
            }
            askAlagzaDoubt(spokenQuery)
            repository.logActivity("Voice", "AI Voice detected spoken doubt: \"$spokenQuery\"")
        }
    }

    // --- Smart Textbook Scanner Simulator ---
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanSolution = MutableStateFlow("")
    val scanSolution: StateFlow<String> = _scanSolution.asStateFlow()

    private val _scanSubject = MutableStateFlow("Physics")
    val scanSubject: StateFlow<String> = _scanSubject.asStateFlow()

    private val _scannedImageUrl = MutableStateFlow("")
    val scannedImageUrl: StateFlow<String> = _scannedImageUrl.asStateFlow()

    fun setScanSubject(subject: String) {
        _scanSubject.value = subject
    }

    fun simulateCameraScan(preloadedQuestion: String) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanSolution.value = ""
            delay(3000) // Simulate scanning glow line and camera processing
            _isScanning.value = false

            // Ask Alagza Brain for OCR mock solver
            val prompt = "Translate OCR and solve this textbook problem: '$preloadedQuestion'"
            val result = repository.askTutor(
                query = prompt,
                subject = _scanSubject.value,
                isExpert = true,
                language = userProfile.value.selectedLanguage
            )

            // Store inside chat/notes or profile
            val aiMsgResult = ChatMessage(sender = "ai", text = "📸 **Textbook Scan Result**\nScanned Problem: '$preloadedQuestion'\n\n$result", language = userProfile.value.selectedLanguage, type = "scanner")
            repository.insertChatMessage(aiMsgResult)

            _scanSolution.value = result
            repository.logActivity("Scan", "Scanned textbook page. Problem detected: '$preloadedQuestion'")
            repository.incrementUserPoints(20)
        }
    }

    // --- MCQ / Quiz Mode State Management ---
    data class MCQQuestion(
        val question: String,
        val options: List<String>,
        val answerIndex: Int,
        val explanation: String
    )

    private val _quizzes = MutableStateFlow<List<MCQQuestion>>(emptyList())
    val quizzes: StateFlow<List<MCQQuestion>> = _quizzes.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizSelectedAnswerIndex = MutableStateFlow(-1)
    val quizSelectedAnswerIndex: StateFlow<Int> = _quizSelectedAnswerIndex.asStateFlow()

    private val _isQuizAnswered = MutableStateFlow(false)
    val isQuizAnswered: StateFlow<Boolean> = _isQuizAnswered.asStateFlow()

    private val _quizSubject = MutableStateFlow("Physics")
    val quizSubject: StateFlow<String> = _quizSubject.asStateFlow()

    private val _quizTimerSeconds = MutableStateFlow(20)
    val quizTimerSeconds: StateFlow<Int> = _quizTimerSeconds.asStateFlow()

    private val _isQuizFinished = MutableStateFlow(false)
    val isQuizFinished: StateFlow<Boolean> = _isQuizFinished.asStateFlow()

    private val _isQuizGenerating = MutableStateFlow(false)
    val isQuizGenerating: StateFlow<Boolean> = _isQuizGenerating.asStateFlow()

    private var timerJob: Job? = null

    fun selectQuizSubject(subject: String) {
        _quizSubject.value = subject
    }

    fun startSubjectQuiz() {
        viewModelScope.launch {
            _isQuizGenerating.value = true
            _isQuizFinished.value = false
            _quizScore.value = 0
            _currentQuizIndex.value = 0
            _quizSelectedAnswerIndex.value = -1
            _isQuizAnswered.value = false

            // Try to generate via Gemini, fallback to premium offline quizzes compiled locally
            val list = fetchMCQsForSubject(_quizSubject.value, userProfile.value.selectedLanguage)
            _quizzes.value = list
            _isQuizGenerating.value = false

            startTimer()
            repository.logActivity("Quiz", "Started interactive Quiz in ${_quizSubject.value}")
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _quizTimerSeconds.value = 20
        timerJob = viewModelScope.launch {
            while (_quizTimerSeconds.value > 0 && !_isQuizFinished.value) {
                delay(1000)
                _quizTimerSeconds.value -= 1
            }
            if (_quizTimerSeconds.value == 0 && !_isQuizAnswered.value) {
                submitQuizAnswer(-1) // timeout
            }
        }
    }

    fun submitQuizAnswer(selectedIndex: Int) {
        if (_isQuizAnswered.value) return
        timerJob?.cancel()
        _quizSelectedAnswerIndex.value = selectedIndex
        _isQuizAnswered.value = true

        val currentQ = _quizzes.value.getOrNull(_currentQuizIndex.value)
        if (currentQ != null && selectedIndex == currentQ.answerIndex) {
            _quizScore.value += 1
        }
    }

    fun nextQuizQuestion() {
        val nextIdx = _currentQuizIndex.value + 1
        if (nextIdx < _quizzes.value.size) {
            _currentQuizIndex.value = nextIdx
            _quizSelectedAnswerIndex.value = -1
            _isQuizAnswered.value = false
            startTimer()
        } else {
            finishQuizGame()
        }
    }

    private fun finishQuizGame() {
        _isQuizFinished.value = true
        timerJob?.cancel()
        viewModelScope.launch {
            repository.saveQuizResult(
                subject = _quizSubject.value,
                score = _quizScore.value,
                totalQuestions = _quizzes.value.size
            )
        }
    }

    private suspend fun fetchMCQsForSubject(subject: String, lang: String): List<MCQQuestion> {
        // Mock default questions for rich gameplay
        val localizedQuiz = getMockQuizDatabase(subject, lang)
        return try {
            val jsonRaw = repository.generateFlashQuiz(subject, 4, lang)
            // Parse JSON if possible
            val parsedList = mutableListOf<MCQQuestion>()
            val cleanedJson = jsonRaw.trim().trim('`').removePrefix("json").trim()
            val array = JSONArray(cleanedJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val qText = obj.getString("question")
                val optArr = obj.getJSONArray("options")
                val options = List(optArr.length()) { optArr.getString(it) }
                val ans = obj.getInt("answerIndex")
                val explanation = obj.optString("explanation", "Excellent solution!")
                parsedList.add(MCQQuestion(qText, options, ans, explanation))
            }
            if (parsedList.isNotEmpty()) parsedList else localizedQuiz
        } catch (e: Exception) {
            // fallback to structured local quiz if offline or json parse fail
            localizedQuiz
        }
    }

    private fun getMockQuizDatabase(subject: String, lang: String): List<MCQQuestion> {
        val q1: MCQQuestion
        val q2: MCQQuestion
        val q3: MCQQuestion

        if (lang == "Hindi") {
            when (subject) {
                "Mathematics" -> {
                    q1 = MCQQuestion("समाकलन (Integration) e^x dx का मान क्या होता है?", listOf("e^x + C", "xe^x", "e^x", "log(x)"), 0, "समाकलन e^x dx = e^x + C है, क्योंकि e^x का अवकलन भी e^x होता है।")
                    q2 = MCQQuestion("वृत्त का क्षेत्रफल (Area of Circle) सूत्र है:", listOf("2pi*r", "pi * r^2", "pi * d", "4/3 pi*r^3"), 1, "वृत्त का कुल क्षेत्रफल त्रिज्या r के वर्ग और pi का गुणनफल होता है।")
                    q3 = MCQQuestion("यदि f(x) = x^2, तो अवकलन f'(x) क्या होगा?", listOf("x", "2x", "x^2/2", "2x^2"), 1, "d/dx(x^n) = n*x^(n-1) नियम का उपयोग कर 2x आता है।")
                }
                "Physics" -> {
                    q1 = MCQQuestion("पलायन वेग (Escape Velocity) पृथ्वी की सतह पर कितना होता है?", listOf("9.8 km/s", "11.2 km/s", "8.0 km/s", "15.0 km/s"), 1, "पृथ्वी की गुरुत्वाकर्षण सीमा को पर करने के लिए आवश्यक वेग 11.2 किमी प्रति सेकंड है।")
                    q2 = MCQQuestion("प्रकाश की चाल अधिकतम किसमें होती है?", listOf("हवा", "पानी", "निर्वात (Vacuum)", "कांच"), 2, "निर्वात में प्रकाश की गति उच्चतम (3 x 10^8 m/s) होती है।")
                    q3 = MCQQuestion("बल (Force) की SI इकाई क्या है?", listOf("जूल", "वॉट", "न्यूटन", "पास्कल"), 2, "बल की SI इकाई आइजक न्यूटन के नाम पर न्यूटन (N) है।")
                }
                else -> {
                    q1 = MCQQuestion("संविधान सभा की पहली बैठक कब हुई थी?", listOf("1946", "1950", "1947", "1935"), 0, "संविधान सभा की ऐतिहासिक पहली बैठक 9 दिसंबर 1946 को हुई थी।")
                    q2 = MCQQuestion("सकल घरेलू उत्पाद (GDP) क्या है?", listOf("कुल राष्ट्रीय कर", "कुल घरेलू उत्पादन मूल्य", "वित्तीय घाटा", "निर्यात मूल्य"), 1, "किसी देश की भौगोलिक सीमा के भीतर उत्पादित अंतिम वस्तुओं और सेवाओं का कुल बाजार मूल्य जीडीपी है।")
                    q3 = MCQQuestion("कोशिका का 'पावरहाउस' किसे कहा जाता है?", listOf("राइबोसोम", "लाइसोसोम", "माइटोकॉन्ड्रिया", "गॉल्जीकाय"), 2, "माइटोकॉन्ड्रिया एटीपी के रूप में सेलुलर ऊर्जा उत्पन्न करने में मुख्य है।")
                }
            }
        } else if (lang == "Kannada") {
            when (subject) {
                "Mathematics" -> {
                    q1 = MCQQuestion("ವೃತ್ತದ ಸುತ್ತಳತೆಯ ಸೂತ್ರ ಯಾವುದು?", listOf("pi * r^2", "2 * pi * r", "3 * pi * r", "pi * d^2"), 1, "ವೃತ್ತದ ಒಟ್ಟು ಸುತ್ತಳತೆ 2*pi*r ಆಗಿರುತ್ತದೆ.")
                    q2 = MCQQuestion("ಚಿಕ್ಕ ಅವಿಭಾಜ್ಯ ಸಂಖ್ಯೆ (Smallest Prime number) ಯಾವುದು?", listOf("1", "2", "3", "5"), 1, "2 ಮಾತ್ರ ಚಿಕ್ಕದಾದ ಮತ್ತು ಏಕೈಕ ಸರಿ ಅವಿಭಾಜ್ಯ ಸಂಖ್ಯೆಯಾಗಿದೆ.")
                    q3 = MCQQuestion("ax^2 + bx + c = 0 ಸಮೀಕರಣದಲ್ಲಿ ಶೋಧಕ (Discriminant) ಯಾವುದು?", listOf("b^2 - 4ac", "b^2 + 4ac", "4ac - b^2", "a^2 - 4bc"), 0, "D = b^2 - 4ac ಆಗಿದೆ.")
                }
                "Physics" -> {
                    q1 = MCQQuestion("ಗುರುತ್ವಾಕರ್ಷಣದ ಸ್ಥಿರಾಂಕ g ಯ ಮೌಲ್ಯವೆಷ್ಟು?", listOf("9.8 m/s^2", "11.2 m/s^2", "8.9 m/s^2", "1.6 m/s^2"), 0, "ಭೂಮಿಯ ಮೇಲಿನ ಸರಾಸರಿ ಗುರುತ್ವ ವೇಗವರ್ಧನೆ g = 9.8 m/s^2.")
                    q2 = MCQQuestion("ಶಕ್ತಿಯ ಅತಿ ದೊಡ್ಡ ನೈಸರ್ಗಿಕ ಆಕರ ಯಾವುದು?", listOf("ಕಲ್ಲಿದ್ದಲು", "ವಾತಾವರಣ", "ಸೂರ್ಯ", "ಗಾಳಿ"), 2, "ಸೂರ್ಯನು ಸಮಸ್ತ ಜೀವಿಗಳಿಗೆ ಶಕ್ತಿಯ ಅತಿ ದೊಡ್ಡ ಆಕರ.")
                    q3 = MCQQuestion("ಒತ್ತಡದ SI ಮಾನ ಯಾವುದು?", listOf("ನ್ಯೂಟನ್", "ವಾಟ್", "ಪಾಸ್ಕಲ್", "ಜೂಲ್"), 2, "ಒತ್ತಡದ ಮಾನ ಪಾಸ್ಕಲ್ (Pa) ಆಗಿದೆ.")
                }
                else -> {
                    q1 = MCQQuestion("ಮಾನವ ದೇಹದಲ್ಲಿರುವ ಒಟ್ಟು ಮೂಳೆಗಳ ಸಂಖ್ಯೆ ಎಷ್ಟು?", listOf("206", "300", "208", "196"), 0, "ವಯಸ್ಕ ಮಾನವನ ದೇಹದಲ್ಲಿ ಒಟ್ಟು 206 ಮೂಳೆಗಳಿರುತ್ತವೆ.")
                    q2 = MCQQuestion("ಭಾರತದ ಮೊದಲ ಉಪಗ್ರಹ ಯಾವುದು?", listOf("ಭಾಸ್ಕರ", "ಆರ್ಯಭಟ", "ರೋಹಿಣಿ", "ಇನ್ಸಾಟ್"), 1, "ಭಾರತದ ಪ್ರಥಮ ಕೃತಕ ಉಪಗ್ರಹ ಆರ್ಯಭಟ (1975).")
                    q3 = MCQQuestion("ಕನ್ನಡದ ಮೊದಲ ಕಾವ್ಯ ಯಾವುದು?", listOf("ಕವಿರಾಜಮಾರ್ಗ", "ವಡ್ಡಾರಾಧನೆ", "ಪಂಪಭಾರತ", "ಕರ್ಣಾಟ ಕಾದಂಬರಿ"), 0, "ಕವಿರಾಜಮಾರ್ಗವು ಪತ್ತೆಯಾದ ಕನ್ನಡದ ಅತ್ಯಂತ ಹಳೆಯ ಕಾವ್ಯ ಗ್ರಂಥವಾಗಿದೆ.")
                }
            }
        } else {
            // English
            when (subject) {
                "Mathematics" -> {
                    q1 = MCQQuestion("What is the derivative of sin(x) with respect to x?", listOf("cos(x)", "-cos(x)", "sin(x)", "tan(x)"), 0, "The standard derivative of sin(x) is cos(x).")
                    q2 = MCQQuestion("What is the sum of angles in a triangle?", listOf("90 degrees", "180 degrees", "270 degrees", "360 degrees"), 1, "The sum of interior angles in any Euclidean triangle is always 180°.")
                    q3 = MCQQuestion("Solve for x: 3x - 7 = 11", listOf("4", "5", "6", "8"), 2, "3x = 11 + 7 => 3x = 18 => x = 6.")
                }
                "Physics" -> {
                    q1 = MCQQuestion("What is the resistance of an ideal ammeter?", listOf("Zero", "Infinite", "100 Ohms", "Varies dynamically"), 0, "An ideal ammeter must have zero internal resistance to avoid affecting the circuit current.")
                    q2 = MCQQuestion("Which spectrum of light bends the most through a prism?", listOf("Red light", "Yellow light", "Green light", "Violet light"), 3, "Violet light has the shortest wavelength and highest frequency, refracting the most.")
                    q3 = MCQQuestion("The work-energy theorem states that work is equal to change in:", listOf("Potential Energy", "Kinetic Energy", "Linear Momentum", "Total Acceleration"), 1, "Net work done on an object equals its change in kinetic energy.")
                }
                "Chemistry" -> {
                    q1 = MCQQuestion("Which of these represents an ideal gas equation?", listOf("PV = nRT", "P = V/T", "VT = nP", "PV^2 = R"), 0, "PV = nRT is the equation of state for an ideal gas.")
                    q2 = MCQQuestion("What is the chemical formula of common baking soda?", listOf("NaHCO3", "Na2CO3", "NaOH", "NaCl"), 0, "Sodium bicarbonate (NaHCO3) is commonly known as baking soda.")
                    q3 = MCQQuestion("Which bond involves sharing of electron pairs between atoms?", listOf("Ionic Bond", "Covalent Bond", "Hydrogen Bond", "Metallic Bond"), 1, "Covalent bonds are formed by the mutual sharing of valence electrons.")
                }
                "Biology" -> {
                    q1 = MCQQuestion("Which component of blood is responsible for clotting?", listOf("Red Blood Cells", "White Blood Cells", "Platelets", "Plasma"), 2, "Platelets aggregate at injury sites to trigger clotting pathways.")
                    q2 = MCQQuestion("What is the primary site of photosynthesis in plants?", listOf("Roots", "Chloroplasts in stem/leaves", "Mitochondria", "Nucleus"), 1, "Chloroplasts contain chlorophyll pigments which capture light for food production.")
                    q3 = MCQQuestion("How many chambers are there in a human heart?", listOf("Two", "Three", "Four", "Five"), 2, "The human heart possesses 4 distinct chambers: two atria and two ventricles.")
                }
                else -> {
                    q1 = MCQQuestion("Who is known as the father of modern macroeconomic theory?", listOf("Adam Smith", "John Maynard Keynes", "Milton Friedman", "Karl Marx"), 1, "John Maynard Keynes revolutionized macroeconomics during the Great Depression.")
                    q2 = MCQQuestion("In accountancy, assets are defined as:", listOf("What a business owes", "What a business owns", "Total yearly revenues", "Sellers liabilities"), 1, "Assets represent resources owned by a business entity that yield future economic value.")
                    q3 = MCQQuestion("Which Indian national exam opens admissions to IITs?", listOf("JEE Advanced", "NEET", "UPSC IAS", "CAT"), 0, "JEE Advanced is the premier exam for secure entry into IIT engineering portals.")
                }
            }
        }
        return listOf(q1, q2, q3)
    }

    // --- AI Smart Study Notes Section ---
    private val _notesInputChapter = MutableStateFlow("")
    val notesInputChapter: StateFlow<String> = _notesInputChapter.asStateFlow()

    private val _notesInputSubject = MutableStateFlow("Mathematics")
    val notesInputSubject: StateFlow<String> = _notesInputSubject.asStateFlow()

    private val _isGeneratingNote = MutableStateFlow(false)
    val isGeneratingNote: StateFlow<Boolean> = _isGeneratingNote.asStateFlow()

    fun setNotesInputChapter(chapter: String) {
        _notesInputChapter.value = chapter
    }

    fun setNotesInputSubject(subject: String) {
        _notesInputSubject.value = subject
    }

    fun generateChapterNotesAndSave() {
        if (_notesInputChapter.value.trim().isEmpty()) return
        viewModelScope.launch {
            _isGeneratingNote.value = true
            val generatedContent = repository.generateShortNotes(
                chapterTitle = _notesInputChapter.value,
                subject = _notesInputSubject.value,
                language = userProfile.value.selectedLanguage
            )
            repository.saveNote(
                title = _notesInputChapter.value,
                content = generatedContent,
                subject = _notesInputSubject.value
            )
            _isGeneratingNote.value = false
            _notesInputChapter.value = ""
        }
    }

    fun removeNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    // --- Onboarding / Student Register Setup ---
    fun registerStudentProfile(name: String, studentType: String) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(
                name = name,
                studentType = studentType
            )
            repository.updateUserProfile(updated)
            repository.logActivity("Login", "Registered student '$name' as a $studentType!")
            repository.incrementDailyStreak()
            navigateTo("home")
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(selectedLanguage = lang)
            repository.updateUserProfile(updated)
            repository.logActivity("Setting", "Switched primary language to: $lang")
        }
    }

    fun updateTutorTone(tone: String) {
        viewModelScope.launch {
            val updated = userProfile.value.copy(tutorTone = tone)
            repository.updateUserProfile(updated)
            repository.logActivity("Setting", "Updated tutor persona to: $tone")
        }
    }

    fun toggleAppTheme() {
        viewModelScope.launch {
            val updated = userProfile.value.copy(isDarkMode = !userProfile.value.isDarkMode)
            repository.updateUserProfile(updated)
            repository.logActivity("Setting", "Toggled system visual theme")
        }
    }

    // --- Text-To-Speech (Speech assistant) ---
    private var textToSpeech: TextToSpeech? = null
    private val _isTtsEnabled = MutableStateFlow(false)
    val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

    init {
        try {
            textToSpeech = TextToSpeech(application, this)
        } catch (e: Exception) {
            e.printStackTrace()
            _isTtsEnabled.value = false
        }
        viewModelScope.launch {
            repository.ensureProfileExists()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            _isTtsEnabled.value = true
            textToSpeech?.language = Locale.ENGLISH
        }
    }

    fun speakText(text: String) {
        if (!_isTtsEnabled.value) return
        viewModelScope.launch(Dispatchers.Main) {
            val cleaned = text
                .replace(Regex("[*#_`📊📸✨]"), "") // clear markdown formatting before speech
                .take(300) // cap to reasonable speech segment
            textToSpeech?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, "AlagzaSpeak")
        }
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
    }

    override fun onCleared() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onCleared()
    }
}

// --- Factory for providing ViewModel ---

class AlagzaViewModelFactory(
    private val application: Application,
    private val repository: AlagzaRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlagzaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AlagzaViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
