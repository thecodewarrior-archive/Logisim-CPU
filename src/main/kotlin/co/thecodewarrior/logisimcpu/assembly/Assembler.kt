package co.thecodewarrior.logisimcpu.assembly

import co.thecodewarrior.logisimcpu.CPU
import co.thecodewarrior.logisimcpu.microcode.FixedAssemblyWord
import co.thecodewarrior.logisimcpu.microcode.FixedInstructionPayload
import co.thecodewarrior.logisimcpu.microcode.Instruction
import co.thecodewarrior.logisimcpu.microcode.VariableAssemblyWord
import co.thecodewarrior.logisimcpu.microcode.VariableInstructionPayload
import java.io.File

class Assembler(val file: File, val cpu: CPU) {

    fun parse() {
        return file.readLines().asSequence()
            .map { it.split("#").first().trim() }
            .filter { it.isNotBlank() }
            .forEach { line ->
                parse(line)
            }
    }

    private val statements = mutableListOf<Pair<ASMLabel?, List<ASMWord>>>()
    private var currentLabel: ASMLabel? = null
    private var labels = mutableMapOf<String, ASMLabel>()

    fun parse(line: String) {
        if(line.isBlank()) return
        val referenceRegex = """>(\w+)(?:([+-])(0[xob])?(\d+))?""".toRegex()
        val numberRegex = """(-)?(0[xob])?(\d+)""".toRegex()
        val labelRegex = """^(?:(\w+):)?(.*)""".toRegex()

        val (label, insn) = labelRegex.matchEntire(line)!!.destructured
        if(label.isNotEmpty()) {
            currentLabel = labels.getOrPut(label) { ASMLabel(label) }
        }

        if(insn.isBlank()) return
        val wordStrings = insn.split("\\s+".toRegex())
        statements.add(currentLabel to wordStrings.map {

            val numberMatch = numberRegex.matchEntire(it)
            if(numberMatch != null) {
                val (sign, prefix, digits) = numberMatch.destructured
                var value =
                    when(prefix) {
                        "0x" -> digits.toInt(16)
                        "0o" -> digits.toInt(8)
                        "0b" -> digits.toInt(2)
                        else -> digits.toInt()
                    }
                if(sign.isNotEmpty()) value *= -1
                return@map ASMWordConstant(value.toUInt())
            }

            val referenceMatch = referenceRegex.matchEntire(it)
            if(referenceMatch != null) {
                val (label, sign, prefix, digits) = referenceMatch.destructured
                var value =
                    when(prefix) {
                        "0x" -> digits.toInt(16)
                        "0o" -> digits.toInt(8)
                        "0b" -> digits.toInt(2)
                        else -> digits.toInt()
                    }
                if(sign == "-") value *= -1
                return@map ASMWordReference(labels.getOrPut(label) { ASMLabel(label) }, value)
            }

            return@map ASMWordText(it)
        })
        currentLabel = null
    }

    fun assemble(): List<UInt> {
        val words = statements.map {
            match(it.first, it.second)
        }.map { words(it) }
        var i = 0u
        words.forEach { statement ->
            statement.forEach { word ->
                if(word is MachineWordInsn) {
                    word.statement.label?.also { it.location = i }
                    i++
                }
            }
        }
        return words.flatMap { it }.map { it.word }
    }

    fun write(): String {
        return "v2.0 raw\n" + assemble().joinToString(" ") { it.toString(16) }
    }

    fun match(label: ASMLabel?, statement: List<ASMWord>): ASMStatement {
        for(insn in cpu.instructions) {
            match(insn, statement)?.let {
                return ASMStatement(label, insn, it)
            }
        }
        throw RuntimeException("could not match statement $statement")
    }

    fun match(insn: Instruction, statement: List<ASMWord>): Map<Char, ASMWordValue>? {
        if (insn.assembly.size != statement.size) return null
        val match = mutableMapOf<Char, ASMWordValue>()
        insn.assembly.zip(statement).forEach { (insnWord, asmWord) ->
            if(insnWord is FixedAssemblyWord && asmWord is ASMWordText) {
                if(insnWord.text != asmWord.text) return null
            } else if(insnWord is VariableAssemblyWord && asmWord is ASMWordValue) {
                match[insnWord.name] = asmWord
            } else {
                return null
            }
        }
        return match
    }

    fun words(statement: ASMStatement): List<MachineWord> {
        val words = mutableListOf<MachineWord>()
        words.add(MachineWordInsn(statement))
        statement.insn.bits.payload.forEach {
            when (it) {
                is FixedInstructionPayload -> words.add(MachineWordConst(it.value))
                is VariableInstructionPayload -> words.add(MachineWordVariable(statement.variables[it.name]!!))
            }
        }
        return words
    }
}

sealed class ASMWord
data class ASMWordText(val text: String): ASMWord()
abstract class ASMWordValue: ASMWord() {
    abstract val value: UInt
}
data class ASMWordConstant(override val value: UInt): ASMWordValue()
data class ASMWordReference(val label: ASMLabel, val offset: Int): ASMWordValue() {
    override var value: UInt = label.location
}

data class ASMLabel(val name: String) {
    var location: UInt = 0u
}

data class ASMStatement(val label: ASMLabel?, val insn: Instruction, val variables: Map<Char, ASMWordValue>)

sealed class MachineWord {
    abstract val word: UInt
}

data class MachineWordInsn(val statement: ASMStatement): MachineWord() {
    override val word: UInt
        get() = statement.insn.opcode(statement.variables.mapValues { it.value.value })
}

data class MachineWordVariable(val asmWord: ASMWordValue): MachineWord() {
    override val word: UInt
        get() = asmWord.value
}

data class MachineWordConst(override val word: UInt): MachineWord()
