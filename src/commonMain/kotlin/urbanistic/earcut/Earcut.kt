package urbanistic.earcut

import urbanistic.transform.AnyToXYTransform
import urbanistic.transform.normal
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

/**
 * Kotlin Multiplatform Port of earcut: https://github.com/mapbox/earcut
 * based on the java port: https://github.com/the3deers/earcut-java
 */

class Node(var i: Int, var x: Double, var y: Double) {
    // vertex index in coordinates array
    // vertex coordinates
    // previous and next vertex nodes in a polygon ring
    // z-order curve value
    // previous and next nodes in z-order
    // indicates whether this is a steiner point

    var z: Double = -1.0
    var steiner: Boolean = false
    lateinit var prev: Node
    lateinit var next: Node
    var nextZ: Node? = null
    var prevZ: Node? = null
}

val xComparator = Comparator { a: Node, b: Node -> compareValues(a.x, b.x) }

// done
fun earcut(data: DoubleArray, holeIndices: IntArray, dim: Int): List<Int> {
    val hasHoles = holeIndices.isNotEmpty()
    val outerLen = if (hasHoles) holeIndices[0] * dim else data.size
    var outerNode: Node? = linkedList(data, 0, outerLen, dim, true)
    val triangles: MutableList<Int> = mutableListOf()

    if (outerNode == null || outerNode.next === outerNode.prev) return triangles

    var minX = 0.0
    var minY = 0.0
    var maxX: Double
    var maxY: Double

    var x: Double
    var y: Double

    var invSize = 0.0

    if (hasHoles) outerNode = eliminateHoles(data, holeIndices, outerNode, dim)

    // if the shape is not too simple, we'll use z-order curve hash later; calculate polygon bbox
    if (data.size > 80 * dim) {
        maxX = data[0]
        minX = data[0]
        maxY = data[1]
        minY = data[1]

        for (i in dim until outerLen) {
            x = data[i]
            y = data[i + 1]
            if (x < minX) minX = x
            if (y < minY) minY = y
            if (x > maxX) maxX = x
            if (y > maxY) maxY = y
        }

        // minX, minY and invSize are later used to transform coords into integers for z-order calculation
        invSize = max(maxX - minX, maxY - minY)
        invSize = if (invSize != 0.0) 32767 / invSize else 0.0
    }

    earcutLinked(outerNode, triangles, dim, minX, minY, invSize, 0)

    return triangles
}

// done
// create a circular doubly linked list from polygon points in the specified winding order
private fun linkedList(data: DoubleArray, start: Int, end: Int, dim: Int, clockwise: Boolean): Node? {
    var last: Node? = null

    if (clockwise == (signedArea(data, start, end, dim) > 0)) {
        for (i in start until end) {
            last = insertNode(i, data[i], data[i + 1], last)
        }
    } else {
        for (i in (end - dim) downTo start) {
            last = insertNode(i, data[i], data[i + 1], last)
        }
    }

    if (last != null && equals(last, last.next)) {
        removeNode(last)
        last = last.next
    }

    return last
}

// done
// eliminate collinear or duplicate points
private fun filterPoints(start: Node?, _end: Node?): Node? {
    var end: Node? = _end

    if (start == null) return start
    if (end == null) end = start

    var p: Node = start
    var again: Boolean

    do {
        again = false

        if (!p.steiner && (equals(p, p.next) || area(p.prev, p, p.next) == 0.0)) {
            removeNode(p)
            p = p.prev
            end = p.prev
            if (p === p.next) break
            again = true

        } else {
            p = p.next
        }
    } while (again || p !== end)

    return end
}

