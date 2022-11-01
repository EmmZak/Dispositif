package com.app.app

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.app.db.AppRepository

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Ignore

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    var appRepo: AppRepository = AppRepository()

    @Test
    fun test() {
        appRepo.findApp()
    }

    @Ignore
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.app.app", appContext.packageName)
    }
}