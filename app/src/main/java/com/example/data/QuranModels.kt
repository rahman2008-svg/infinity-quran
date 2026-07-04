package com.example.data

data class Surah(
    val number: Int,
    val name: String,
    val englishName: String,
    val banglaName: String,
    val revelationType: String,
    val numberOfAyahs: Int,
    val ayahs: List<Ayah>
) {
    // Utility getter to count verses
    val totalVerses: Int get() = ayahs.size
}

data class Ayah(
    val number: Int,
    val text: String,
    val english: String,
    val bangla: String
)

// Extra data structures for our other features
data class NamesOfAllah(
    val number: Int,
    val name: String,
    val banglaMeaning: String,
    val pronunciation: String,
    val explanation: String
)

data class Hadith(
    val id: Int,
    val narrator: String,
    val textBangla: String,
    val textEnglish: String,
    val source: String
)

data class DailyDua(
    val title: String,
    val arabic: String,
    val pronunciation: String,
    val meaningBangla: String,
    val meaningEnglish: String,
    val source: String
)
