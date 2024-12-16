package com.nat.finalstoryapp.ui.stories

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.nat.finalstoryapp.data.api.response.Story
import com.nat.finalstoryapp.data.di.Injection
import com.nat.finalstoryapp.databinding.ActivityStoryBinding
import com.nat.finalstoryapp.ui.authpage.LoginActivity
import com.nat.finalstoryapp.ui.newstory.NewStoryActivity

class StoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStoryBinding
    private val storyViewModel: StoryViewModel by viewModels {
        StoryViewModelFactory(Injection.provideRepository(this))
    }
    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isUserLoggedIn()) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding.buttonCreate.setOnClickListener {
            val intent = Intent(this, NewStoryActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_NEW_STORY)
        }

        binding.actionLogout.setOnClickListener {
            logout()
        }

        setupRecyclerView()
        observeStories()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_NEW_STORY && resultCode == RESULT_OK) {
            // Refresh stories when a new story is successfully uploaded
            observeStories()
        }
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
        storyAdapter = StoryAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@StoryActivity)
            adapter = storyAdapter
        }
    }

    private fun observeStories() {
        val token = getTokenFromPreferences()
        if (token != null) {
            Log.d("StoryActivity", "Token: $token")
            showProgressBar(true)
            storyViewModel.getStories(token).observe(this) { storyResponse ->
                showProgressBar(false)
                if (storyResponse != null) {
                    val stories = storyResponse.listStory.map { listStoryItem ->
                        Story(
                            id = listStoryItem.id,
                            title = listStoryItem.name ?: "",
                            description = listStoryItem.description ?: "",
                            photoUrl = listStoryItem.photoUrl ?: "",
                            createdAt = listStoryItem.createdAt ?: ""
                        )
                    }
                    storyAdapter.submitList(stories)
                } else {
                    Toast.makeText(this, "Failed to fetch stories", Toast.LENGTH_SHORT).show()
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

    override fun onBackPressed() {
        finishAffinity() // close the app, not going to the previous activity
    }

    companion object {
        private const val REQUEST_CODE_NEW_STORY = 1
    }
}