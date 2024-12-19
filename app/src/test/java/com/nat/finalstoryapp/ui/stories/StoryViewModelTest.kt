package com.nat.finalstoryapp.ui.stories

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.nat.finalstoryapp.DataDummy
import com.nat.finalstoryapp.MainDispatcherRule
import com.nat.finalstoryapp.data.api.response.ListStoryItem
import com.nat.finalstoryapp.data.repository.StoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Test
    fun `when Get Stories Should Not Null and Return Data`() = runTest {
        val dummyStories = DataDummy.generateDummyStoryResponse()
        val data: PagingData<ListStoryItem> = PagingTestDataSources.snapshot(dummyStories)
        val expectedStories: Flow<PagingData<ListStoryItem>> = flowOf(data)
        Mockito.`when`(storyRepository.getStories("token")).thenReturn(expectedStories)

        val storyViewModel = StoryViewModel(storyRepository)
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryListAdapter.DIFF_CALLBACK,
            updateCallback = ListUpdateCallbackHelper(),
            workerDispatcher = Dispatchers.Main,
        )

        val job = launch {
            storyViewModel.getStories("token").collectLatest {
                differ.submitData(it)
            }
        }

        advanceUntilIdle()
        job.cancel()

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStories.size, differ.snapshot().size)
        Assert.assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Stories Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())
        val expectedStories: Flow<PagingData<ListStoryItem>> = flowOf(data)
        Mockito.`when`(storyRepository.getStories("token")).thenReturn(expectedStories)

        val storyViewModel = StoryViewModel(storyRepository)
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryListAdapter.DIFF_CALLBACK,
            updateCallback = ListUpdateCallbackHelper(),
            workerDispatcher = Dispatchers.Main,
        )

        val job = launch {
            storyViewModel.getStories("token").collectLatest {
                differ.submitData(it)
            }
        }

        advanceUntilIdle()
        job.cancel()

        Assert.assertEquals(0, differ.snapshot().size)
    }
}

object PagingTestDataSources {
    fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
        return PagingData.from(items)
    }
}

class ListUpdateCallbackHelper : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}