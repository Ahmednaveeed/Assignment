package com.ahmed.i221132

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import com.ahmed.i221132.savedacc

@RunWith(AndroidJUnit4::class)
class LoginButtonTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(savedacc::class.java)

    @Test
    fun testLoginButtonText() {
        activityRule.scenario.onActivity { activity ->
            val loginBtn = activity.findViewById<android.widget.Button>(R.id.loginBtn)
            assertEquals("Log In", loginBtn.text.toString())
        }
    }
}