// done
// main ear slicing loop which triangulates a polygon (given as a linked list)
private fun earcutLinked(
        _ear: Node?,
        triangles: MutableList<Int>,
        dim: Int,
        minX: Double,
        minY: Double,
        invSize: Double,
        pass: Int
) {
    var ear: Node = _ear ?: return

    // interlink polygon nodes in z-order
    if (pass == 0 && invSize != 0.0) indexCurve(ear, minX, minY, invSize)

    var stop: Node = ear
    var prev: Node
    var next: Node

    // iterate through ears, slicing them one by one
    while (ear.prev !== ear.next) {
        prev = ear.prev
        next = ear.next

        if (if (invSize != 0.0) isEarHashed(ear, minX, minY, invSize) else isEar(ear)) {
            // cut off the triangle
            triangles.add(prev.i / dim or 0)
            triangles.add(ear.i / dim or 0)
            triangles.add(next.i / dim or 0)

            removeNode(ear)

            // skipping the next vertex leads to less sliver triangles
            ear = next.next
            stop = next.next

            continue
        }

        ear = next

        // if we looped through the whole remaining polygon and can't find any more ears
        if (ear === stop) {
            when (pass) {
                // try filtering points and slicing again
                0 -> {
                    earcutLinked(filterPoints(ear, null), triangles, dim, minX, minY, invSize, 1)
                }
                // if this didn't work, try curing all small self-intersections locally
                1 -> {
                    ear = cureLocalIntersections(filterPoints(ear, null)!!, triangles, dim)!!
                    earcutLinked(ear, triangles, dim, minX, minY, invSize, 2)
                }
                // as a last resort, try splitting the remaining polygon into two
                2 -> {
                    splitEarcut(ear, triangles, dim, minX, minY, invSize)
                }
            }

            break
        }
    }
}

// done
// check whether a polygon node forms a valid ear with adjacent nodes
private fun isEar(ear: Node): Boolean {
    val a: Node = ear.prev
    val b: Node = ear
    val c: Node = ear.next

    if (area(a, b, c) >= 0) return false // reflex, can't be an ear

    val ax = a.x
    val bx = b.x
    val cx = c.x
    val ay = a.y
    val by = b.y
    val cy = c.y

    // triangle bbox; min & max are calculated like this for speed
    val x0 = if (ax < bx) (if (ax < cx) ax else cx) else if (bx < cx) bx else cx
    val y0 = if (ay < by) (if (ay < cy) ay else cy) else if (by < cy) by else cy
    val x1 = if (ax > bx) (if (ax > cx) ax else cx) else if (bx > cx) bx else cx
    val y1 = if (ay > by) (if (ay > cy) ay else cy) else if (by > cy) by else cy

    // now make sure we don't have other points inside the potential ear
    var p = c.next
    while (p !== a) {
        if (p.x >= x0 && p.x <= x1 && p.y >= y0 && p.y <= y1 &&
                pointInTriangle(ax, ay, bx, by, cx, cy, p.x, p.y) &&
                area(p.prev, p, p.next) >= 0) return false
        p = p.next;
    }

    return true
}

// done
private fun isEarHashed(ear: Node, minX: Double, minY: Double, invSize: Double): Boolean {
    val a: Node = ear.prev
    val b: Node = ear
    val c: Node = ear.next

    if (area(a, b, c) >= 0) return false // reflex, can't be an ear

    val ax = a.x
    val bx = b.x
    val cx = c.x
    val ay = a.y
    val by = b.y
    val cy = c.y

    // triangle bbox; min & max are calculated like this for speed
    val x0 = if (ax < bx) (if (ax < cx) ax else cx) else if (bx < cx) bx else cx
    val y0 = if (ay < by) (if (ay < cy) ay else cy) else if (by < cy) by else cy
    val x1 = if (ax > bx) (if (ax > cx) ax else cx) else if (bx > cx) bx else cx
    val y1 = if (ay > by) (if (ay > cy) ay else cy) else if (by > cy) by else cy

    // z-order range for the current triangle bbox;
    val minZ = zOrder(x0, y0, minX, minY, invSize)
    val maxZ = zOrder(x1, y1, minX, minY, invSize)

    var p = ear.prevZ
    var n = ear.nextZ

    // look for points inside the triangle in both directions
    while (p != null && p.z >= minZ && n != null && n.z <= maxZ) {
        if (p.x >= x0 && p.x <= x1 && p.y >= y0 && p.y <= y1 && p !== a && p !== c &&
                pointInTriangle(ax, ay, bx, by, cx, cy, p.x, p.y) && area(p.prev, p, p.next) >= 0) return false
        p = p.prevZ

        if (n.x >= x0 && n.x <= x1 && n.y >= y0 && n.y <= y1 && n !== a && n !== c &&
                pointInTriangle(ax, ay, bx, by, cx, cy, n.x, n.y) && area(n.prev, n, n.next) >= 0) return false
        n = n.nextZ
    }

    // look for remaining points in decreasing z-order
    while (p != null && p.z >= minZ) {
        if (p.x >= x0 && p.x <= x1 && p.y >= y0 && p.y <= y1 && p !== a && p !== c &&
                pointInTriangle(ax, ay, bx, by, cx, cy, p.x, p.y) && area(p.prev, p, p.next) >= 0) return false
        p = p.prevZ
    }


    // look for remaining points in increasing z-order
    while (n != null && n.z <= maxZ) {
        if (n.x >= x0 && n.x <= x1 && n.y >= y0 && n.y <= y1 && n !== a && n !== c &&
                pointInTriangle(ax, ay, bx, by, cx, cy, n.x, n.y) && area(n.prev, n, n.next) >= 0) return false
        n = n.nextZ
    }

    return true
}

