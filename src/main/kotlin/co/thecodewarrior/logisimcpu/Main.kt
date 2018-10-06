package co.thecodewarrior.logisimcpu

import co.thecodewarrior.logisimcpu.microcode.Microcode
import java.io.File

fun main(args: Array<String>) {
    val cpu = "v2"
    File("out/$cpu").mkdirs()

    val microcode = Microcode(File("definitions/$cpu/"), 8)
    File("out/$cpu/microcode.hex").writeText(microcode.microcode())
}
