package co.thecodewarrior.logisimcpu

import co.thecodewarrior.logisimcpu.instruction.Instructions
import java.io.File

fun main(args: Array<String>) {
    File("controlUnit.hex").writeText(Instructions.controlUnitROM().toString())
    File("program.hex").writeText(Assembler(File("program.asm").readText()).assemble().toString())
}
