package co.thecodewarrior.logisimcpu

import co.thecodewarrior.logisimcpu.microcode.ControlLine
import co.thecodewarrior.logisimcpu.microcode.Instruction
import co.thecodewarrior.logisimcpu.microcode.Microcode
import java.io.File

class CPU(val definitions: File, val output: File) {
    val controlLines = ControlLine.controlLines(definitions.resolve("controls.txt"))
    val instructions = Instruction.parse(definitions.resolve("instructions.txt"), controlLines)

    fun exportMicrocode() {
        val microcode = Microcode(this, 8)
        output.resolve("microcode.hex").writeText(microcode.microcode())
    }
}