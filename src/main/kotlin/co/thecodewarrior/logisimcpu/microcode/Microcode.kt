package co.thecodewarrior.logisimcpu.microcode

import co.thecodewarrior.logisimcpu.CPU
import java.math.BigInteger
import kotlin.math.min

class Microcode(val cpu: CPU, val stepWidth: Int) {

    fun microcode(): String {
        val lines = mutableListOf("v2.0 big")
        cpu.instructions.forEach { insn ->
            lines += "${(insn.bits shl stepWidth).toString(16)}: " +
                insn.steps.joinToString(" ") { stepBinary(it).toString(16) }
        }
        return lines.joinToString("\n")
    }

    fun stepBinary(step: InstructionStep): BigInteger {
        var binary = BigInteger.ZERO
        step.lines.forEach { line ->
            var value = when(line) {
                is ConstantStepControlBundle -> line.const
                else -> 1u
            }
            value = value and ((1u shl line.line.width)-1u)
            binary = binary or (BigInteger.valueOf(value.toLong()) shl line.line.firstBit)
        }
        return binary
    }
}
