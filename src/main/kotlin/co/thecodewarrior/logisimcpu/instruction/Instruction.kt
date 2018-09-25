package co.thecodewarrior.logisimcpu.instruction

import co.thecodewarrior.logisimcpu.instruction.ControlUnitWire.*

class Instruction(name: String, val opcode: UShort, conf: Instruction.() -> Unit) {
    val words = mutableListOf<Map<String, UShortArray>>()

    val steps: MutableList<InstructionStep> = mutableListOf()

    fun matches(words: List<String>): Boolean {
        if(words.size != this.words.size) return false
        words.zip(this.words).forEach { (word, map) ->
            if(word[0].isDigit())
                map["<>"] ?: return false
            else
                map[word] ?: return false
        }
        return true
    }

    init {
        words.add(mapOf(name to ushortArrayOf()))
        this.conf()
        step(LOAD_PROG, STORE_INSN, PROG_NEXT, INSN_END)
    }

    fun step(vararg wires: ControlUnitWire) {
        steps.add(InstructionStep(*wires))
    }

    fun amend(vararg wires: ControlUnitWire) {
        steps.last().wires.addAll(wires)
    }

    fun word(word: String) {
        this.words.add(mapOf(word to ushortArrayOf()))
    }

    fun arg() {
        this.words.add(mapOf("<>" to ushortArrayOf()))
    }

    fun payload(payload: Map<String, UShortArray>) {
        this.words.add(payload)
    }

    fun toROM(): List<UInt> {
        return steps.map { step ->
            var v = 0u
            step.wires.forEach {
                v = v or (1u shl it.ordinal)
            }
            v
        }
    }
}