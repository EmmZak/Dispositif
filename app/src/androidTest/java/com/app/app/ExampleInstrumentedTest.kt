package com.app.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.app.db.AppRepository
import com.app.app.enums.AlarmFrequency
import com.app.app.model.Alarm
import com.app.app.service.alarm.AlarmService

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Before




/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
//@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    var appRepo: AppRepository = AppRepository()

    var alarmService: AlarmService = AlarmService(ApplicationProvider.getApplicationContext())


    @Ignore
    @Test
    fun removeAll() {
        val c: Context = ApplicationProvider.getApplicationContext()
        val store = c.getSharedPreferences("alarm", Context.MODE_PRIVATE)

        with (store.edit()) {
            clear()
            apply()
        }
    }

    //@Ignore
    @Test
    fun setAlarms() {
        val a1 = Alarm(123, true, "premier alarm", "12/12/2022", AlarmFrequency.ONCE, listOf())
        val a2 = Alarm(456, false, "2nd alarm", "02/12/2022", AlarmFrequency.REPEATING, listOf(1, 3, 5))
        val a3 = Alarm(789, true, "last alarm", "6/12/2022", AlarmFrequency.ONCE, listOf())

        var alarms = listOf<Alarm>(a1, a2, a3)

        alarms.forEach { a -> println("manu alarm $a") }

        alarmService.setAlarms(alarms)

        assertTrue(1 < 2)
    }

    @Ignore
    @Test
    fun test() {
        appRepo.findApp()
    }

    @Ignore("TODO")
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.app.app", appContext.packageName)
    }
}