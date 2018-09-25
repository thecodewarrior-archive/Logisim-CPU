package co.thecodewarrior.logisimcpu.instruction

data class InstructionStep(val wires: MutableSet<ControlUnitWire>) {
    constructor(vararg wires: ControlUnitWire) : this(wires.toMutableSet())
}

@Suppress("EnumEntryName")
enum class ControlUnitWire {
    INSN_END,
    PROG_NEXT,
    LOAD_PROG,
    HALT,
    FAULT,

    LOAD_A,
    LOAD_B,
    LOAD_ALU,
    LOAD_INTERNAL_A,

    STORE_A,
    STORE_B,
    STORE_INSN,
    STORE_DISPLAY,
    STORE_ALU_OP,
    STORE_INTERNAL_A
}