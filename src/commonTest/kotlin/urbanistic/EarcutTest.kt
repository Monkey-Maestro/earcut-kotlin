package urbanistic

import kotlin.test.Test
import urbanistic.earcut.deviation
import urbanistic.earcut.earcut
import urbanistic.earcut.flatten

import bad_diagonals
import bad_hole
import boxy
import building
import collinear_diagonal
import degenerate
import dude
import eberly_3
import empty_square
import expextedError
import expextedTriangleCount
import hole_touching_outer
import hourglass
import issue107
import issue111
import issue119
import issue131
import issue16
import issue17
import issue29
import issue34
import issue35
import issue45
import issue52
import issue83
import outside_ring
import self_touching
import shared_points
import simplified_us_border
import steiner
import touching2
import touching3
import touching4
import touching_holes
import water3
import water3b
import kotlin.test.assertEquals


class EarcutTest{


    private fun doDeviation(input : Array<Array<DoubleArray>>, triangles : List<Int>) : Double{
        val earcutInput = flatten(input)

        val vertices : DoubleArray = earcutInput[0] as DoubleArray
        val holeIndices : IntArray = earcutInput[1] as IntArray
        val dim = earcutInput[2] as Int

        return deviation(vertices, holeIndices, dim, triangles)
    }

    private fun doEarcut(input : Array<Array<DoubleArray>>) : List<Int>?{
        val earcutInput = flatten(input)

        val vertices : DoubleArray = earcutInput[0] as DoubleArray
        val holeIndices : IntArray = earcutInput[1] as IntArray
        val dim = earcutInput[2] as Int

        return earcut(vertices, holeIndices, dim)
    }

