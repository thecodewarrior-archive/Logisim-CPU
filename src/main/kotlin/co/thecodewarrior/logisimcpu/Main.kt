package co.thecodewarrior.logisimcpu

import co.thecodewarrior.logisimcpu.instruction.Instructions
import java.io.File

fun main(args: Array<String>) {
    Instructions.microcodeROM().forEachIndexed { i, hex ->
        File("microcode$i.hex").writeText(hex.toString())
    }
    File("microcode.txt").writeText(Instructions.microcodeText())
    File("program.hex").writeText(Assembler(File("program.asm").readText()).assemble().toString())
}