// done
// go through all polygon nodes and cure small local self-intersections
private fun cureLocalIntersections(_start: Node, triangles: MutableList<Int>, dim: Int): Node? {
    var start: Node = _start
    var p: Node = start

    do {
        val a: Node = p.prev
        val b: Node = p.next.next

        if (!equals(a, b) && intersects(a, p, p.next, b) && locallyInside(a, b) && locallyInside(b, a)) {
            triangles.add(a.i / dim or 0)
            triangles.add(p.i / dim or 0)
            triangles.add(b.i / dim or 0)

            // remove two nodes involved
            removeNode(p)
            removeNode(p.next)

            start = b
            p = b
        }
        p = p.next
    } while (p !== start)

    return filterPoints(p, null)
}

// done
// try splitting polygon into two and triangulate them independently
private fun splitEarcut(
        start: Node,
        triangles: MutableList<Int>,
        dim: Int,
        minX: Double,
        minY: Double,
        invSize: Double
) {
    // look for a valid diagonal that divides the polygon into two
    var a: Node? = start
    do {
        var b: Node? = a!!.next.next
        while (b !== a!!.prev) {
            if (a!!.i != b!!.i && isValidDiagonal(a, b)) {
                // split the polygon in two by the diagonal
                var c: Node? = splitPolygon(a, b)

                // filter collinear points around the cuts
                a = filterPoints(a, a.next)
                c = filterPoints(c!!, c.next)

                // run earcut on each half
                earcutLinked(a, triangles, dim, minX, minY, invSize, 0)
                earcutLinked(c, triangles, dim, minX, minY, invSize, 0)
                return
            }
            b = b.next
        }
        a = a!!.next
    } while (a !== start)
}

// link every hole into the outer loop, producing a single-ring polygon without holes
private fun eliminateHoles(
        data: DoubleArray,
        holeIndices: IntArray,
        _outerNode: Node,
        dim: Int
): Node? {
    var outerNode: Node? = _outerNode

    val queue: MutableList<Node> = mutableListOf()

    var i = 0
    var len: Int
    var start: Int
    var end: Int
    var list: Node?

    i = 0
    len = holeIndices.size
    while (i < len) {
        start = holeIndices[i] * dim;
        end = if (i < len - 1) holeIndices[i + 1] * dim else data.size
        list = linkedList(data, start, end, dim, false)

        if (list === list!!.next) list.steiner = true
        queue.add(getLeftmost(list)!!)

        i++
    }

    queue.sortWith(xComparator)

    // process holes from left to right
    for (ii in 0 until queue.size) {
        outerNode = eliminateHole(queue[ii], outerNode!!)
    }

    return outerNode
}

// done
// find a bridge between vertices that connects hole with an outer ring and link it
private fun eliminateHole(hole: Node, outerNode: Node): Node {
    val bridge = findHoleBridge(hole, outerNode)
    if (bridge == null) {
        return outerNode;
    }

    val bridgeReverse = splitPolygon(bridge, hole)

    // filter collinear points around the cuts
    filterPoints(bridgeReverse, bridgeReverse.next)
    return filterPoints(bridge, bridge.next)!!
}

