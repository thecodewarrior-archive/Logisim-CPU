package co.thecodewarrior.logisimcpu.instruction

data class InstructionStep(val wires: MutableSet<ControlUnitWire>) {
    constructor(vararg wires: ControlUnitWire) : this(wires.toMutableSet())
}
