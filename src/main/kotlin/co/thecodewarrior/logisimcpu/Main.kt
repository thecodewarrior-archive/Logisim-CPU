package co.thecodewarrior.logisimcpu

import co.thecodewarrior.logisimcpu.microcode.ControlLine
import co.thecodewarrior.logisimcpu.microcode.Instruction
import java.io.File

fun main(args: Array<String>) {
    val controlLines = ControlLine.controlLines(File("definitions/v2/controls.txt"))
    println(controlLines)
    val instructions = Instruction.parse(File("definitions/v2/instructions.txt"), controlLines)
    println(instructions.joinToString("\n"))
}
