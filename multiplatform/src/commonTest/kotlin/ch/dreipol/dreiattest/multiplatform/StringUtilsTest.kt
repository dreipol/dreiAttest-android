package ch.dreipol.dreiattest.multiplatform

import ch.dreipol.dreiattest.multiplatform.utils.removeProtocolFromUrl
import kotlin.test.Test
import kotlin.test.assertEquals

class StringUtilsTest {

    @Test
    fun removeProtocolFromUrlTest() {
        val url = "https://www.google.ch"
        assertEquals("www.google.ch", url.removeProtocolFromUrl())
    }
}