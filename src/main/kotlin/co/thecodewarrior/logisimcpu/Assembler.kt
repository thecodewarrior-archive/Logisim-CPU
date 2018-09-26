package co.thecodewarrior.logisimcpu

import co.thecodewarrior.logisimcpu.instruction.Instructions
import java.lang.IllegalArgumentException
import java.util.LinkedList
import java.util.TreeMap

class Assembler(val assembly: String) {
    var hexFile = HexFile()
    val labels = mutableMapOf<String, Long>()
    val labelRequests = mutableMapOf<Long, String>()
    val lineAddresses = TreeMap<Long, Int>()
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

    private fun pushLabel(name: String) {
        if(name in labels) throw IllegalArgumentException("Label $name already exists")
        labels[name] = addr
    }

    private fun requestLabel(name: String) {
        labelRequests[addr] = name
        addr++
    }

    fun assemble(): HexFile {
        hexFile = HexFile()
        addr = 0L

        val lineStrings = assembly.lines()
        val lines: List<Pair<Int, List<String>>> = lineStrings
            .mapIndexed { index, s -> index to s }
            .map { it.first to it.second.split("#").first() }
            .filter { it.second.isNotBlank() }
            .map { it.first to it.second.split("\\s+".toRegex()).filter { it.isNotBlank() } }

        lines.forEach { (lineNumber, line) ->
            val linkedLine = LinkedList(line)
            try {
                lineAddresses[addr] = lineNumber
                parseInsn(linkedLine)
            } catch (e: Exception) {
                throw AssemblyParseException("Error parsing instruction on line $lineNumber, " +
                    "near ${line[line.size - line.size]}", e)
            }
        }
        labelRequests.forEach { (location, label) ->
            val labelLocation = labels[label]
                ?: throw AssemblyParseException("Unknown label $label on line ${lineAddresses.floorEntry(location).value}")
            hexFile[location] = labelLocation
        }

        val decompLines = mutableListOf<Pair<String, LongArray>>()
        lines.forEach { (lineNumber, line) ->
            val lineText = lineStrings[lineNumber]
            val start = lineAddresses.entries.find { it.value == lineNumber }?.key
            if(start == null) {
                decompLines.add(lineText to longArrayOf())
                return@forEach
            }
            val end = lineAddresses.higherEntry(start)?.key ?: addr
            decompLines.add(lineText to LongArray((end-start).toInt()) { hexFile[start+it] ?: 0L })
        }

        val width = decompLines.asSequence().map { it.first.length }.max() ?: 0
        val hexWidth = decompLines.asSequence().map { it.second.max() ?: 0 }.max()?.toString(16)?.length ?: 0
        var decompText = ""
        decompLines.forEach { (line, instructions) ->
            if(instructions.isEmpty()) {
                decompText += line + "\n"
            } else {
                val hex: String = instructions.map { it.toString(16).padStart(hexWidth, '0') }.joinToString(" ")
                decompText += line.padEnd(width, ' ') + " -> " + hex + "\n"
            }
        }
        println()
        print(decompText)
        println()

        return hexFile
    }

    fun parseInsn(rawLine: LinkedList<String>) {
        val line = LinkedList(rawLine
            .flatMap { if(it.startsWith(">")) listOf("CONST", it) else listOf(it) }) // add "CONST" before label references
        val matchLine = line
            .filter { !it.endsWith(":") } // remove labels
            .map { if(it.startsWith(">")) "0$it" else it } // replace label references with fake numbers

        val insn =
            if(matchLine.isEmpty()) // meaning only labels
                null
            else
                Instructions.instructions.firstOrNull { it.matches(matchLine) }
                    ?: throw AssemblyParseException("Could not find matching instruction for $matchLine")

        val insnWords = LinkedList(insn?.words ?: listOf())
        line.forEach { word ->
            if(word.endsWith(":")) {
                pushLabel(word.removeSuffix(":"))
                return@forEach
            }
            val insnWord = insnWords.poll()
            if(word.startsWith(">")) {
                requestLabel(word.removePrefix(">"))
                return@forEach
            }

            when {
                word.startsWith("0x") -> push(word.removePrefix("0x").replace("_", "").toUShort(16))
                word.startsWith("0o") -> push(word.removePrefix("0o").replace("_", "").toUShort(8))
                word.startsWith("0b") -> push(word.removePrefix("0b").replace("_", "").toUShort(2))
                word[0].isDigit() -> push(word.replace("_", "").toUShort())
                else -> push(insnWord[word]!!)
            }
        }
    }
}

class AssemblyParseException(message: String, cause: Throwable? = null): Exception(message, cause)