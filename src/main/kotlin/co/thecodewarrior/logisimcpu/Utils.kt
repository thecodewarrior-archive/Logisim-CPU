package co.thecodewarrior.logisimcpu

/**
 * returns the lowest set bit, or -1 if this == 0u
 */
val UInt.lowestSetBit: Int
    get() {
        if(this == 0u) return -1
        var i = 0
        var running = this
        while(running and 1u == 0u) {
            running = running shr 1
            i++
        }
        return i
    }

val UInt.bitCount: Int
    get() {
        var count = 0
        var running = this
        while(running != 0u) {
            count += (running and 1u).toInt()
            running = running shr 1
        }
        return count
    }
