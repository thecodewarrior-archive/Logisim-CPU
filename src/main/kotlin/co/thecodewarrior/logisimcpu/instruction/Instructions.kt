package co.thecodewarrior.logisimcpu.instruction

import co.thecodewarrior.logisimcpu.HexFile
import co.thecodewarrior.logisimcpu.instruction.ControlUnitWire.*
import co.thecodewarrior.logisimcpu.instruction.Opcode.*

object Instructions {
    val instructions: MutableList<Instruction> = mutableListOf()

    init {
        insn(OP_BOOTSTRAP) {}

        insn(OP_HALT) {
            step(HALT)
        }

        insn(OP_NOP) {
        }

        insn(OP_SWAP) {
            step(SWAP_INT)
            amendEnd = true
        }

        insn(OP_SWAP_FLAGS) {
            step(SWAP_FLAGS)
            amendEnd = true
        }

        memSource(OP_SLEEP) { source  ->
            word(source.name)
            source(this)
            amend(SLEEP)
        }

        memSource(OP_JMP) { source  ->
            word(source.name)
            source(this)
            amend(JMP)
        }

        memSource(OP_JMP_IF) { source  ->
            word(source.name)
            source(this)
            amend(LOAD_FLAG_A, JMP_IF)
        }

        memSource(OP_JMP_IFN) { source  ->
            word(source.name)
            source(this)
            amend(LOAD_FLAG_A, JMP_IFN)
        }

        memSource(OP_DISPLAY) { source  ->
            word(source.name)
            source(this)
            amend(STORE_DISPLAY)
        }

        boolMemSourceDest(OP_STORE__FLAG) { source, dest ->
            word(source.name)
            source(this)
            word(dest.name)
            dest(this)
        }

        memSourceDest(OP_STORE) { source, dest ->
            word(source.name)
            source(this)
            word(dest.name)
            dest(this)
        }
    }

    private fun memSource(
        opcode: Opcode,
        conf: Instruction.(source: DataSource) -> Unit
    ): List<Instruction> {
        return mem(opcode) { source, _ ->
            conf(source!!)
        }
    }

    private fun memDest(
        opcode: Opcode,
        conf: Instruction.(dest: DataDest) -> Unit
    ): List<Instruction> {
        return mem(opcode) { _, dest ->
            conf(dest!!)
        }
    }

    private fun memSourceDest(
        opcode: Opcode,
        conf: Instruction.(source: DataSource, dest: DataDest) -> Unit
    ): List<Instruction> {
        return mem(opcode) { source, dest ->
            conf(source!!, dest!!)
        }
    }

    /**
     * Automatically creates a set of instructions for all data sources and destinations. If only input or output bits
     * are specified in the template string then the one that isn't specified will be passed as null into the config
     * function.
     *
     * The `ssss` and `dddd` placeholders in the template will be replaced with the bits that represent the source and
     * destination passed
     *
     */
    private fun mem(
        opcode: Opcode,
        conf: Instruction.(source: DataSource?, dest: DataDest?) -> Unit
    ): List<Instruction> {
        val templateBits = opcode.opcode

        val sources: List<DataSource?> = if(templateBits.contains("ssss")) listOf(*DataSource.values()) else listOf(null)
        val destinations: List<DataDest?> = if(templateBits.contains("dddd")) listOf(*DataDest.values()) else listOf(null)

        val list = mutableListOf<Instruction>()
        sources.map { source ->
            destinations.map { destination ->
                var bits = templateBits
                if(source != null) bits = bits.replace("ssss", source.ordinal.toString(2).padStart(4, '0'))
                if(destination != null) bits = bits.replace("dddd", destination.ordinal.toString(2).padStart(4, '0'))

                val insn = Instruction(opcode.opname, bits.toUShort(2)) {
                    conf(source, destination)
                }
                list.add(insn)
                insn
            }
        }
        instructions.addAll(list)
        return list
    }

    private fun boolMemSource(
        opcode: Opcode,
        conf: Instruction.(source: BoolDataSource) -> Unit
    ): List<Instruction> {
        return boolMem(opcode) { source, _ ->
            conf(source!!)
        }
    }

    private fun boolMemDest(
        opcode: Opcode,
        conf: Instruction.(dest: BoolDataDest) -> Unit
    ): List<Instruction> {
        return boolMem(opcode) { _, dest ->
            conf(dest!!)
        }
    }

    private fun boolMemSourceDest(
        opcode: Opcode,
        conf: Instruction.(source: BoolDataSource, dest: BoolDataDest) -> Unit
    ): List<Instruction> {
        return boolMem(opcode) { source, dest ->
            conf(source!!, dest!!)
        }
    }

    /**
     * Automatically creates a set of instructions for all data sources and destinations. If only input or output bits
     * are specified in the template string then the one that isn't specified will be passed as null into the config
     * function.
     *
     * The `ssss` and `dddd` placeholders in the template will be replaced with the bits that represent the source and
     * destination passed
     *
     */
    private fun boolMem(
        opcode: Opcode,
        conf: Instruction.(source: BoolDataSource?, dest: BoolDataDest?) -> Unit
    ): List<Instruction> {
        val templateBits = opcode.opcode

        val sources: List<BoolDataSource?> = if(templateBits.contains("ssss")) listOf(*BoolDataSource.values()) else listOf(null)
        val destinations: List<BoolDataDest?> = if(templateBits.contains("dddd")) listOf(*BoolDataDest.values()) else listOf(null)

        val list = mutableListOf<Instruction>()
        sources.map { source ->
            destinations.map { destination ->
                var bits = templateBits
                if(source != null) bits = bits.replace("ssss", source.ordinal.toString(2).padStart(4, '0'))
                if(destination != null) bits = bits.replace("dddd", destination.ordinal.toString(2).padStart(4, '0'))

                val insn = Instruction(opcode.opname, bits.toUShort(2)) {
                    conf(source, destination)
                }
                list.add(insn)
                insn
            }
        }
        instructions.addAll(list)
        return list
    }

    private fun insn(opcode: Opcode, conf: Instruction.() -> Unit): Instruction {
        val insn = Instruction(opcode.opname, opcode.opcode.replace("_", "").toUShort(2), conf)
        instructions.add(insn)
        return insn
    }

    fun microcodeROM(): Pair<HexFile, HexFile> {
        val page1 = HexFile()
        val page2 = HexFile()
        instructions.forEach { insn ->
            val offset = insn.opcode.toUInt().toLong() shl 8
            insn.microcode().forEachIndexed { i, step ->
                page1[offset + i] = (step and 0xFFFFFFFFuL).toLong()
                page2[offset + i] = (step shr 32 and 0xFFFFFFFFuL).toLong()
            }
        }
        return page1 to page2
    }

}