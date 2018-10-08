package co.thecodewarrior.logisimcpu.microcode

import co.thecodewarrior.logisimcpu.parseUIntLike
import java.io.File
import java.util.LinkedList

data class Instruction(val name: String, val bits: UInt, val steps: List<InstructionStep>) {

    companion object {
        fun parse(file: File, controlLinesList: List<ControlLine>): List<Instruction> {
            val controlLines = controlLinesList.associateBy { it.name }
            return readFile(file).asSequence()
                .map { it.split("#").first().trim() }
                .filter { it.isNotBlank() }
                .toList().let { LinkedList(it) }
                .let { lines ->
                    val instructions = mutableListOf<Instruction>()
                    while(lines.peek() != null) {
                        if(lines.peek().startsWith(":")) {
                            val (name, bits) = lines.pop().removePrefix(":").split("=").map { it.trim() }
                            val steps = lines.takeWhile { it.startsWith("-") }
                                .map { lines.pop(); InstructionStep.parse(it.removePrefix("-"), controlLines) }
                            instructions.add(Instruction(name, bits.parseUIntLike(), steps))
                        } else {
                            throw RuntimeException("illegal line ${lines.pop()}")
                        }
                    }
                    instructions
                }
        }

        private fun readFile(file: File): List<String> {
            val allLines = mutableListOf<String>()
            val fileLines = file.readLines()
            fileLines.forEach {
                if(it.startsWith("#include ")) {
                    val includedName = it.removePrefix("#include ")
                    val includedFile = file.resolveSibling(includedName)
                    allLines.addAll(readFile(includedFile))
                } else {
                    allLines.add(it)
                }
            }
            return allLines
        }
    }
}

data class InstructionStep(val lines: List<StepControlLine>) {
    companion object {
        fun parse(text: String, controlLines: Map<String, ControlLine>): InstructionStep {
            val lines = text.trim().split("\\s+".toRegex())
                .map { StepControlLine.parse(it, controlLines) }
            return InstructionStep(lines)
        }
    }
}

open class StepControlLine(val line: ControlLine) {
    companion object {
        fun parse(text: String, controlLines: Map<String, ControlLine>): StepControlLine {
            val regex = """(\w+)(?:<(\w+)>)?""".toRegex()
            val match= regex.matchEntire(text.trim())
                ?: throw RuntimeException("Control line doesn't match: `$text`")
            val (lineName, value) = match.destructured
            val line = controlLines[lineName]
                ?: throw RuntimeException("Couldn't find control line: `$lineName`")
            if(value.isNotEmpty()) {
                return ConstantStepControlBundle(line, value.parseUIntLike())
            } else {
                return StepControlLine(line)
            }
        }
    }
}

class ConstantStepControlBundle(line: ControlLine, val const: UInt): StepControlLine(line)
