package co.thecodewarrior.logisimcpu.instruction

data class InstructionStep(val wires: MutableSet<ControlUnitWire>) {
    constructor(vararg wires: ControlUnitWire) : this(wires.toMutableSet())
}

enum class ControlUnitWire {
    INSN_END,
    PROG_NEXT,
    LOAD_PROG,
    HALT,

    LOAD_A,
    LOAD_B,
    LOAD_ALU,

    STORE_A,
    STORE_B,
    STORE_INSN,
    STORE_DISPLAY
}