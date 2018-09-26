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

    private var skipEnd = false
    var amendEnd = false

    init {
        words.addAll(name.split(" ").map { mapOf(it to ushortArrayOf()) })
        this.conf()
        if(!skipEnd) {
            if(amendEnd)
                amend(NEXT_INSN)
            else
                step(NEXT_INSN)
        }

        words[0].let { map ->
            val mutable = map.toMutableMap()
            map.forEach { (key, value) ->
                mutable[key] = ushortArrayOf(opcode, *value)
            }
            words[0] = mutable
        }
    }

    fun step(vararg wires: ControlUnitWire) {
        steps.add(InstructionStep(*wires))
    }

    fun amend(vararg wires: ControlUnitWire) {
        steps.last().wires.addAll(wires)
    }

    /**
     * Set a custom ending instruction. If this is called the default end will not be added
     */
    fun end(vararg wires: ControlUnitWire) {
        steps.add(InstructionStep(*wires))
        skipEnd = true
    }

    fun amendEnd() {
        this.amendEnd = true
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

    fun microcode(): List<ULong> {
        return steps.map { step ->
            var v = 0uL
            step.wires.forEach {
                v = v or (1uL shl it.ordinal)
            }
            v
        }
    }

    //
    fun microcodeText(): String {
        val lines = steps.mapIndexed { index, step ->
            "    $index: ${step.wires.joinToString(" ")}"
        }.joinToString("\n")

        val name = words.joinToString(" ") {
            if(it.size > 1) {
                "..."
            } else {
                it.keys.first()
            }
        } + " (0x${opcode.toString(16)})"

        return "# $name\n$lines"

//        var combos = words.first().entries.map { it.key to it.value.toList() }
//        words.drop(1).forEach { map ->
//            val mutable = mutableListOf<Pair<String, List<UShort>>>()
//            val comboMaxWidth = combos.maxBy { it.first.length }?.first?.length ?: 0
//            map.forEach { (word, payload) ->
//                combos.forEach { combo ->
//                    mutable.add((combo.first.padEnd(comboMaxWidth) + " " + word) to (combo.second + payload))
//                }
//            }
//            combos = mutable
//        }
//        val nameWidth = combos.maxBy { it.first.length }!!.first.length
//        val hexWidth = combos.map { it.second.map { it.toString(16).length }.max() ?: 0 }.max()!!
//        var str = ""
//        combos.forEach { combo ->
//            str += combo.first.padEnd(nameWidth) + " > " + combo.second.map { it.toString(16).padStart(hexWidth) }.joinToString(" ") + "\n"
//        }
//        return str
    }
}
