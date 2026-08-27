package com.milepicture.app.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.milepicture.app.ui.viewmodel.MainViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testHomeScreenLoads() {
        val viewModel = mockk<MainViewModel>(relaxed = true)
        
        // Mocking StateFlows to ensure they return valid flows for Compose to collect
        every { viewModel.searchQuery } returns MutableStateFlow("")
        every { viewModel.tags } returns MutableStateFlow(emptyList())
        every { viewModel.selectedTagId } returns MutableStateFlow("all")
        every { viewModel.images } returns MutableStateFlow(emptyList())
        every { viewModel.isLoading } returns MutableStateFlow(false)
        every { viewModel.errorMessage } returns MutableStateFlow(null)
        every { viewModel.favorites } returns MutableStateFlow(emptyList())

        composeTestRule.setContent {
            HomeScreen(
                viewModel = viewModel,
                onImageClick = {},
                onFilterClick = {}
            )
        }

        // Verify that the search bar placeholder is displayed, indicating the screen has loaded
        composeTestRule.onNodeWithText("搜索 8 大图库高品质图片、馆藏与插画...", substring = true).assertIsDisplayed()
    }
}
