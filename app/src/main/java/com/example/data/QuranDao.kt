package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {
    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber")
    suspend fun deleteBookmark(surahNumber: Int, ayahNumber: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber)")
    fun isBookmarkedFlow(surahNumber: Int, ayahNumber: Int): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber)")
    suspend fun isBookmarked(surahNumber: Int, ayahNumber: Int): Boolean

    // Reading History
    @Query("SELECT * FROM reading_history WHERE id = 1 LIMIT 1")
    fun getReadingHistory(): Flow<ReadingHistory?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveReadingHistory(history: ReadingHistory)

    // User Notes
    @Query("SELECT * FROM user_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<UserNote>>

    @Query("SELECT * FROM user_notes WHERE surahNumber = :surahNumber AND ayahNumber = :ayahNumber ORDER BY timestamp DESC")
    fun getNotesForAyah(surahNumber: Int, ayahNumber: Int): Flow<List<UserNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: UserNote)

    @Query("DELETE FROM user_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)

    // Tasbih
    @Query("SELECT * FROM tasbih_counts ORDER BY timestamp DESC")
    fun getAllTasbihCounts(): Flow<List<TasbihCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasbihCount(count: TasbihCount)

    @Query("DELETE FROM tasbih_counts WHERE id = :id")
    suspend fun deleteTasbihCount(id: Int)
}
