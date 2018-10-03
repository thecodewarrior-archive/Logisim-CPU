package co.thecodewarrior.logisimcpu.microcode

import java.io.File
import java.math.BigInteger

data class ControlLine(val name: String, val firstBit: Int, val width: Int = 1) {
    val mask = ((BigInteger.ONE shl width) - BigInteger.ONE) shl firstBit

    fun encode(value: Int): BigInteger {
        return (value.toBigInteger() shl firstBit) and mask
    }

    companion object {
        fun controlLines(file: File): List<ControlLine> {
            val regex = """(\w+)(?:\[(\d+)])?""".toRegex()
            var i = 0
            return file.readLines().asSequence()
                .map { it.split("#").first().trim() }
                .filter { it.isNotBlank() }
                .mapNotNull {
                    regex.matchEntire(it)?.groupValues
                }
                .map {
                    val width = it[2].toIntOrNull() ?: 1
                    val line = ControlLine(it[1], i, width)
                    i += width
                    line
                }.toList()
        }
    }
}