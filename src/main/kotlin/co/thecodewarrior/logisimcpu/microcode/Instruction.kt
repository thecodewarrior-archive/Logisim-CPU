package co.thecodewarrior.logisimcpu.microcode

import java.io.File
import java.util.Deque
import java.util.LinkedList

data class Instruction(val assembly: List<AssemblyWord>, val steps: List<InstructionStep>) {
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
                        val words = AssemblyWord.parse(lines.pop().removePrefix(":"))
                        val steps = lines.takeWhile { it.startsWith("-") }
                            .map { InstructionStep.parse(it.removePrefix("-"), controlLines) }
                        steps.forEach { _ -> lines.pop() }
                        instructions.add(Instruction(words, steps))
                    }
                    instructions
                }
        }
    }
}

data class InstructionStep(val lines: List<ParameterizedControlLine>) {
    companion object {
        fun parse(text: String, controlLines: Map<String, ControlLine>): InstructionStep {
            val lines = text.trim().split("\\s+".toRegex())
                .map { ParameterizedControlLine.parse(it, controlLines) }
            return InstructionStep(lines)
        }
    }
}

data class ParameterizedControlLine(val line: ControlLine, val variable: String?) {
    companion object {
        fun parse(text: String, controlLines: Map<String, ControlLine>): ParameterizedControlLine {
            val regex = """(\w+)(?:<(\w+)>)?""".toRegex()
            val matches = regex.matchEntire(text.trim())?.groupValues
                ?: throw RuntimeException("Control line doesn't match: `$text`")
            val line = controlLines[matches[1]]
                ?: throw RuntimeException("Couldn't find control line: `${matches[1]}`")
            return ParameterizedControlLine(line, matches[2].let { if(it.isEmpty()) null else it })
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
                        VariableAssemblyWord(it.removePrefix("<").removeSuffix(">"))
                    else
                        FixedAssemblyWord(it)
                }
        }
    }
}
data class FixedAssemblyWord(val text: String): AssemblyWord() {
    override fun matches(value: String) = value == text
}
data class VariableAssemblyWord(val name: String): AssemblyWord() {
    override fun matches(value: String) = value[0].isDigit() || value[0] == '>'
}