    @Test
    fun bad_diagonals_Test(){
        val result = doEarcut(bad_diagonals)
        println("badDiagonalsTest: $result")
        println("tris: ${expextedTriangleCount["bad_diagonals"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["bad_diagonals"], result.size/3)
    }

    @Test
    fun bad_hole_Test(){
        val result = doEarcut(bad_hole)
        println("bad_hole: $result")
        println("tris: ${expextedTriangleCount["bad_hole"]} -> ${result!!.size/3}")
        println("error: ${expextedError["bad_hole"]} -> ${doDeviation(bad_hole, result)}")
        assertEquals(expextedTriangleCount["bad_hole"], result.size/3)
    }

    @Test
    fun boxy_Test(){
        val result = doEarcut(boxy)
        println("boxy: $result")
        println("tris: ${expextedTriangleCount["boxy"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["boxy"], result.size/3)
    }

    @Test
    fun building_Test(){
        val result = doEarcut(building)
        println("building: $result")
        println("tris: ${expextedTriangleCount["building"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["building"], result.size/3)
    }

    @Test
    fun collinear_diagonal_Test(){
        val result = doEarcut(collinear_diagonal)
        println("collinear_diagonal: $result")
        println("tris: ${expextedTriangleCount["collinear_diagonal"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["collinear_diagonal"], result.size/3)
    }

    @Test
    fun degenerate_Test(){
        val result = doEarcut(degenerate)
        println("degenerate: $result")
        println("tris: ${expextedTriangleCount["degenerate"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["degenerate"], result.size/3)
    }

    @Test
    fun dude_Test(){
        val result = doEarcut(dude)
        println("dude: $result")
        println("tris: ${expextedTriangleCount["dude"]} -> ${result!!.size/3}")
        println("error: 2e-15 -> ${doDeviation(dude, result)}")
        assertEquals(expextedTriangleCount["dude"], result.size/3)
    }

    @Test
    fun eberly_3_Test(){
        val result = doEarcut(eberly_3)
        println("eberly_3: $result")
        println("tris: ${expextedTriangleCount["eberly_3"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["eberly_3"], result.size/3)
    }

    @Test
    fun empty_square_Test(){
        val result = doEarcut(empty_square)
        println("empty_square: $result")
        println("tris: ${expextedTriangleCount["empty_square"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["empty_square"], result.size/3)
    }

    @Test
    fun hole_touching_outer_Test(){
        val result = doEarcut(hole_touching_outer)
        println("hole_touching_outer: $result")
        println("tris: ${expextedTriangleCount["hole_touching_outer"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["hole_touching_outer"], result.size/3)
    }

    @Test
    fun hourglass_Test(){
        val result = doEarcut(hourglass)
        println("hourglass: $result")
        println("tris: ${expextedTriangleCount["hourglass"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["hourglass"], result.size/3)
    }

    @Test
    fun issue107_Test(){
        val result = doEarcut(issue107)
        println("issue107: $result")
        println("tris: ${expextedTriangleCount["issue107"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["issue107"], result.size/3)
    }

    @Test
    fun issue111_Test(){
        val result = doEarcut(issue111)
        println("issue111: $result")
        println("tris: ${expextedTriangleCount["issue111"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["issue111"], result.size/3)
    }

    @Test
    fun issue119_Test(){
        val result = doEarcut(issue119)
        println("issue119: $result")
        println("tris: ${expextedTriangleCount["issue119"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["issue119"], result.size/3)
    }

    @Test
    fun issue131_Test(){
        val result = doEarcut(issue131)
        println("issue131: $result")
        println("tris: ${expextedTriangleCount["issue131"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["issue131"], result.size/3)
    }

    @Test
    fun issue16_Test(){
        val result = doEarcut(issue16)
        println("issue16: $result")
        println("tris: ${expextedTriangleCount["issue16"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["issue16"], result.size/3)
    }

    @Test
    fun issue17_Test(){
        val result = doEarcut(issue17)
        println("issue17: $result")
        println("tris: ${expextedTriangleCount["issue17"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["issue17"], result.size/3)
    }

    @Test
    fun issue29_Test(){
        val result = doEarcut(issue29)
        println("issue29: $result")
        println("tris: ${expextedTriangleCount["issue29"]} -> ${result!!.size/3}")
        println("error: ${expextedError["issue29"]} -> ${doDeviation(issue29, result)}")
        assertEquals(expextedTriangleCount["issue29"], result.size/3)
    }

    @Test
    fun issue34_Test(){
        val result = doEarcut(issue34)
        println("issue34: $result")
        println("tris: ${expextedTriangleCount["issue34"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["issue34"], result.size/3)
    }

    @Test
    fun issue35_Test(){
        val result = doEarcut(issue35)
        println("issue35: $result")
        println("tris: ${expextedTriangleCount["issue35"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["issue35"], result.size/3)
    }

    @Test
    fun issue45_Test(){
        val result = doEarcut(issue45)
        println("issue45: $result")
        println("tris: ${expextedTriangleCount["issue45"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["issue45"], result.size/3)
    }

    @Test
    fun issue52_Test(){
        val result = doEarcut(issue52)
        println("issue52: $result")
        println("tris: ${expextedTriangleCount["issue52"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["issue52"], result.size/3)
    }

    @Test
    fun issue83_Test(){
        val result = doEarcut(issue83)
        println("issue83: $result")
        println("tris: ${expextedTriangleCount["issue83"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["issue83"], result.size/3)
    }

    @Test
    fun outside_ring_Test(){
        val result = doEarcut(outside_ring)
        println("outside_ring: $result")
        println("tris: ${expextedTriangleCount["outside_ring"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["outside_ring"], result.size/3)
    }

    @Test
    fun self_touching_Test(){
        val result = doEarcut(self_touching)
        println("self_touching: $result")
        println("tris: ${expextedTriangleCount["self_touching"]} -> ${result!!.size/3}")
        println("error: ${expextedError["self_touching"]} -> ${doDeviation(self_touching, result)}")
        assertEquals(expextedTriangleCount["self_touching"], result.size/3)
    }

    @Test
    fun shared_points_Test(){
        val result = doEarcut(shared_points)
        println("shared_points: $result")
        println("tris: ${expextedTriangleCount["shared_points"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["shared_points"], result.size/3)
    }

    @Test
    fun simplified_us_border_Test(){
        val result = doEarcut(simplified_us_border)
        println("simplified_us_border: $result")
        println("tris: ${expextedTriangleCount["simplified_us_border"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["simplified_us_border"], result.size/3)
    }

    @Test
    fun steiner_Test(){
        val result = doEarcut(steiner)
        println("steiner: $result")
        println("tris: ${expextedTriangleCount["steiner"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["steiner"], result.size/3)
    }

    @Test
    fun touching_holes_Test(){
        val result = doEarcut(touching_holes)
        println("touching_holes: $result")
        println("tris: ${expextedTriangleCount["touching_holes"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["touching_holes"], result.size/3)
    }

    @Test
    fun touching2_Test(){
        val result = doEarcut(touching2)
        println("touching2: $result")
        println("tris: ${expextedTriangleCount["touching2"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["touching2"], result.size/3)
    }

    @Test
    fun touching3_Test(){
        val result = doEarcut(touching3)
        println("touching3: $result")
        println("tris: ${expextedTriangleCount["touching3"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["touching3"], result.size/3)
    }

    @Test
    fun touching4_Test(){
        val result = doEarcut(touching4)
        println("touching4: $result")
        println("tris: ${expextedTriangleCount["touching4"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["touching4"], result.size/3)
    }

    @Test
    fun water3_Test(){
        val result = doEarcut(water3)
        println("water3: $result")
        println("tris: ${expextedTriangleCount["water3"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["water3"], result.size/3)
    }

    @Test
    fun water3b_Test(){
        val result = doEarcut(water3b)
        println("water3b: $result")
        println("tris: ${expextedTriangleCount["water3b"]} -> ${result!!.size/3}")
        assertEquals(expextedTriangleCount["water3b"], result.size/3)
    }
}