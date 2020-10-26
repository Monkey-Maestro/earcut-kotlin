package urbanistic.transform


class Point3(var x: Double, var y: Double, var z: Double){
    fun set(x: Double, y: Double, z: Double){
        this.x = x
        this.y = y
        this.z = z
    }
}


class AnyToXYTransform(nx: Double, ny: Double, nz: Double){

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
            if (c > 0) {
                m22 = 1.0
            } else {
                m22 = -1.0
            }
        }
    }

    /**
     * Assumes source normal is normalized
     */
    init {
        setSourceNormal(nx, ny, nz)
    }

    fun transform(p: Point3, store: Point3) {
        val px: Double = p.x
        val py: Double = p.y
        val pz: Double = p.z
        store.set(m00 * px + m01 * py + m02 * pz,
                m10 * px + m11 * py + m12 * pz,
                m20 * px + m21 * py + m22 * pz)
    }

    fun transform(p: Point3) {
        val px: Double = p.x
        val py: Double = p.y
        val pz: Double = p.z
        p.set(m00 * px + m01 * py + m02 * pz,
                m10 * px + m11 * py + m12 * pz,
                m20 * px + m21 * py + m22 * pz)
    }

    fun transform(list: List<Point3>) {
        for (p in list) {
            transform(p)
        }
    }
}