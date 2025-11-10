package com.example.musicfilemanager

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.example.musicfilemanager.ui.MusicDetailScreen
import org.junit.Rule
import org.junit.Test

class MusicDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun test_detail_screen_displays_correctly() {
        // 1. Set the content to the MusicDetailScreen
        composeTestRule.setContent {
            MusicDetailScreen()
        }

        // 2. Find a composable by the text it displays
        val fileNameLabel = composeTestRule.onNodeWithText("Tên File:")

        // 3. Assert that the node is displayed
        fileNameLabel.assertIsDisplayed()

        // You can add more assertions here for other elements
        composeTestRule.onNodeWithText("Nghệ sĩ:").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tải lên").assertIsDisplayed()
    }
}
