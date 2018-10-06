package co.thecodewarrior.logisimcpu.microcode

import java.io.File
import java.math.BigInteger

class Microcode(val file: File, val stepWidth: Int) {
    val controlLines = ControlLine.controlLines(file.resolve("controls.txt"))
    val instructions = Instruction.parse(file.resolve("instructions.txt"), controlLines)

    fun microcode(): String {
        val lines = mutableListOf("v2.0 big")
        instructions.forEach {
            microcodes(it).forEach {
                lines += "${(it.binary shl stepWidth).toString(16)}: ${it.steps.joinToString(" ") { it.binary.toString(16) }}"
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

    fun variableValues(insn: Instruction): List<Map<Char, Int>> {
        var variableCombinations = listOf(mapOf<Char, Int>())
        insn.variables.filter { it.value.isInline }.forEach { variable ->
            val possibleValues = 0 until (1 shl variable.value.width)
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

    fun stepBinary(step: InstructionStep, variableValues: Map<Char, Int>): MicrocodeStep {
        var binary = BigInteger.ZERO
        step.lines.forEach { line ->
            var value = line.variable?.let { variableValues[it.name] } ?: 1
            value = value and ((1 shl line.line.width)-1)
            binary = binary or (BigInteger.valueOf(value.toLong()) shl line.line.firstBit)
        }
        return MicrocodeStep(step, binary)
    }
}

data class MicrocodeInstruction(val insn: Instruction, val variableValues: Map<Char, Int>, val binary: BigInteger, val steps: List<MicrocodeStep>) {
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
    fun pretty(variableValues: Map<Char, Int>): String {
        return step.lines.joinToString(" ") {
            it.line.name + (it.variable?.let { "<${variableValues[it.name]}>" } ?: "")
        }
    }
}