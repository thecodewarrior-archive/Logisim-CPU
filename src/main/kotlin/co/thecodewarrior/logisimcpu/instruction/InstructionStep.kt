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

    STORE_A,
    STORE_B,
    STORE_INSN,
    STORE_DISPLAY,
    COMMIT_ALU,
    ALU_OP_1s,
    ALU_OP_2s,
    ALU_OP_4s,
    ALU_OP_8s,
}