// done
// David Eberly's algorithm for finding a bridge between hole and outer polygon
private fun findHoleBridge(hole: Node, outerNode: Node): Node? {
    var p: Node = outerNode
    val hx: Double = hole.x
    val hy: Double = hole.y
    var qx = -Double.MAX_VALUE
    var m: Node? = null

    // find a segment intersected by a ray from the hole's leftmost point to the left;
    // segment's endpoint with lesser x will be potential connection point
    do {
        if (hy <= p.y && hy >= p.next.y && p.next.y != p.y) {
            val x: Double = p.x + (hy - p.y) * (p.next.x - p.x) / (p.next.y - p.y)
            if (x <= hx && x > qx) {
                qx = x
                m = if(p.x < p.next.x) p else p.next
                if(x == hx) return m
            }
        }
        p = p.next
    } while (p !== outerNode)

    if (m == null) return null

    // look for points inside the triangle of hole point, segment intersection and endpoint;
    // if there are no points found, we have a valid connection;
    // otherwise choose the point of the minimum angle with the ray as connection point

    val stop: Node = m
    val mx: Double = m.x
    val my: Double = m.y
    var tanMin = Double.MAX_VALUE
    var tan: Double

    p = m

    do {
        if (hx >= p.x && p.x >= mx && hx != p.x &&
                pointInTriangle(if (hy < my) hx else qx, hy, mx, my, if (hy < my) qx else hx, hy, p.x, p.y)
        ) {

            tan = abs(hy - p.y) / (hx - p.x) // tangential

            if (locallyInside(p, hole) &&
                    (tan < tanMin || tan == tanMin && (p.x > m!!.x || p.x == m.x && sectorContainsSector(m, p)))
            ) {
                m = p
                tanMin = tan
            }
        }

        p = p.next
    } while (p !== stop)

    return m
}

// whether sector in vertex m contains sector in vertex p in the same coordinates
private fun sectorContainsSector(m: Node, p: Node): Boolean {
    return area(m.prev, m, p.prev) < 0 && area(p.next, m, m.next) < 0
}

// interlink polygon nodes in z-order
private fun indexCurve(start: Node, minX: Double, minY: Double, invSize: Double) {
    var p: Node? = start
    do {
        if (p!!.z == 0.0) p.z = zOrder(p.x, p.y, minX, minY, invSize)
        p.prevZ = p.prev
        p.nextZ = p.next
        p = p.next
    } while (p !== start)

    p.prevZ!!.nextZ = null
    p.prevZ = null

    sortLinked(p)
}

// done
// Simon Tatham's linked list merge sort algorithm
// http://www.chiark.greenend.org.uk/~sgtatham/algorithms/listsort.html
private fun sortLinked(_list: Node): Node {
    var list: Node? = _list
    var p: Node?
    var q: Node?
    var e: Node?
    var tail: Node?
    var numMerges: Int
    var pSize: Int
    var qSize: Int
    var inSize = 1

    do {
        p = list
        list = null
        tail = null
        numMerges = 0

        while (p != null) {
            numMerges++
            q = p
            pSize = 0

            for(i in 0 until inSize){
                pSize++
                q = q!!.nextZ
                if (q == null) break
            }
            qSize = inSize

            while (pSize > 0 || (qSize > 0 && q != null)) {

                if (pSize != 0 && (qSize == 0 || q == null || p!!.z <= q.z)) {
                    e = p
                    p = p!!.nextZ
                    pSize--
                } else {
                    e = q
                    q = q!!.nextZ
                    qSize--
                }

                if (tail != null) tail.nextZ = e else list = e

                e!!.prevZ = tail
                tail = e
            }

            p = q
        }

        tail!!.nextZ = null
        inSize *= 2

    } while (numMerges > 1)

    return list!!
}

// z-order of a point given coords and inverse of the longer side of data bbox
private fun zOrder(x0: Double, y0: Double, minX: Double, minY: Double, invSize: Double): Double {
    // coords are transformed into non-negative 15-bit integer range
    var x = (32767 * (x0 - minX) * invSize).toInt()
    var y = (32767 * (y0 - minY) * invSize).toInt()
    x = x or (x shl 8) and 0x00FF00FF
    x = x or (x shl 4) and 0x0F0F0F0F
    x = x or (x shl 2) and 0x33333333
    x = x or (x shl 1) and 0x55555555
    y = y or (y shl 8) and 0x00FF00FF
    y = y or (y shl 4) and 0x0F0F0F0F
    y = y or (y shl 2) and 0x33333333
    y = y or (y shl 1) and 0x55555555
    return (x or (y shl 1)).toDouble()
}

