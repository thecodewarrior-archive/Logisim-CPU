package co.thecodewarrior.logisimcpu.instruction

data class InstructionStep(val wires: MutableSet<ControlUnitWire>) {
    constructor(vararg wires: ControlUnitWire) : this(wires.toMutableSet())
}

@Suppress("EnumEntryName")
enum class ControlUnitWire {
    HALT,
    FAULT,

    INSN_END,
    PROG_NEXT,
    LOAD_PROG,
    STORE_INSN,
    JMP,
    JMP_IF,
    JMP_IFN,

    LOAD_A,
    LOAD_B,
    LOAD_INTERNAL_A,
    LOAD_FLAG_A,
    LOAD_FLAG_B,
    LOAD_ALU,
    LOAD_ALU_FLAG,

    STORE_A,
    STORE_B,
    STORE_INTERNAL_A,
    STORE_FLAG_A,
    STORE_FLAG_B,
    STORE_ALU_OP,
    STORE_DISPLAY,

    I2B,
    B2I,
}