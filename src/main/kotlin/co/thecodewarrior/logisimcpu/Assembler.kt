package co.thecodewarrior.logisimcpu

import co.thecodewarrior.logisimcpu.instruction.Instructions
import java.util.LinkedList

class Assembler(val assembly: String) {
    var hexFile = HexFile()
    var addr = 0L

    private fun push(value: UShort) {
        hexFile[addr] = value.toLong()
        addr++
    }

    private fun push(value: Iterable<UShort>) {
        value.forEach {
            push(it)
        }
    }

    fun assemble(): HexFile {
        hexFile = HexFile()
        addr = 0L

        val lines: Sequence<Pair<Int, LinkedList<String>>> = assembly.lineSequence()
            .mapIndexed { index, s -> index to s }
            .map { it.first to it.second.split("#").first() }
            .filter { it.second.isNotBlank() }
            .map { it.first to LinkedList( it.second.split("\\s+".toRegex()) ) }

        lines.forEach { (lineNumber, line) ->
            val fullLine = line.toList()
            try {
                parseInsn(line)
            } catch (e: Exception) {
                throw AssemblyParseException("Error parsing instruction on line $lineNumber, " +
                    "near ${fullLine[fullLine.size - line.size]}", e)
            }
        }

        return hexFile
    }

    fun parseInsn(line: LinkedList<String>) {
//        if(line.peek()?.endsWith(":") == true)
//            pushLabel(line.poll().removeSuffix(":"))
        val insn = Instructions.instructions.first { it.matches(line) }

        push(insn.opcode)
        line.zip(insn.words).forEach { (lineWord, insnWord) ->
            line.poll()
            when {
                lineWord.startsWith("0x") -> push(lineWord.removePrefix("0x").replace("_", "").toUShort(16))
                lineWord.startsWith("0o") -> push(lineWord.removePrefix("0o").replace("_", "").toUShort(8))
                lineWord.startsWith("0b") -> push(lineWord.removePrefix("0b").replace("_", "").toUShort(2))
                lineWord[0].isDigit() -> push(lineWord.replace("_", "").toUShort())
                else -> push(insnWord[lineWord]!!)
            }
        }
    }
}

class AssemblyParseException(message: String, cause: Throwable? = null): Exception(message, cause)