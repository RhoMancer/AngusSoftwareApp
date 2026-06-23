package dev.angussoftware.app.screens

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScreenEnumTest {

    @Test
    fun screenEnumHasExpectedValues() {
        val values = Screen.entries
        assertEquals(5, values.size)
        assertTrue(values.contains(Screen.Home))
        assertTrue(values.contains(Screen.Projects))
        assertTrue(values.contains(Screen.Blog))
        assertTrue(values.contains(Screen.Settings))
        assertTrue(values.contains(Screen.BlogPost))
    }

    @Test
    fun screenEnumValueOfWorks() {
        assertEquals(Screen.Home, Screen.valueOf("Home"))
        assertEquals(Screen.Projects, Screen.valueOf("Projects"))
        assertEquals(Screen.Blog, Screen.valueOf("Blog"))
        assertEquals(Screen.Settings, Screen.valueOf("Settings"))
        assertEquals(Screen.BlogPost, Screen.valueOf("BlogPost"))
    }

    @Test
    fun navBarTestTagIsCorrect() {
        assertEquals("BottomNavigationBar", NAV_BAR_TEST_TAG)
    }

    @Test
    fun navRailTestTagIsCorrect() {
        assertEquals("NavigationRail", NAV_RAIL_TEST_TAG)
    }

    @Test
    fun navItemTestTagsAreCorrect() {
        assertEquals("NavItem_Home", NAV_ITEM_HOME_TAG)
        assertEquals("NavItem_Projects", NAV_ITEM_PROJECTS_TAG)
        assertEquals("NavItem_Blog", NAV_ITEM_BLOG_TAG)
        assertEquals("NavItem_Settings", NAV_ITEM_SETTINGS_TAG)
    }

    @Test
    fun screenContentTestTagsAreCorrect() {
        assertEquals("HomeScreen", HOME_SCREEN_TEST_TAG)
        assertEquals("ProjectsScreen", PROJECTS_SCREEN_TEST_TAG)
        assertEquals("BlogScreen", BLOG_SCREEN_TEST_TAG)
        assertEquals("BlogPostItem", BLOG_POST_ITEM_TEST_TAG)
    }
}
