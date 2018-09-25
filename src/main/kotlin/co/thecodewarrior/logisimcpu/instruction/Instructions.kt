package co.thecodewarrior.logisimcpu.instruction

import co.thecodewarrior.logisimcpu.HexFile
import co.thecodewarrior.logisimcpu.instruction.ControlUnitWire.*
import co.thecodewarrior.logisimcpu.instruction.Instructions.Opcode.*
import co.thecodewarrior.logisimcpu.instruction.Instructions.ALUType.*

object Instructions {
    val instructions: MutableList<Instruction> = mutableListOf()

    /**
     * @property opcode A binary string with optional specialty placeholders
     * @param opcode A binary string with optional specialty placeholders. Spaces in this string will be removed to
     * allow separation into block, and periods (`.`) will be replaced with `0`s so leading zeros don't create visual
     * noise which disrupts important information.
     */
    enum class Opcode(opcode: String) {
        OP_HALT         (".... .... .... ...."),
        OP_NOP          (".... .... .... ...1"),
        OP_DISPLAY      (".... .... ..10 ssss"),
        OP_STORE        (".... ...1 ssss dddd"),
        OP_BOOTSTRAP    ("1111 1111 1111 1111");

        val opcode = opcode.replace(" ", "").replace(".", "0")
        val opname: String
            get() = name.removePrefix("OP_")
    }

    init {
        insn(OP_BOOTSTRAP) {}.apply {
            steps.clear()
            steps.add(InstructionStep(LOAD_PROG, STORE_INSN, PROG_NEXT, INSN_END))
        }

        insn(OP_HALT) {
            step(HALT)
        }

        insn(OP_NOP) {
        }

        memSource(OP_DISPLAY) { source  ->
            word(source.name)
            source(this)
            amend(STORE_DISPLAY)
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

    private fun insn(opcode: Opcode, conf: Instruction.() -> Unit): Instruction {
        val insn = Instruction(opcode.opname, opcode.opcode.replace("_", "").toUShort(2), conf)
        instructions.add(insn)
        return insn
    }

    fun controlUnitROM(): HexFile {
        val file = HexFile()
        instructions.forEach { insn ->
            val offset = insn.opcode.toUInt().toLong() shl 8
            insn.toROM().forEachIndexed { i, step ->
                file[offset + i] = step.toLong()
            }
        }
        return file
    }

    enum class ALUType {
        INT,
        BOOL,
    }
    enum class ALUOp(vararg val outputs: ALUType) {
        // arithmetic
        ADD(INT), // A + B
        SUB(INT), // A - B
        MUL(INT), // A * B
        DIV(INT), // A / B
        REM(INT), // remainder of A / B
        NEG(INT), // -A
        INC(INT), // A + 1
        DEC(INT), // A - 1

        // bitwise
        SHL(INT), // A << B
        SHR(INT), // A >> B
        CNT(INT), // count 1s in A
        NOT(INT, BOOL), // ~A
        AND(INT, BOOL), // A & B
        OR(INT, BOOL), // A | B
        XOR(INT, BOOL), // A ^ B

        // comparison
        CMPEQ(BOOL), // A == B
        CMPNEQ(BOOL), // A != B
        CMPLT(BOOL), // A < B
        CMPGT(BOOL), // A > B
        CMPEQZ(BOOL), // A == 0
        CMPNEQZ(BOOL), // A != 0
        CMPLTZ(BOOL), // A < 0
        CMPGTZ(BOOL), // A > 0
    }

    enum class DataSource {
        REG_A {
            override fun invoke(insn: Instruction) {
                insn.step(LOAD_A)
            }
        },
        REG_B {
            override fun invoke(insn: Instruction) {
                insn.step(LOAD_B)
            }
        },
        ALU {
            override fun invoke(insn: Instruction) {
                insn.payload(ALUOp.values()
                    .filter { INT in it.outputs }
                    .associate { it.name to ushortArrayOf(it.ordinal.toUShort()) }
                )
                insn.step(LOAD_PROG, STORE_ALU_OP, PROG_NEXT)
                insn.step(LOAD_ALU)
            }
        },
        CONST {
            override fun invoke(insn: Instruction) {
                insn.arg()
                insn.step(LOAD_PROG, PROG_NEXT)
            }
        },
        FLAG_REG_A {
            override fun invoke(insn: Instruction) {
                insn.step(LOAD_FLAG_A, B2I)
            }
        },
        FLAG_REG_B {
            override fun invoke(insn: Instruction) {
                insn.step(LOAD_FLAG_B, B2I)
            }
        };

        /**
         * Adds one or more steps to the passed instruction such that the value in the source has been loaded
         * onto the bus in the last step.
         */
        abstract operator fun invoke(insn: Instruction)
    }

    enum class DataDest {
        REG_A {
            override fun invoke(insn: Instruction) {
                insn.amend(STORE_A)
            }
        },
        REG_B {
            override fun invoke(insn: Instruction) {
                insn.amend(STORE_B)
            }
        };

        /**
         * Amends the last step so the value on the bus is written to the destination. This method may or may not add
         * additional steps.
         */
        abstract operator fun invoke(insn: Instruction)
    }

    enum class BoolDataSource {
        REG_A {
            override fun invoke(insn: Instruction) {
                insn.step(LOAD_FLAG_A)
            }
        },
        REG_B {
            override fun invoke(insn: Instruction) {
                insn.step(LOAD_FLAG_B)
            }
        },
        ALU {
            override fun invoke(insn: Instruction) {
                insn.payload(ALUOp.values()
                    .filter { BOOL in it.outputs }
                    .associate { it.name to ushortArrayOf(it.ordinal.toUShort()) }
                )
                insn.step(LOAD_PROG, STORE_ALU_OP, PROG_NEXT)
                insn.step(LOAD_ALU)
            }
        },
        CONST {
            override fun invoke(insn: Instruction) {
                insn.arg()
                insn.step(LOAD_PROG, I2B, PROG_NEXT)
            }
        },
        INT_REG_A {
            override fun invoke(insn: Instruction) {
                insn.step(LOAD_A, I2B)
            }
        },
        INT_REG_B {
            override fun invoke(insn: Instruction) {
                insn.step(LOAD_B, I2B)
            }
        };

        /**
         * Adds one or more steps to the passed instruction such that the value in the source has been loaded
         * onto the bus in the last step.
         */
        abstract operator fun invoke(insn: Instruction)
    }

    enum class BoolDataDest {
        REG_A {
            override fun invoke(insn: Instruction) {
                insn.amend(STORE_FLAG_A)
            }
        },
        REG_B {
            override fun invoke(insn: Instruction) {
                insn.amend(STORE_FLAG_B)
            }
        };

        /**
         * Amends the last step so the value on the bus is written to the destination. This method may or may not add
         * additional steps.
         */
        abstract operator fun invoke(insn: Instruction)
    }
}