package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import android.app.Application
import com.example.ui.viewmodel.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    assertEquals("Syllabus AI", appName)
  }

  @Test
  fun `enforce 50 daily upload restriction limit`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = MainViewModel(app)

    // First 50 uploads should succeed
    for (i in 1..50) {
      val success = viewModel.attemptUpload("Title $i", "Content $i", "Biology", "FILE", "doc_$i.pdf")
      assertTrue("Upload $i should succeed", success)
      assertEquals(i, viewModel.dailyUploadCount.value)
    }

    // 51st upload must be blocked by the restriction
    val blocked = viewModel.attemptUpload("Title 51", "Content 51", "Biology", "FILE", "doc_51.pdf")
    assertFalse("51st upload should be blocked", blocked)
    assertEquals(50, viewModel.dailyUploadCount.value)
    assertTrue(viewModel.uploadError.value?.contains("Maximum 50 uploads") == true)
  }
}
