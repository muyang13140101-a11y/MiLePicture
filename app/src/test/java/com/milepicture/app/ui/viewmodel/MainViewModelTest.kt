package com.milepicture.app.ui.viewmodel

import android.app.Application
import com.milepicture.app.data.api.ApiClient
import com.milepicture.app.data.api.MiLePictureApiService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private val application = mockk<Application>(relaxed = true)
    private val apiService = mockk<MiLePictureApiService>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(ApiClient)
        every { ApiClient.getService() } returns apiService
        
        // FavoritesRepository also initialized in init, it uses application.getSharedPreferences
        // Since application is mocked with relaxed=true, it should be fine.

        viewModel = MainViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `test search query updates correctly`() {
        val testQuery = "nature"
        viewModel.onQueryChange(testQuery)
        assertEquals(testQuery, viewModel.searchQuery.value)
    }
}
