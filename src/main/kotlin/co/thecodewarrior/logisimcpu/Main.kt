package co.thecodewarrior.logisimcpu

import co.thecodewarrior.logisimcpu.assembly.Assembler
import co.thecodewarrior.logisimcpu.microcode.Microcode
import java.io.File

fun main(args: Array<String>) {
    val name = "v2"
    val cpu = CPU(File("definitions/$name"), File("out/$name"))
    cpu.output.mkdirs()
    cpu.exportMicrocode()
    val assembler = Assembler(File("program.asm"), cpu)
    assembler.parse()
    File("program.hex").writeText(assembler.write())
}
