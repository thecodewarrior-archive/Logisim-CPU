package co.thecodewarrior.logisimcpu.microcode

import co.thecodewarrior.logisimcpu.bitCount
import co.thecodewarrior.logisimcpu.lowestSetBit
import co.thecodewarrior.logisimcpu.parseIntLike
import co.thecodewarrior.logisimcpu.parseUIntLikeOrNull
import java.io.File
import java.util.LinkedList

data class Instruction(val assembly: List<AssemblyWord>, val variables: Map<Char, InstructionVariable>, val bits: InstructionBits, val steps: List<InstructionStep>) {

    fun opcode(variableValues: Map<Char, UInt>): UInt {
        var binary = this.bits.bits
        this.bits.variables.forEach { variable ->
            var value = variableValues[variable.key] ?: 0u
            var mask = variable.value
            while(mask.lowestSetBit >= 0) {
                val bit = value and 1u
                binary = binary or (bit shl mask.lowestSetBit)
                mask = mask xor (1u shl mask.lowestSetBit)
                value = value shr 1
            }
        }
        return binary
    }

    companion object {
        fun parse(file: File, controlLinesList: List<ControlLine>): List<Instruction> {
            val controlLines = controlLinesList.associateBy { it.name }
            val enums = mutableMapOf<String, Map<String, UInt>>()
            return file.readLines().asSequence()
                .map { it.split("#").first().trim() }
                .filter { it.isNotBlank() }
                .toList().let { LinkedList(it) }
                .let { lines ->
                    val instructions = mutableListOf<Instruction>()
                    var lastBits: InstructionBits? = null
                    while(lines.peek() != null) {
                        if(lines.peek().startsWith(":")) {
                            val wordsBits = lines.pop().removePrefix(":").split("=")
                            val words = AssemblyWord.parse(wordsBits[0], enums)
                            val variables = InstructionVariable.from(words)
                            lastBits = InstructionBits.parse(wordsBits[1], variables, lastBits)
                            val steps = lines.takeWhile { it.startsWith("-") }
                                .map { InstructionStep.parse(it.removePrefix("-"), controlLines, variables) }
                            steps.forEach { lines.pop() }
                            instructions.add(Instruction(words, variables, lastBits, steps))
                        } else if(lines.peek().startsWith("enum") && lines.peek().endsWith("{")) {
                            val name = lines.pop().removePrefix("enum").removeSuffix("{").trim()
                            val enum = lines.takeWhile { !it.startsWith("}") }
                                .associate { it.split("=").let { it[0] to it[1].parseIntLike().toUInt() } }
                            enum.forEach { lines.pop() }
                            lines.pop() // pop the trailing }
                            enums[name] = enum
                        } else {
                            throw RuntimeException("illegal line ${lines.pop()}")
                        }
                    }
                    instructions
                }
        }
    }
}

data class InstructionStep(val lines: List<StepControlLine>) {
    companion object {
        fun parse(text: String, controlLines: Map<String, ControlLine>, variables: Map<Char, InstructionVariable>): InstructionStep {
            val lines = text.trim().split("\\s+".toRegex())
                .map { StepControlLine.parse(it, controlLines, variables) }
            return InstructionStep(lines)
        }
    }
}

open class StepControlLine(val line: ControlLine) {
    companion object {
        fun parse(text: String, controlLines: Map<String, ControlLine>, variables: Map<Char, InstructionVariable>): StepControlLine {
            val regex = """(\w+)(?:<(\w)>)?""".toRegex()
            val match= regex.matchEntire(text.trim())
                ?: throw RuntimeException("Control line doesn't match: `$text`")
            val (lineName, value) = match.destructured
            val line = controlLines[lineName]
                ?: throw RuntimeException("Couldn't find control line: `$lineName`")
            val constant = value.parseUIntLikeOrNull()
            val variable = value.firstOrNull()?.let { variables[it] }
            if(constant != null) {
                return ConstantStepControlBundle(line, constant)
            } else if(variable != null) {
                return VariableStepControlBundle(line, variable)
            } else {
                return StepControlLine(line)
            }
        }
    }
}

class VariableStepControlBundle(line: ControlLine, val variable: InstructionVariable): StepControlLine(line) {

}

class ConstantStepControlBundle(line: ControlLine, val const: UInt): StepControlLine(line) {

}

sealed class AssemblyWord {
    abstract fun matches(value: String): Boolean

    companion object {
        fun parse(text: String, enums: Map<String, Map<String, UInt>>): List<AssemblyWord> {
            return text.trim().split("\\s+".toRegex())
                .map {
                    if(it.startsWith("<") && it.endsWith(">")) {
                        val (name, enumName) = "(.)(?::(\\w+))?".toRegex()
                            .matchEntire(it.removePrefix("<").removeSuffix(">"))!!.destructured
                        VariableAssemblyWord(name[0], if(enumName.isEmpty()) null else enums[enumName]!!)
                    } else {
                        FixedAssemblyWord(it)
                    }
                }
        }
    }
}

data class FixedAssemblyWord(val text: String): AssemblyWord() {
    override fun matches(value: String) = value == text
}

data class VariableAssemblyWord(val name: Char, val enum: Map<String, UInt>?): AssemblyWord() {
    override fun matches(value: String) = value[0].isDigit() || value[0] == '>' || (enum?.let { value in it } ?: false)
}

data class InstructionBits(val bits: UInt, val paramWidth: Int, val variables: Map<Char, UInt>, val payload: List<InstructionPayload>) {
    companion object {
        fun parse(text: String, variableReferences: Map<Char, InstructionVariable>, lastBits: InstructionBits?): InstructionBits {
            val variables = mutableMapOf<Char, UInt>()
            var bits = 0u
            val words = text.split(";")
            val paramWidth: Int
            words[0].replace("\\s+".toRegex(), "").let {
                paramWidth = it.length - (it.indexOf('|')+1)
                if(it.startsWith("@")) {
                    (lastBits?.let { (it.bits shr it.paramWidth) + 1u } ?: 0u).toString(2) + it.removePrefix("@").replace("|","")
                } else {
                    it.replace("|", "")
                }
            }.reversed().forEachIndexed { i, chr ->
                when(chr) {
                    '0' -> {}
                    '1' -> bits = bits or (1u shl i)
                    else -> variables[chr] = variables.getOrDefault(chr, 0u) or (1u shl i)
                }
            }
            variables.forEach {
                if(it.value.lowestSetBit >= paramWidth)
                    variableReferences[it.key]?.isInline = true
                variableReferences[it.key]?.width = it.value.bitCount
            }
            return InstructionBits(bits, paramWidth, variables, words.drop(1).map { InstructionPayload.parse(it) })
        }
    }
}

sealed class InstructionPayload {
    companion object {
        fun parse(text: String): InstructionPayload {
            return text.trim().let {
                if(it.startsWith("<") && it.endsWith(">"))
                    VariableInstructionPayload(it[1])
                else
                    FixedInstructionPayload(it.toUInt(2))
            }
        }
    }
}

data class FixedInstructionPayload(val value: UInt): InstructionPayload()
data class VariableInstructionPayload(val name: Char): InstructionPayload()

data class InstructionVariable(val name: Char, var width: Int, var enum: Map<String, UInt>?) {
    var isInline: Boolean = false
    companion object {
        fun from(words: List<AssemblyWord>): Map<Char, InstructionVariable> {
            return words.filterIsInstance<VariableAssemblyWord>().associate { it.name to InstructionVariable(it.name, 0, it.enum) }
        }
    }
}
