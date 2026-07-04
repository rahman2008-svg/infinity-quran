package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException

class QuranRepository(
    private val context: Context,
    private val quranDao: QuranDao
) {
    // In-memory cache of parsed Surahs
    private var cachedSurahs: List<Surah>? = null

    // Load and parse Quran JSON from assets
    suspend fun getSurahs(): List<Surah> = withContext(Dispatchers.IO) {
        cachedSurahs?.let { return@withContext it }

        try {
            val jsonString = context.assets.open("quran.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            val surahs = mutableListOf<Surah>()

            for (i in 0 until jsonArray.length()) {
                val surahObj = jsonArray.getJSONObject(i)
                val number = surahObj.getInt("number")
                val name = surahObj.getString("name")
                val englishName = surahObj.getString("englishName")
                val banglaName = surahObj.getString("banglaName")
                val revelationType = surahObj.getString("revelationType")
                val numberOfAyahs = surahObj.getInt("numberOfAyahs")

                val ayahsArray = surahObj.getJSONArray("ayahs")
                val ayahs = mutableListOf<Ayah>()
                for (j in 0 until ayahsArray.length()) {
                    val ayahObj = ayahsArray.getJSONObject(j)
                    val ayahNum = ayahObj.getInt("number")
                    val ayahText = ayahObj.getString("text")
                    val english = ayahObj.getString("english")
                    val bangla = ayahObj.getString("bangla")

                    ayahs.add(Ayah(ayahNum, ayahText, english, bangla))
                }

                surahs.add(
                    Surah(
                        number = number,
                        name = name,
                        englishName = englishName,
                        banglaName = banglaName,
                        revelationType = revelationType,
                        numberOfAyahs = numberOfAyahs,
                        ayahs = ayahs
                    )
                )
            }

            cachedSurahs = surahs
            surahs
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getSurah(number: Int): Surah? {
        return getSurahs().find { it.number == number }
    }

    // Searches Surah names and Ayah text across Arabic, Bangla, and English
    suspend fun searchQuran(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val lowercaseQuery = query.lowercase().trim()
        val surahs = getSurahs()
        val results = mutableListOf<SearchResult>()

        for (surah in surahs) {
            // Check if query matches Surah Name (English, Bangla, or Arabic)
            val matchesSurah = surah.englishName.lowercase().contains(lowercaseQuery) ||
                    surah.banglaName.contains(lowercaseQuery) ||
                    surah.name.contains(lowercaseQuery)

            if (matchesSurah) {
                // Return Surah level match (first ayah as preview)
                results.add(
                    SearchResult(
                        surah = surah,
                        ayah = surah.ayahs.firstOrNull(),
                        isSurahMatch = true
                    )
                )
            }

            // Check ayahs
            for (ayah in surah.ayahs) {
                val matchesAyah = ayah.text.contains(lowercaseQuery) ||
                        ayah.english.lowercase().contains(lowercaseQuery) ||
                        ayah.bangla.contains(lowercaseQuery) ||
                        ayah.number.toString() == lowercaseQuery

                if (matchesAyah) {
                    results.add(
                        SearchResult(
                            surah = surah,
                            ayah = ayah,
                            isSurahMatch = false
                        )
                    )
                }
            }
        }
        results
    }

    // Bookmarks database exposure
    val allBookmarks: Flow<List<Bookmark>> = quranDao.getAllBookmarks()

    suspend fun toggleBookmark(surahNumber: Int, ayahNumber: Int, surahName: String) {
        if (quranDao.isBookmarked(surahNumber, ayahNumber)) {
            quranDao.deleteBookmark(surahNumber, ayahNumber)
        } else {
            quranDao.insertBookmark(
                Bookmark(
                    surahNumber = surahNumber,
                    ayahNumber = ayahNumber,
                    surahName = surahName
                )
            )
        }
    }

    fun isBookmarkedFlow(surahNumber: Int, ayahNumber: Int): Flow<Boolean> {
        return quranDao.isBookmarkedFlow(surahNumber, ayahNumber)
    }

    // Last Read/Reading History Exposure
    val readingHistory: Flow<ReadingHistory?> = quranDao.getReadingHistory()

    suspend fun saveReadingHistory(surahNumber: Int, ayahNumber: Int, surahName: String) {
        quranDao.saveReadingHistory(
            ReadingHistory(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                surahName = surahName
            )
        )
    }

    // Notes Exposure
    val allNotes: Flow<List<UserNote>> = quranDao.getAllNotes()

    fun getNotesForAyah(surahNumber: Int, ayahNumber: Int): Flow<List<UserNote>> {
        return quranDao.getNotesForAyah(surahNumber, ayahNumber)
    }

    suspend fun addNote(surahNumber: Int, ayahNumber: Int, surahName: String, noteText: String) {
        quranDao.insertNote(
            UserNote(
                surahNumber = surahNumber,
                ayahNumber = ayahNumber,
                surahName = surahName,
                noteText = noteText
            )
        )
    }

    suspend fun deleteNote(id: Int) {
        quranDao.deleteNoteById(id)
    }

    // Tasbih Exposure
    val allTasbihCounts: Flow<List<TasbihCount>> = quranDao.getAllTasbihCounts()

    suspend fun saveTasbihCount(count: Int, targetName: String) {
        quranDao.insertTasbihCount(
            TasbihCount(count = count, targetName = targetName)
        )
    }

    suspend fun deleteTasbihCount(id: Int) {
        quranDao.deleteTasbihCount(id)
    }
}

data class SearchResult(
    val surah: Surah,
    val ayah: Ayah?,
    val isSurahMatch: Boolean
)
