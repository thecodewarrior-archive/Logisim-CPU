package co.thecodewarrior.logisimcpu

import co.thecodewarrior.logisimcpu.microcode.ControlLine
import co.thecodewarrior.logisimcpu.microcode.Instruction
import co.thecodewarrior.logisimcpu.microcode.Microcode
import java.io.File

fun main(args: Array<String>) {
    val microcode = Microcode(File("definitions/v2/"))
    microcode.microcodes()
}