// find the leftmost node of a polygon ring
private fun getLeftmost(start: Node?): Node? {
    if (start == null) {
        return null
    }

    var p: Node = start
    var leftmost: Node = start
    do {
        if (p.x < leftmost.x || p.x == leftmost.x && p.y < leftmost.y) leftmost = p
        p = p.next
    } while (p !== start)
    return leftmost
}

// check if a point lies within a convex triangle
private fun pointInTriangle(
        ax: Double,
        ay: Double,
        bx: Double,
        by: Double,
        cx: Double,
        cy: Double,
        px: Double,
        py: Double
): Boolean {
    return (cx - px) * (ay - py) - (ax - px) * (cy - py) >= 0 && (ax - px) * (by - py) - (bx - px) * (ay - py) >= 0 && (bx - px) * (cy - py) - (cx - px) * (by - py) >= 0
}

// check if a diagonal between two polygon nodes is valid (lies in polygon interior)
private fun isValidDiagonal(a: Node, b: Node): Boolean {
    return a.next.i != b.i && a.prev.i != b.i && !intersectsPolygon(a, b) && // dones't intersect other edges
            (
                    locallyInside(a, b) && locallyInside(b, a) && middleInside(a, b) && // locally visible
                            (area(a.prev, a, b.prev) != 0.0 || area(
                                    a,
                                    b.prev,
                                    b
                            ) != 0.0) || // does not create opposite-facing sectors
                            equals(a, b) && area(a.prev, a, a.next) > 0 && area(b.prev, b, b.next) > 0
                    ) // special zero-length case
}

// signed area of a triangle
private fun area(p: Node, q: Node, r: Node): Double {
    return (q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y)
}

// check if two points are equal
private fun equals(p1: Node?, p2: Node?): Boolean {
    if (p1 == null || p2 == null) return false
    return p1.x == p2.x && p1.y == p2.y
}

// check if two segments intersect
private fun intersects(p1: Node?, q1: Node?, p2: Node, q2: Node): Boolean {
    val o1 = sign(area(p1, q1, p2)).toInt()
    val o2 = sign(area(p1, q1, q2)).toInt()
    val o3 = sign(area(p2, q2, p1)).toInt()
    val o4 = sign(area(p2, q2, q1)).toInt()
    if (o1 != o2 && o3 != o4) return true // general case
    if (o1 == 0 && onSegment(p1, p2, q1)) return true // p1, q1 and p2 are collinear and p2 lies on p1q1
    if (o2 == 0 && onSegment(p1, q2, q1)) return true // p1, q1 and q2 are collinear and q2 lies on p1q1
    if (o3 == 0 && onSegment(p2, p1, q2)) return true // p2, q2 and p1 are collinear and p1 lies on p2q2
    return o4 == 0 && onSegment(p2, q1, q2) // p2, q2 and q1 are collinear and q1 lies on p2q2
}

// for collinear points p, q, r, check if point q lies on segment pr
private fun onSegment(p: Node?, q: Node?, r: Node?): Boolean {
    return q!!.x <= max(p!!.x, r!!.x) && q.x >= min(
            p.x,
            r.x
    ) && q.y <= max(
            p.y,
            r.y
    ) && q.y >= min(p.y, r.y)
}

// check if a polygon diagonal intersects any polygon segments
private fun intersectsPolygon(a: Node, b: Node): Boolean {
    var p: Node? = a
    do {
        if (p!!.i != a.i && p.next.i != a.i && p.i != b.i && p.next.i != b.i &&
                intersects(p, p.next, a, b)
        ) {
            return true
        }
        p = p.next
    } while (p !== a)
    return false
}

// check if a polygon diagonal is locally inside the polygon
private fun locallyInside(a: Node, b: Node): Boolean {
    return if (area(a.prev, a, a.next) < 0) {
        area(a, b, a.next) >= 0 && area(a, a.prev, b) >= 0
    } else {
        area(a, b, a.prev) < 0 || area(a, a.next, b) < 0
    }
}

