package com.ahmed.i221132

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileNavigationTest {

    // Launch HomeActivity where the profile_image exists
    @get:Rule
    val activityRule = ActivityScenarioRule(HomeActivity::class.java)

    @Test
    fun testProfileImageOpensProfile() {
        // Click on profile image
        onView(withId(R.id.profile_image)).perform(click())

        // Check if something from ProfileActivity appears
        onView(withId(R.id.bio_name))
            .check(matches(isDisplayed()))
    }
}
