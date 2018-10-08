package co.thecodewarrior.logisimcpu

import java.io.File

fun main(args: Array<String>) {
    val name = "v2"
    val cpu = CPU(File("definitions/$name"), File("out/$name"))
    cpu.output.mkdirs()
    cpu.exportMicrocode()
}
