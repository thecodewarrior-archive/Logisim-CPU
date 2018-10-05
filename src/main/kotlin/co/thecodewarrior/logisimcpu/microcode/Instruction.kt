package co.thecodewarrior.logisimcpu.microcode

import java.io.File
import java.math.BigInteger
import java.util.LinkedList

data class Instruction(val assembly: List<AssemblyWord>, val variables: Map<Char, InstructionVariable>, val bits: InstructionBits, val steps: List<InstructionStep>) {
    companion object {
        fun parse(file: File, controlLinesList: List<ControlLine>): List<Instruction> {
            val controlLines = controlLinesList.associateBy { it.name }
            return file.readLines().asSequence()
                .map { it.split("#").first().trim() }
                .filter { it.isNotBlank() }
                .toList().let { LinkedList(it) }
                .let { lines ->
                    val instructions = mutableListOf<Instruction>()
                    while(lines.peek() != null) {
                        val wordsBits = lines.pop().removePrefix(":").split("=")
                        val words = AssemblyWord.parse(wordsBits[0])
                        val variables = InstructionVariable.from(words)
                        val bits = InstructionBits.parse(wordsBits[1], variables)
                        val steps = lines.takeWhile { it.startsWith("-") }
                            .map { InstructionStep.parse(it.removePrefix("-"), controlLines, variables) }
                        steps.forEach { _ -> lines.pop() }
                        instructions.add(Instruction(words, variables, bits, steps))
                    }
                    instructions
                }
        }
    }
}

data class InstructionStep(val lines: List<ParameterizedControlLine>) {
    companion object {
        fun parse(text: String, controlLines: Map<String, ControlLine>, variables: Map<Char, InstructionVariable>): InstructionStep {
            val lines = text.trim().split("\\s+".toRegex())
                .map { ParameterizedControlLine.parse(it, controlLines, variables) }
            return InstructionStep(lines)
        }
    }
}

data class ParameterizedControlLine(val line: ControlLine, val variable: InstructionVariable?) {
    companion object {
        fun parse(text: String, controlLines: Map<String, ControlLine>, variables: Map<Char, InstructionVariable>): ParameterizedControlLine {
            val regex = """(\w+)(?:<(\w)>)?""".toRegex()
            val matches = regex.matchEntire(text.trim())?.groupValues
                ?: throw RuntimeException("Control line doesn't match: `$text`")
            val line = controlLines[matches[1]]
                ?: throw RuntimeException("Couldn't find control line: `${matches[1]}`")
            return ParameterizedControlLine(line, matches[2].let { if(it.isEmpty()) null else variables[it[0]] })
        }
    }
}

abstract class AssemblyWord {
    abstract fun matches(value: String): Boolean

    companion object {
        fun parse(text: String): List<AssemblyWord> {
            return text.split("\\s+".toRegex())
                .map {
                    if(it.startsWith("<") && it.endsWith(">"))
                        VariableAssemblyWord(it[1])
                    else
                        FixedAssemblyWord(it)
                }
        }
    }
}
data class FixedAssemblyWord(val text: String): AssemblyWord() {
    override fun matches(value: String) = value == text
}
data class VariableAssemblyWord(val name: Char): AssemblyWord() {
    override fun matches(value: String) = value[0].isDigit() || value[0] == '>'
}

data class InstructionBits(val bits: BigInteger, val variables: Map<Char, BigInteger>) {

    companion object {
        fun parse(text: String, variableReferences: Map<Char, InstructionVariable>): InstructionBits {
            val variables = mutableMapOf<Char, BigInteger>()
            var bits = BigInteger.ZERO
            text.replace("\\s+".toRegex(), "")
                .reversed().forEachIndexed { i, chr ->
                    when(chr) {
                        '0' -> {}
                        '1' -> bits = bits or (BigInteger.ONE shl i)
                        else -> variables[chr] = variables.getOrDefault(chr, BigInteger.ZERO) or (BigInteger.ONE shl i)
                    }
                }
            variables.forEach { variableReferences[it.key]?.width = it.value.bitCount() }
            return InstructionBits(bits, variables)
        }
    }
}

class InstructionVariable(val name: Char, var width: Int) {
    companion object {
        fun from(words: List<AssemblyWord>): Map<Char, InstructionVariable> {
             return words.filterIsInstance<VariableAssemblyWord>().associate { it.name to InstructionVariable(it.name, 0) }
        }
    }
}
