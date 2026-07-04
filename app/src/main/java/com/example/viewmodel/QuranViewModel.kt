package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class QuranViewModel(
    application: Application,
    private val repository: QuranRepository
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)

    // Onboarding & Language Settings
    private val _onboardingCompleted = MutableStateFlow(sharedPrefs.getBoolean("onboarding_completed", false))
    val onboardingCompleted = _onboardingCompleted.asStateFlow()

    private val _appLanguage = MutableStateFlow(sharedPrefs.getString("app_language", "bn") ?: "bn") // bn, en, ar
    val appLanguage = _appLanguage.asStateFlow()

    private val _translationOption = MutableStateFlow(sharedPrefs.getString("translation_option", "both") ?: "both") // ar_only, bn_only, en_only, both, ar_en, ar_bn
    val translationOption = _translationOption.asStateFlow()

    private val _fontSize = MutableStateFlow(sharedPrefs.getFloat("font_size", 18f))
    val fontSize = _fontSize.asStateFlow()

    private val _arabicFont = MutableStateFlow(sharedPrefs.getString("arabic_font", "uthmani") ?: "uthmani") // uthmani, indopak
    val arabicFont = _arabicFont.asStateFlow()

    private val _darkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", true))
    val darkMode = _darkMode.asStateFlow()

    // Quran Lists
    private val _surahs = MutableStateFlow<List<Surah>>(emptyList())
    val surahs = _surahs.asStateFlow()

    private val _loadingSurahs = MutableStateFlow(true)
    val loadingSurahs = _loadingSurahs.asStateFlow()

    // Active Surah Details
    private val _selectedSurah = MutableStateFlow<Surah?>(null)
    val selectedSurah = _selectedSurah.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _searching = MutableStateFlow(false)
    val searching = _searching.asStateFlow()

    // Bookmarks, Notes, and Reading History
    val bookmarks = repository.allBookmarks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val readingHistory = repository.readingHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allNotes = repository.allNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily Ayah & Dua
    val dailyAyah = flow {
        // Deterministic daily Ayah based on the day of year
        val surahsList = repository.getSurahs()
        if (surahsList.isNotEmpty()) {
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val selectedSurahIdx = dayOfYear % surahsList.size
            val surah = surahsList[selectedSurahIdx]
            val ayahIdx = dayOfYear % surah.ayahs.size
            emit(Pair(surah, surah.ayahs[ayahIdx]))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyDua = flow {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val idx = dayOfYear % StaticIslamicData.dailyDuas.size
        emit(StaticIslamicData.dailyDuas[idx])
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StaticIslamicData.dailyDuas[0])

    // Tasbih Counter State
    private val _tasbihCount = MutableStateFlow(0)
    val tasbihCount = _tasbihCount.asStateFlow()

    private val _tasbihTargetName = MutableStateFlow("SubhanAllah (সুবহানাল্লাহ)")
    val tasbihTargetName = _tasbihTargetName.asStateFlow()

    val tasbihHistory = repository.allTasbihCounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Prayer Times State
    private val _prayerTimes = MutableStateFlow<Map<String, String>>(emptyMap())
    val prayerTimes = _prayerTimes.asStateFlow()

    private val _nextPrayerName = MutableStateFlow("")
    val nextPrayerName = _nextPrayerName.asStateFlow()

    private val _nextPrayerCountdown = MutableStateFlow("")
    val nextPrayerCountdown = _nextPrayerCountdown.asStateFlow()

    // Qibla direction State
    private val _qiblaAngle = MutableStateFlow(0f)
    val qiblaAngle = _qiblaAngle.asStateFlow()

    // Audio Player State (Simulated)
    private val _currentPlayingAyah = MutableStateFlow<Int?>(null) // Ayah number currently playing
    val currentPlayingAyah = _currentPlayingAyah.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed = _playbackSpeed.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerMinutes = _sleepTimerMinutes.asStateFlow()

    // Hifz Mode State
    private val _hifzRepeatCount = MutableStateFlow(1) // 1 to 5, or loop
    val hifzRepeatCount = _hifzRepeatCount.asStateFlow()

    private val _hifzHideArabic = MutableStateFlow(false)
    val hifzHideArabic = _hifzHideArabic.asStateFlow()

    private val _hifzHideTranslation = MutableStateFlow(false)
    val hifzHideTranslation = _hifzHideTranslation.asStateFlow()

    // Progress Stats
    val studyStreak = flow {
        emit(sharedPrefs.getInt("study_streak", 3)) // default mock dynamic streak
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    val totalReadingMinutes = flow {
        emit(sharedPrefs.getInt("total_reading_minutes", 124)) // mock
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 124)

    val totalAyahsRead = flow {
        emit(sharedPrefs.getInt("total_ayahs_read", 485)) // mock
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 485)

    init {
        loadSurahs()
        startPrayerTimeCountdownTracker()
        simulateQiblaCompassRotation()
    }

    private fun loadSurahs() {
        viewModelScope.launch {
            _loadingSurahs.value = true
            val loaded = repository.getSurahs()
            _surahs.value = loaded
            _loadingSurahs.value = false
        }
    }

    fun selectSurah(number: Int) {
        viewModelScope.launch {
            _selectedSurah.value = repository.getSurah(number)
            // Save as reading history/last read when a surah is selected
            _selectedSurah.value?.let {
                repository.saveReadingHistory(it.number, 1, it.englishName)
            }
        }
    }

    fun selectAyahForLastRead(surahNumber: Int, ayahNumber: Int, surahName: String) {
        viewModelScope.launch {
            repository.saveReadingHistory(surahNumber, ayahNumber, surahName)
        }
    }

    // Onboarding Actions
    fun completeOnboarding() {
        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
        _onboardingCompleted.value = true
    }

    fun setAppLanguage(language: String) {
        sharedPrefs.edit().putString("app_language", language).apply()
        _appLanguage.value = language
    }

    fun setTranslationOption(option: String) {
        sharedPrefs.edit().putString("translation_option", option).apply()
        _translationOption.value = option
    }

    fun setFontSize(size: Float) {
        sharedPrefs.edit().putFloat("font_size", size).apply()
        _fontSize.value = size
    }

    fun setArabicFont(font: String) {
        sharedPrefs.edit().putString("arabic_font", font).apply()
        _arabicFont.value = font
    }

    fun toggleDarkMode() {
        val next = !_darkMode.value
        sharedPrefs.edit().putBoolean("dark_mode", next).apply()
        _darkMode.value = next
    }

    // Bookmark Actions
    fun toggleBookmark(surahNumber: Int, ayahNumber: Int, surahName: String) {
        viewModelScope.launch {
            repository.toggleBookmark(surahNumber, ayahNumber, surahName)
        }
    }

    // Note Actions
    fun addNote(surahNumber: Int, ayahNumber: Int, surahName: String, noteText: String) {
        viewModelScope.launch {
            repository.addNote(surahNumber, ayahNumber, surahName, noteText)
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    // Tasbih Counter Actions
    fun incrementTasbih() {
        _tasbihCount.value += 1
        triggerVibration()
    }

    fun resetTasbih() {
        _tasbihCount.value = 0
        triggerVibration()
    }

    fun saveTasbihProgress() {
        viewModelScope.launch {
            if (_tasbihCount.value > 0) {
                repository.saveTasbihCount(_tasbihCount.value, _tasbihTargetName.value)
                _tasbihCount.value = 0
            }
        }
    }

    fun changeTasbihPhrase(phrase: String) {
        _tasbihTargetName.value = phrase
        _tasbihCount.value = 0
    }

    fun deleteTasbihCount(id: Int) {
        viewModelScope.launch {
            repository.deleteTasbihCount(id)
        }
    }

    private fun triggerVibration() {
        val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(50)
            }
        }
    }

    // Hifz Mode Actions
    fun toggleHifzHideArabic() {
        _hifzHideArabic.value = !_hifzHideArabic.value
    }

    fun toggleHifzHideTranslation() {
        _hifzHideTranslation.value = !_hifzHideTranslation.value
    }

    fun setHifzRepeatCount(count: Int) {
        _hifzRepeatCount.value = count
    }

    // Audio Player Simulation
    fun toggleAudioPlayback(ayahNumber: Int? = null) {
        if (ayahNumber != null) {
            _currentPlayingAyah.value = ayahNumber
            _isPlaying.value = true
        } else {
            _isPlaying.value = !_isPlaying.value
            if (_isPlaying.value && _currentPlayingAyah.value == null) {
                _currentPlayingAyah.value = 1
            }
        }
    }

    fun playNextAyah() {
        val surah = _selectedSurah.value ?: return
        val current = _currentPlayingAyah.value ?: 1
        if (current < surah.numberOfAyahs) {
            _currentPlayingAyah.value = current + 1
            _isPlaying.value = true
        } else {
            _isPlaying.value = false
            _currentPlayingAyah.value = null
        }
    }

    fun playPrevAyah() {
        val current = _currentPlayingAyah.value ?: return
        if (current > 1) {
            _currentPlayingAyah.value = current - 1
            _isPlaying.value = true
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
    }

    fun setSleepTimer(minutes: Int?) {
        _sleepTimerMinutes.value = minutes
    }

    // Search Actions
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _searching.value = true
            _searchResults.value = repository.searchQuran(query)
            _searching.value = false
        }
    }

    // Qibla rotation simulator
    private fun simulateQiblaCompassRotation() {
        viewModelScope.launch(Dispatchers.Default) {
            var baseAngle = 135f // Makkah direction estimation for SE Asia
            while (true) {
                // Simulate minor hand tremor rotation variations for compass realism
                val noise = (-2..2).random().toFloat()
                _qiblaAngle.value = (baseAngle + noise + 360f) % 360f
                delay(300)
            }
        }
    }

    // Real-time dynamic prayer countdown
    private fun startPrayerTimeCountdownTracker() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val second = calendar.get(Calendar.SECOND)

                // Standard estimated timings
                val times = mapOf(
                    "Fajr" to "04:30",
                    "Dhuhr" to "12:15",
                    "Asr" to "15:45",
                    "Maghrib" to "18:50",
                    "Isha" to "20:15"
                )
                _prayerTimes.value = times

                // Decide next prayer
                val nowInMinutes = hour * 60 + minute
                val fajrMin = 4 * 60 + 30
                val dhuhrMin = 12 * 60 + 15
                val asrMin = 15 * 60 + 45
                val maghribMin = 18 * 60 + 50
                val ishaMin = 20 * 60 + 15

                val nextPrayer: String
                val targetMinutes: Int

                if (nowInMinutes < fajrMin) {
                    nextPrayer = "Fajr"
                    targetMinutes = fajrMin
                } else if (nowInMinutes < dhuhrMin) {
                    nextPrayer = "Dhuhr"
                    targetMinutes = dhuhrMin
                } else if (nowInMinutes < asrMin) {
                    nextPrayer = "Asr"
                    targetMinutes = asrMin
                } else if (nowInMinutes < maghribMin) {
                    nextPrayer = "Maghrib"
                    targetMinutes = maghribMin
                } else if (nowInMinutes < ishaMin) {
                    nextPrayer = "Isha"
                    targetMinutes = ishaMin
                } else {
                    nextPrayer = "Fajr"
                    targetMinutes = fajrMin + 24 * 60
                }

                _nextPrayerName.value = nextPrayer

                var diffSecs = (targetMinutes * 60) - (nowInMinutes * 60 + second)
                if (diffSecs < 0) {
                    diffSecs += 24 * 3600
                }

                val diffH = diffSecs / 3600
                val diffM = (diffSecs % 3600) / 60
                val diffS = diffSecs % 60

                _nextPrayerCountdown.value = String.format("%02d:%02d:%02d", diffH, diffM, diffS)
                delay(1000)
            }
        }
    }
}

class QuranViewModelFactory(
    private val application: Application,
    private val repository: QuranRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuranViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuranViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
