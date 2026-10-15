package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testQrCodeGenerationProducesValidBitmap() {
    val bitmap = com.example.util.QrCodeUtils.generateQrBitmap("SR-JKT-8821", 256)
    assertNotNull(bitmap)
    assertEquals(256, bitmap?.width)
    assertEquals(256, bitmap?.height)
  }
}