// check if the middle point of a polygon diagonal is inside the polygon
private fun middleInside(a: Node, b: Node): Boolean {
    var p: Node? = a
    var inside = false
    val px = (a.x + b.x) / 2
    val py = (a.y + b.y) / 2
    do {
        if (p!!.y > py != p.next.y > py && p.next.y != p.y &&
                px < (p.next.x - p.x) * (py - p.y) / (p.next.y - p.y) + p.x
        ) {
            inside = !inside
        }
        p = p.next
    } while (p !== a)
    return inside
}

// link two polygon vertices with a bridge; if the vertices belong to the same ring, it splits polygon into two;
// if one belongs to the outer ring and another to a hole, it merges it into a single ring
private fun splitPolygon(a: Node, b: Node): Node {
    val a2 = Node(a.i, a.x, a.y)
    val b2 = Node(b.i, b.x, b.y)
    val an = a.next
    val bp = b.prev
    a.next = b
    b.prev = a
    a2.next = an
    an.prev = a2
    b2.next = a2
    a2.prev = b2
    bp.next = b2
    b2.prev = bp
    return b2
}

// create a node and optionally link it with previous one (in a circular doubly linked list)
private fun insertNode(i: Int, x: Double, y: Double, last: Node?): Node {
    val p = Node(i, x, y)
    if (last == null) {
        p.prev = p
        p.next = p
    } else {
        p.next = last.next
        p.prev = last
        last.next.prev = p
        last.next = p
    }
    return p
}

private fun removeNode(p: Node) {
    p.next.prev = p.prev
    p.prev.next = p.next
    if (p.prevZ != null) p.prevZ!!.nextZ = p.nextZ
    if (p.nextZ != null) p.nextZ!!.prevZ = p.prevZ
}

private fun signedArea(data: DoubleArray, start: Int, end: Int, dim: Int): Double {
    var sum = 0.0
    var i = start
    var j = end - dim
    while (i < end) {
        sum += (data[j] - data[i]) * (data[i + 1] + data[j + 1])
        j = i
        i += dim
    }
    return sum
}

// return a percentage difference between the polygon area and its triangulation area;
// used to verify correctness of triangulation
fun deviation(data: DoubleArray, holeIndices: IntArray?, dim: Int, triangles: List<Int>): Double {
    val hasHoles = holeIndices != null && holeIndices.isNotEmpty()
    val outerLen = if (hasHoles) holeIndices!![0] * dim else data.size
    var polygonArea: Double = abs(signedArea(data, 0, outerLen, dim))
    if (hasHoles) {
        var i = 0
        val len = holeIndices!!.size
        while (i < len) {
            val start = holeIndices[i] * dim
            val end = if (i < len - 1) holeIndices[i + 1] * dim else data.size
            polygonArea -= abs(signedArea(data, start, end, dim))
            i++
        }
    }
    var trianglesArea = 0.0
    var i = 0
    while (i < triangles.size) {
        val a = triangles[i] * dim
        val b = triangles[i + 1] * dim
        val c = triangles[i + 2] * dim
        trianglesArea += abs(
                (data[a] - data[c]) * (data[b + 1] - data[a + 1]) -
                        (data[a] - data[b]) * (data[c + 1] - data[a + 1])
        )
        i += 3
    }
    return if (polygonArea == 0.0 && trianglesArea == 0.0) 0.0 else abs((trianglesArea - polygonArea) / polygonArea)
}

// turn a polygon in a multi-dimensional array form (e.g. as in GeoJSON) into a form Earcut accepts
fun flatten(data: Array<Array<DoubleArray>>): Array<Any> {
    val dim: Int = data[0][0].size
    val f: MutableList<Double> = mutableListOf()
    val hi: MutableList<Int> = mutableListOf()
    var holeIndex = 0
    for (i in data.indices) {
        for (element in data[i]) {
            for (d in 0 until dim) f.add(element[d])
        }
        if (i > 0) {
            holeIndex += data[i - 1].size
            hi.add(holeIndex)
        }
    }

    return arrayOf(f.toDoubleArray(), hi.toIntArray(), dim)
}

/**
 * lays any 3Dimensional planar polygon out on xy plane
 */
fun toXY(data: DoubleArray): DoubleArray {
    val normal = normal(data)
    val anyToXYTransform = AnyToXYTransform(normal[0], normal[1], normal[2])
    val result = data.copyOf()
    anyToXYTransform.transform(result)
    return result
}
