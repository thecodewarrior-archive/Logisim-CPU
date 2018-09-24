package co.thecodewarrior.logisimcpu.instruction

import co.thecodewarrior.logisimcpu.instruction.ControlUnitWire.*

class Instruction(val name: String, val opcode: UByte, conf: Instruction.() -> Unit) {
    var qualifier: String? = null

    val steps: MutableList<InstructionStep> = mutableListOf()

    init {
        this.conf()
        step(PROG_NEXT)
        step(LOAD_PROG, STORE_INSN, INSN_END)
    }

    fun step(vararg wires: ControlUnitWire) {
        steps.add(InstructionStep(*wires))
    }

    fun amend(vararg wires: ControlUnitWire) {
        steps.last().wires.addAll(wires)
    }

    fun toROM(): List<Long> {
        return steps.map { step ->
            var v = 0L
            step.wires.forEach {
                v = v or (1L shl it.ordinal)
            }
            v
        }
    }
}