package zechs.zplex.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import zechs.zplex.utils.util.Converter
import zechs.zplex.utils.util.Converter.convertMinutes
import zechs.zplex.utils.util.Converter.getSize
import zechs.zplex.utils.util.Converter.parseDate
import zechs.zplex.utils.util.Converter.toDuration
import zechs.zplex.utils.util.Converter.yearsBetween

class ConverterUtilsTest {

    @Test
    fun `dates to time ago`() {
        val expected = "2 years ago"
        val response = toDuration("2020-04-20 20:00")
        assertEquals(expected, response)
    }

    @Test
    fun `bytes to human readable size`() {
        val expected = "1.00 KB"
        val response = getSize(1024L)
        assertEquals(expected, response)
    }

    @Test
    fun `minutes to hours`() {
        val expected = "30 min"
        val response = convertMinutes(30)
        assertEquals(expected, response)
    }

    @Test
    fun `dates to formatted date`() {
        val expected = "Monday 20, 2020"
        val response = parseDate("2020-04-20")
        assertEquals(expected, response)
    }

    @Test
    fun `age from birthday`() {
        val expected = 20
        val response = yearsBetween("2002-02-21", Converter.getDate())
        assertEquals(expected, response)
    }

}