package urbanistic.transform

import kotlin.math.sqrt

class Point3(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0) {
    fun set(x: Double, y: Double, z: Double) {
        this.x = x
        this.y = y
        this.z = z
    }

    operator fun minus(v2: Point3): Point3 {
        return Point3(x - v2.x, y - v2.y, z - v2.z)
    }

    operator fun plus(v2: Point3): Point3 {
        return Point3(v2.x + x, v2.y + y, v2.z + z)
    }

    operator fun times(sc: Double): Point3 {
        return Point3(x * sc, y * sc, z * sc)
    }

    operator fun div(sc: Double): Point3 {
        return Point3(x / sc, y / sc, z / sc)
    }

    fun normalize() {
        val d = sqrt(x * x + y * y + z * z)
        if (d != 0.0) {
            x /= d
            y /= d
            z /= d
        }
    }

    fun cross(b: Point3): Point3 {
        return Point3(
            y * b.z - b.y * z,
            z * b.x - b.z * x,
            x * b.y - b.x * y
        )
    }
}

fun normal(vertices: DoubleArray): DoubleArray {
    val ccw = true // counterclockwise normal direction

    val points = arrayListOf<Point3>()
    for (i in 0 until (vertices.size / 3)) {
        points.add(Point3(vertices[i * 3], vertices[i * 3 + 1], vertices[i * 3 + 2]))
    }

    var m3: Point3 = points[points.size - 2]
    var m2 = points[points.size - 1]

    var c123 = Point3()
    var v32: Point3
    var v12: Point3

    for (i in points.indices) {
        val m1 = points[i]

        v32 = m3 - m2
        v12 = m1 - m2

        c123 = if (!ccw) {
            c123 + v32.cross(v12)
        } else {
            c123 + v12.cross(v32)
        }

        m3 = m2
        m2 = m1
    }

    c123.normalize()

    return doubleArrayOf(c123.x, c123.y, c123.z)
}

class AnyToXYTransform(nx: Double, ny: Double, nz: Double) {

    protected var m00 = 0.0
    protected var m01 = 0.0
    protected var m02 = 0.0
    protected var m10 = 0.0
    protected var m11 = 0.0
    protected var m12 = 0.0
    protected var m20 = 0.0
    protected var m21 = 0.0
    protected var m22 = 0.0

    /**
     * normal must be normalized
     *
     * @param nx
     * @param ny
     * @param nz
     */
    fun setSourceNormal(nx: Double, ny: Double, nz: Double) {
        val h: Double
        val f: Double
        val hvx: Double
        val vx: Double = -ny
        val vy: Double = nx
        val c: Double = nz
        h = (1 - c) / (1 - c * c)
        hvx = h * vx
        f = if (c < 0) -c else c
        if (f < 1.0 - 1.0E-4) {
            m00 = c + hvx * vx
            m01 = hvx * vy
            m02 = -vy
            m10 = hvx * vy
            m11 = c + h * vy * vy
            m12 = vx
            m20 = vy
            m21 = -vx
            m22 = c
        } else {
            // if "from" and "to" vectors are nearly parallel
            m00 = 1.0
            m01 = 0.0
            m02 = 0.0
            m10 = 0.0
            m11 = 1.0
            m12 = 0.0
            m20 = 0.0
            m21 = 0.0
            m22 = if (c > 0) {
                1.0
            } else {
                -1.0
            }
        }
    }

    /**
     * Assumes source normal is normalized
     */
    init {
        setSourceNormal(nx, ny, nz)
    }

    fun transform(p: Point3) {
        val px: Double = p.x
        val py: Double = p.y
        val pz: Double = p.z
        p.set(
            m00 * px + m01 * py + m02 * pz,
            m10 * px + m11 * py + m12 * pz,
            m20 * px + m21 * py + m22 * pz
        )
    }

    fun transform(data: DoubleArray) {
        for (i in 0 until (data.size / 3)) {
            val point = Point3(data[i * 3], data[i * 3 + 1], data[i * 3 + 2])
            transform(point)
            data[i * 3] = point.x
            data[i * 3 + 1] = point.y
            data[i * 3 + 2] = point.z
        }
    }
}
