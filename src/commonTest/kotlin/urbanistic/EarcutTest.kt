package urbanistic

import expextedError
import expextedTriangleCount
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import testInput
import urbanistic.earcut.*
import urbanistic.transform.normal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EarcutTest {
    private fun stringToEearcutInput(s: String): EarcutInput {
        val testCase = Json.decodeFromString<Array<Array<DoubleArray>>>(s)
        return flatten(testCase)
    }

    @Test
    fun allTests() {
        for (key in testInput.keys) {
            println("testing $key")
            val earcutInput = stringToEearcutInput(testInput[key]!!)
            val earcutResult = earcut(earcutInput)

            if (expextedTriangleCount.containsKey(key)) {
                // check triangle count
                val triCount = expextedTriangleCount[key]!!
                val triCountIs = earcutResult.size / 3

                println("  triangles should be $triCount is $triCountIs")
                assertEquals(triCount, triCountIs)
            }
            if (expextedError.containsKey(key)) {
                // check area error
                val error = deviation(earcutInput, earcutResult)

                println("  area error should be ${expextedError[key]} is $error")
                assertTrue(expextedError[key]!!.toDouble() >= error, "Error too high")
            }
        }
    }

    @Test
    fun toXY_Test() {
        val nonXY = doubleArrayOf(
            0.0, 0.0, 0.0,
            10.0, 0.0, 10.0,
            10.0, 10.0, 10.0,
            0.0, 10.0, 0.0
        )
        val normal = normal(nonXY)
        for (d in normal)
            println(d)
        val xy = toXY(nonXY)
        for (d in xy)
            println(d)
    }
}
