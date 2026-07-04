package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.QuranDatabase
import com.example.data.QuranRepository
import com.example.ui.QuranAppUi
import com.example.viewmodel.QuranViewModel
import com.example.viewmodel.QuranViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Database, Dao, Repository, and ViewModel
        val database = QuranDatabase.getDatabase(applicationContext)
        val quranDao = database.quranDao()
        val repository = QuranRepository(applicationContext, quranDao)
        
        val viewModelFactory = QuranViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[QuranViewModel::class.java]

        setContent {
            QuranAppUi(viewModel = viewModel)
        }
    }
}
