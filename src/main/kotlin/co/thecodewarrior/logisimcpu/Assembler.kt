package co.thecodewarrior.logisimcpu

import co.thecodewarrior.logisimcpu.instruction.Instructions
import java.util.LinkedList

class Assembler(val assembly: String) {
    var hexFile = HexFile()
    var addr = 0L

    private fun push(value: UByte) {
        hexFile[addr] = value.toLong() and 0xff
        addr++
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
        val name = line.peek()?.let { if(it[0] in "0123456789") null else line.poll() }
        val qualifier = line.peek()?.let { if(it[0] in "0123456789") null else line.poll() }
        val insn = name?.let { Instructions.find(name, qualifier) }

        if(insn != null) {
            push(insn.opcode)
        } else if(name != null) {
            if (qualifier != null) {
                throw AssemblyParseException("Unable to find instruction named $name with qualifier $qualifier")
            } else {
                throw AssemblyParseException("Unable to find instruction named $name")
            }
        }

        generateSequence { line.poll() }.map {
            when {
                it.startsWith("0x") -> it.removePrefix("0x").replace("_", "").toInt(16)
                it.startsWith("0o") -> it.removePrefix("0o").replace("_", "").toInt(8)
                it.startsWith("0b") -> it.removePrefix("0b").replace("_", "").toInt(2)
                else -> it.replace("_", "").toInt()
            }
        }.forEach {
            push(it.toUByte())
        }
    }
}

class AssemblyParseException(message: String, cause: Throwable? = null): Exception(message, cause)