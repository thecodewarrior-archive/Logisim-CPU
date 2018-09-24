package co.thecodewarrior.logisimcpu.instruction

import co.thecodewarrior.logisimcpu.HexFile
import co.thecodewarrior.logisimcpu.instruction.ControlUnitWire.*

object Instructions {
    val instructions: MutableMap<UByte, Instruction> = mutableMapOf()

    init {
        (0b0000_0000 - "!!BOOTSTRAP!!" % {}).apply {
            steps.clear()
            steps.add(InstructionStep(INSN_END, LOAD_PROG, STORE_INSN))
        }
        0b0000_0001 - "NOP" % {}
        0b0000_0010 - "HALT" % {
            step(HALT)
        }
        (0b0001_0000 to 0b0001_1111) - "STORE_A" % {
            memsel(this)
            amend(STORE_A)
        }
        (0b0010_0000 to 0b0010_1111) - "STORE_B" % {
            memsel(this)
            amend(STORE_B)
        }
        (0b0011_0000 to 0b0011_1111) - "DISPLAY" % {
            memsel(this)
            amend(STORE_DISPLAY)
        }
        (0b0100_0000 to 0b0100_1111) - "ALU_COMMIT" % {
            step(COMMIT_ALU)
            val opcodeI = opcode.toUInt() and 0b1111u
            if(opcodeI and 0b0001u != 0u) amend(ALU_OP_1s)
            if(opcodeI and 0b0010u != 0u) amend(ALU_OP_2s)
            if(opcodeI and 0b0100u != 0u) amend(ALU_OP_4s)
            if(opcodeI and 0b1000u != 0u) amend(ALU_OP_8s)
            qualifier = listOf(
                "ADD",
                "SUB",
                "MUL",
                "DIV",
                "NEG",
                "SHL",
                "SHR",
                "CNT",
                "CMPEQ",
                "CMPNEQ",
                "CMPLT",
                "CMPGT",
                "NOT",
                "AND",
                "OR",
                "XOR"
            ).getOrNull(opcodeI.toInt())
        }
    }

    private fun memsel(insn: Instruction) {
        when(insn.opcode.toUInt() and 0b0000_1111u) {
            0b0000u -> {
                insn.qualifier = "REG_A"
                insn.step(LOAD_A)
            }
            0b0001u -> {
                insn.qualifier = "REG_B"
                insn.step(LOAD_B)
            }
            0b0010u -> {
                insn.qualifier = "ALU"
                insn.step(LOAD_ALU)
            }
            0b0011u -> {
                insn.qualifier = "PROG"
                insn.step(PROG_NEXT)
                insn.step(LOAD_PROG)
            }
            else -> {
                insn.qualifier = "!!ERR!!"
                insn.step(FAULT, HALT)
            }
        }
    }

    fun controlUnitROM(): HexFile {
        val file = HexFile()
        instructions.forEach { (opcode, insn) ->
            val offset = opcode.toULong().toLong() shl 8
            insn.toROM().forEachIndexed { i, step ->
                file[offset + i] = step
            }
        }
        return file
    }

    private operator fun Int.minus(other: PartialInstruction): Instruction {
        val instruction = Instruction(other.name, this.toUByte(), other.conf)
        instructions[instruction.opcode] = instruction
        return instruction
    }

    private operator fun Pair<Int, Int>.minus(other: PartialInstruction): Map<UByte, Instruction> {
        val map = mutableMapOf<UByte, Instruction>()
        for(opcode in this.first .. this.second) {
            val instruction = Instruction(other.name, opcode.toUByte(), other.conf)
            map[instruction.opcode] = instruction
        }
        instructions.putAll(map)
        return map
    }

    private operator fun String.rem(other: Instruction.() -> Unit): PartialInstruction {
        return PartialInstruction(this, other)
    }

    fun find(name: String, qualifier: String?): Instruction? {
        return instructions.values.find { it.name == name && it.qualifier == qualifier }
    }

    private class PartialInstruction(val name: String, val conf: Instruction.() -> Unit)
}