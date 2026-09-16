package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PrayerTimeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MASJID JAMI' AT-TAQWA", appName)
  }

  @Test
  fun `verify offline prayer time calculation results`() {
    val jakarta = PrayerTimeCalculator.cities.first { it.name == "Jakarta" }
    val times = PrayerTimeCalculator.calculatePrayerTimes(jakarta, 258, 0) // Day of year 258

    assertNotNull(times)
    assertEquals("04:30", times.subuh)
    assertEquals("17:52", times.maghrib)
  }
}
