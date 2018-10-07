package co.thecodewarrior.logisimcpu.microcode

import co.thecodewarrior.logisimcpu.CPU
import java.math.BigInteger
import kotlin.math.min

class Microcode(val cpu: CPU, val stepWidth: Int) {

    fun microcode(): String {
        val lines = mutableListOf("v2.0 big")
        cpu.instructions.forEach {
            microcodes(it).forEach {
                lines += "${(it.binary shr (it.insn.bits.paramWidth - stepWidth)).toString(16)}: ${it.steps.joinToString(" ") { it.binary.toString(16) }}"
            }
        }
        return lines.joinToString("\n")
    }

    fun microcodes(insn: Instruction): List<MicrocodeInstruction> {
        return variableValues(insn).map { variableValues ->
            var binary = BigInteger.valueOf(insn.opcode(variableValues).toLong())
            var steps = insn.steps.map { stepBinary(it, variableValues) }
             MicrocodeInstruction(insn, variableValues, binary, steps)
        }
    }

    fun variableValues(insn: Instruction): List<Map<Char, UInt>> {
        var variableCombinations = listOf(mapOf<Char, UInt>())
        insn.variables.filter { it.value.isInline }.forEach { variable ->
            val possibleValues =
                if(variable.value.enum != null)
                    0u until min(variable.value.enum!!.size.toLong(), (1L shl variable.value.width)).toUInt()
                else
                    0u until (1u shl variable.value.width)
            variableCombinations = variableCombinations.flatMap { combo ->
                possibleValues.map { value ->
                    val newMap = combo.toMutableMap()
                    newMap[variable.key] = value
                    newMap
                }
            }
        }
        return variableCombinations
    }

    fun stepBinary(step: InstructionStep, variableValues: Map<Char, UInt>): MicrocodeStep {
        var binary = BigInteger.ZERO
        step.lines.forEach { line ->
            var value = when(line) {
                is VariableStepControlBundle -> variableValues[line.variable.name] ?: 1u
                is ConstantStepControlBundle -> line.const
                else -> 1u
            }
            value = value and ((1u shl line.line.width)-1u)
            binary = binary or (BigInteger.valueOf(value.toLong()) shl line.line.firstBit)
        }
        return MicrocodeStep(step, binary)
    }
}

data class MicrocodeInstruction(val insn: Instruction, val variableValues: Map<Char, UInt>, val binary: BigInteger, val steps: List<MicrocodeStep>) {
    fun pretty(): String {
        return """
INSN: ${prettyAssembly()}
    `- ${binary.toString(2)}
${
        steps.mapIndexed { i, step ->
            "- step ${i.toString().padStart(2)}: ${step.pretty(variableValues)}\n" +
            "         `- ${step.binary.toString(2)}"
        }.joinToString("\n")
}
        """.trimIndent()
    }

    private fun prettyAssembly(): String {
        return insn.assembly.joinToString(" ") {
            when(it) {
                is FixedAssemblyWord -> it.text
                is VariableAssemblyWord -> variableValues[it.name]?.toString() ?: "?"
                else -> "!"
            }
        }
    }
}

data class MicrocodeStep(val step: InstructionStep, val binary: BigInteger) {
    fun pretty(variableValues: Map<Char, UInt>): String {
        return step.lines.joinToString(" ") {
            var value = when(it) {
                is VariableStepControlBundle -> variableValues[it.variable.name] ?: 1u
                is ConstantStepControlBundle -> it.const
                else -> null
            }
            it.line.name + (value?.let { "<$it>" } ?: "")
        }
    }
}