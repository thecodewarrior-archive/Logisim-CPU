package co.thecodewarrior.logisimcpu

import co.thecodewarrior.logisimcpu.instruction.Instructions
import java.io.File

fun main(args: Array<String>) {
    val microcode = Instructions.microcodeROM()
    File("microcode0.hex").writeText(microcode.first.toString())
    File("microcode1.hex").writeText(microcode.second.toString())
    File("program.hex").writeText(Assembler(File("program.asm").readText()).assemble().toString())
}
