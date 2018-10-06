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

fun String.parseIntLike(): Int {
    if(this.endsWith("u"))
        return this.removeSuffix("u").parseUIntLike().toInt()
    return when {
        this.startsWith("0x") -> this.removePrefix("0x").toInt(16)
        this.startsWith("0o") -> this.removePrefix("0o").toInt(8)
        this.startsWith("0b") -> this.removePrefix("0b").toInt(2)
        else -> this.toInt()
    }
}

fun String.parseUIntLike(): UInt {
    return when {
        this.startsWith("0x") -> this.removePrefix("0x").toUInt(16)
        this.startsWith("0o") -> this.removePrefix("0o").toUInt(8)
        this.startsWith("0b") -> this.removePrefix("0b").toUInt(2)
        else -> this.toUInt()
    }
}
