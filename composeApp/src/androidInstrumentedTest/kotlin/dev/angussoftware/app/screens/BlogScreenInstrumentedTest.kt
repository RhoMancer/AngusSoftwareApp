package dev.angussoftware.app.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.angussoftware.app.blog.BlogPost
import dev.angussoftware.app.ui.utils.LocalWindowAdaptiveInfoOverride
import dev.angussoftware.app.ui.utils.WindowAdaptiveInfo
import dev.angussoftware.app.ui.utils.WindowWidthSizeClass
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BlogScreenInstrumentedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val testPosts =
        listOf(
            BlogPost(
                id = "1",
                title = "First Blog Post",
                url = "https://example.com/post1",
                pubDate = "2025-01-01",
                summary = "Summary of first post",
                imageUrl = null,
                content = null,
            ),
            BlogPost(
                id = "2",
                title = "Second Blog Post",
                url = "https://example.com/post2",
                pubDate = "2025-01-02",
                summary = "Summary of second post",
                imageUrl = null,
                content = null,
            ),
            BlogPost(
                id = "3",
                title = "Third Blog Post",
                url = "https://example.com/post3",
                pubDate = "2025-01-03",
                summary = "Summary of third post",
                imageUrl = null,
                content = null,
            ),
        )

    /**
     * Verifies that blog post items have vertical margin between them.
     * The LazyColumn uses verticalArrangement = Arrangement.spacedBy(12.dp),
     * so there should be a 12dp gap between consecutive items.
     */
    @Test
    fun blogPostItems_haveVerticalMarginBetweenThem() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalWindowAdaptiveInfoOverride provides WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT),
            ) {
                BlogScreen(
                    initialPosts = testPosts,
                    initialIsLoading = false,
                )
            }
        }
        composeTestRule.waitForIdle()

        // Verify blog screen is displayed
        composeTestRule.onNodeWithTag(BLOG_SCREEN_TEST_TAG).assertIsDisplayed()

        // Verify first two blog post items exist and are displayed
        val firstItem = composeTestRule.onNodeWithTag("${BLOG_POST_ITEM_TEST_TAG}_1")
        val secondItem = composeTestRule.onNodeWithTag("${BLOG_POST_ITEM_TEST_TAG}_2")

        firstItem.assertIsDisplayed()
        secondItem.assertIsDisplayed()

        // Get bounds of both items
        val firstBounds = firstItem.getBoundsInRoot()
        val secondBounds = secondItem.getBoundsInRoot()

        // Calculate the gap between first item's bottom and second item's top
        val gap = secondBounds.top - firstBounds.bottom

        // The expected gap is 12.dp (from verticalArrangement = Arrangement.spacedBy(12.dp))
        val expectedGap = 12.dp

        // Allow a small tolerance for rounding (1dp)
        val tolerance = 1.dp

        assert(gap >= expectedGap - tolerance && gap <= expectedGap + tolerance) {
            "Expected vertical gap of $expectedGap between blog post items, but got $gap"
        }
    }

    /**
     * Verifies that multiple blog post items are rendered when posts are provided.
     */
    @Test
    fun blogScreen_displaysMultipleBlogPostItems() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalWindowAdaptiveInfoOverride provides WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT),
            ) {
                BlogScreen(
                    initialPosts = testPosts,
                    initialIsLoading = false,
                )
            }
        }
        composeTestRule.waitForIdle()

        // Verify all three blog post items are displayed
        composeTestRule.onNodeWithTag("${BLOG_POST_ITEM_TEST_TAG}_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("${BLOG_POST_ITEM_TEST_TAG}_2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("${BLOG_POST_ITEM_TEST_TAG}_3").assertIsDisplayed()
    }

    /**
     * Verifies that vertical margin is consistent between all consecutive blog post items.
     */
    @Test
    fun blogPostItems_haveConsistentVerticalMargin() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalWindowAdaptiveInfoOverride provides WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT),
            ) {
                BlogScreen(
                    initialPosts = testPosts,
                    initialIsLoading = false,
                )
            }
        }
        composeTestRule.waitForIdle()

        // Get bounds of all three items
        val firstBounds = composeTestRule.onNodeWithTag("${BLOG_POST_ITEM_TEST_TAG}_1").getBoundsInRoot()
        val secondBounds = composeTestRule.onNodeWithTag("${BLOG_POST_ITEM_TEST_TAG}_2").getBoundsInRoot()
        val thirdBounds = composeTestRule.onNodeWithTag("${BLOG_POST_ITEM_TEST_TAG}_3").getBoundsInRoot()

        // Calculate gaps
        val gap1 = secondBounds.top - firstBounds.bottom
        val gap2 = thirdBounds.top - secondBounds.bottom

        // The expected gap is 12.dp
        val expectedGap = 12.dp
        val tolerance = 1.dp

        // Verify first gap
        assert(gap1 >= expectedGap - tolerance && gap1 <= expectedGap + tolerance) {
            "Expected vertical gap of $expectedGap between items 0 and 1, but got $gap1"
        }

        // Verify second gap
        assert(gap2 >= expectedGap - tolerance && gap2 <= expectedGap + tolerance) {
            "Expected vertical gap of $expectedGap between items 1 and 2, but got $gap2"
        }

        // Verify gaps are consistent with each other (within tolerance)
        val gapDifference = if (gap1 > gap2) gap1 - gap2 else gap2 - gap1
        assert(gapDifference <= tolerance) {
            "Vertical gaps should be consistent. Gap1: $gap1, Gap2: $gap2"
        }
    }

    /**
     * Verifies that clicking on different blog post items navigates to the correct index.
     * This test ensures that each item's click handler passes the correct index to navigation,
     * fixing the bug where all items were navigating to index 0.
     */
    @Test
    fun blogPostItems_navigateToCorrectIndex() {
        var navigatedRoute: String? = null

        composeTestRule.setContent {
            val navController = rememberNavController()

            // Listen to navigation changes
            navController.addOnDestinationChangedListener { _, destination, arguments ->
                navigatedRoute = destination.route
                // Also capture the actual argument if present
                arguments?.getString("postId")?.let { id ->
                    navigatedRoute = "${Screen.BlogPost.name}/$id"
                }
            }

            CompositionLocalProvider(
                LocalWindowAdaptiveInfoOverride provides WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT),
            ) {
                // Wrap in a NavHost so that navController.navigate() has a valid graph
                NavHost(navController = navController, startDestination = Screen.Blog.name) {
                    composable(Screen.Blog.name) {
                        BlogScreen(
                            navController = navController,
                            initialPosts = testPosts,
                            initialIsLoading = false,
                        )
                    }
                    composable(
                        route = "${Screen.BlogPost.name}/{postId}",
                        arguments = listOf(navArgument("postId") { type = NavType.StringType }),
                    ) { }
                }
            }
        }
        composeTestRule.waitForIdle()

        // Click on the first blog post item (id "1")
        composeTestRule.onNodeWithTag("${BLOG_POST_ITEM_TEST_TAG}_1").performClick()
        composeTestRule.waitForIdle()

        // Verify the navigation route contains the correct post ID
        assertEquals(
            "Clicking first item should navigate to BlogPost/1",
            "${Screen.BlogPost.name}/1",
            navigatedRoute,
        )

        // Reset for next test
        navigatedRoute = null

        // Go back and click on second item
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()

        // Click on the second blog post item (id "2")
        composeTestRule.onNodeWithTag("${BLOG_POST_ITEM_TEST_TAG}_2").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "Clicking second item should navigate to BlogPost/2",
            "${Screen.BlogPost.name}/2",
            navigatedRoute,
        )

        // Reset for next test
        navigatedRoute = null

        // Go back and click on third item
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeTestRule.waitForIdle()

        // Click on the third blog post item (id "3")
        composeTestRule.onNodeWithTag("${BLOG_POST_ITEM_TEST_TAG}_3").performClick()
        composeTestRule.waitForIdle()

        assertEquals(
            "Clicking third item should navigate to BlogPost/3",
            "${Screen.BlogPost.name}/3",
            navigatedRoute,
        )
    }

    // === Tests for missed lines: empty state, image placeholder, load more ===

    @Test
    fun blogScreen_emptyState_showsEmptyMessage() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalWindowAdaptiveInfoOverride provides WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT),
            ) {
                BlogScreen(
                    initialPosts = emptyList(),
                    initialIsLoading = false,
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("There are no new Posts to display.").assertExists()
    }

    @Test
    fun blogScreen_postWithImageUrl_showsImagePlaceholder() {
        val postsWithImage = listOf(
            BlogPost(
                id = "img1",
                title = "Post With Image",
                url = "https://example.com/post",
                pubDate = "2025-01-01",
                summary = "Summary",
                imageUrl = "https://example.com/image.jpg",
                content = null,
            ),
        )
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalWindowAdaptiveInfoOverride provides WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT),
            ) {
                BlogScreen(
                    initialPosts = postsWithImage,
                    initialIsLoading = false,
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("BlogList").performScrollToNode(hasText("Image placeholder"))
        composeTestRule.onNodeWithText("Image placeholder").assertExists()
    }

    @Test
    fun blogScreen_fewPosts_noLoadMoreButton() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalWindowAdaptiveInfoOverride provides WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT),
            ) {
                BlogScreen(
                    initialPosts = testPosts,
                    initialIsLoading = false,
                )
            }
        }
        composeTestRule.waitForIdle()
        // With only 2 test posts, "Load more" should not appear
        composeTestRule.onNodeWithText("Load more posts").assertDoesNotExist()
    }

    @Test
    fun blogScreen_postWithImage_scrollToShowImagePlaceholder() {
        val postsWithImage = listOf(
            BlogPost(
                id = "img1",
                title = "Post With Image",
                url = "https://example.com/post",
                pubDate = "2025-01-01",
                summary = "Summary",
                imageUrl = "https://example.com/image.jpg",
                content = null,
            ),
        )
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalWindowAdaptiveInfoOverride provides WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT),
            ) {
                BlogScreen(
                    initialPosts = postsWithImage,
                    initialIsLoading = false,
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("BlogList").performScrollToNode(hasText("Image placeholder"))
        composeTestRule.onNodeWithText("Image placeholder").assertExists()
    }

    @Test
    fun blogScreen_multiplePostsWithImages_allReachable() {
        val postsWithImages = (1..3).map { i ->
            BlogPost(
                id = "img$i",
                title = "Image Post $i",
                url = "https://example.com/post$i",
                pubDate = "2025-01-0$i",
                summary = "Summary $i",
                imageUrl = "https://example.com/image$i.jpg",
                content = null,
            )
        }
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalWindowAdaptiveInfoOverride provides WindowAdaptiveInfo(WindowWidthSizeClass.COMPACT),
            ) {
                BlogScreen(
                    initialPosts = postsWithImages,
                    initialIsLoading = false,
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("BlogList").performScrollToNode(hasText("Image Post 2"))
        composeTestRule.onNodeWithText("Image Post 2").assertExists()
    }
}
