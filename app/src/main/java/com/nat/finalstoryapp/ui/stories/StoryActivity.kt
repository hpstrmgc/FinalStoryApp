package com.nat.finalstoryapp.ui.stories

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.nat.finalstoryapp.data.di.Injection
import com.nat.finalstoryapp.databinding.ActivityStoryBinding
import com.nat.finalstoryapp.ui.authpage.LoginActivity
import com.nat.finalstoryapp.ui.maps.MapsActivity
import com.nat.finalstoryapp.ui.newstory.NewStoryActivity
import com.nat.finalstoryapp.utils.LoadingStateAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryBinding
    private val storyViewModel: StoryViewModel by viewModels {
        StoryViewModelFactory(Injection.provideRepository(this))
    }
    private lateinit var storyAdapter: StoryListAdapter

    private val newStoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            observeStories()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity() // close the app, not going to the previous activity
            }
        })

        if (!isUserLoggedIn()) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding.buttonCreate.setOnClickListener {
            val intent = Intent(this, NewStoryActivity::class.java)
            newStoryLauncher.launch(intent)
        }

        binding.actionLogout.setOnClickListener {
            logout()
        }

        binding.actionMap.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        setupRecyclerView()
        observeStories()
    }

    private fun isUserLoggedIn(): Boolean {
        val token = getTokenFromPreferences()
        return !token.isNullOrEmpty()
    }

    private fun getTokenFromPreferences(): String? {
        val sharedPreferences = getSharedPreferences("your_app_preferences", MODE_PRIVATE)
        return sharedPreferences.getString("token", null)
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryListAdapter()
        val concatAdapter = ConcatAdapter(storyAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter { storyAdapter.retry() }
        ))

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@StoryActivity)
            adapter = concatAdapter
        }

        storyAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading) {
                showProgressBar(true)
            } else {
                showProgressBar(false)
                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error
                errorState?.let {
                    Toast.makeText(this, it.error.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeStories() {
        val token = getTokenFromPreferences()
        if (token != null) {
            Log.d("StoryActivity", "Token: $token")
            lifecycleScope.launch {
                storyViewModel.getStories(token).collectLatest { pagingData ->
                    storyAdapter.submitData(pagingData)
                }
            }
        } else {
            Toast.makeText(this, "Token is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showProgressBar(show: Boolean) {
        binding.progressIndicator.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("your_app_preferences", MODE_PRIVATE)
        sharedPreferences.edit().remove("token").apply()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}