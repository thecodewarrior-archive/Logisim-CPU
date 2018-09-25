package co.thecodewarrior.logisimcpu.instruction

import co.thecodewarrior.logisimcpu.instruction.ALUType.*
import co.thecodewarrior.logisimcpu.instruction.ControlUnitWire.*

/**
 * Double underscores in the enum name will be replaced with a space in the operation name
 * @property opcode A binary string with optional specialty placeholders
 * @param opcode A binary string with optional specialty placeholders. Spaces in this string will be removed to
 * allow separation into block, and periods (`.`) will be replaced with `0`s so leading zeros don't create visual
 * noise which disrupts important information.
 */
enum class Opcode(opcode: String) {
    OP_HALT         (".... .... .... ...."),
    OP_NOP          (".... .... .... ...1"),
    OP_SLEEP        (".... .... ...1 ssss"),
    OP_JMP          (".... .... ..10 ssss"),
    OP_JMP_IF       (".... .... ..11 ssss"),
    OP_JMP_IFN      (".... .... .100 ssss"),
    OP_DISPLAY      (".... .... .101 ssss"),
    OP_STORE        (".... ...1 ssss dddd"),
    OP_STORE__FLAG  (".... ..10 ssss dddd"),
    OP_BOOTSTRAP    ("1111 1111 1111 1111");

    val opcode = opcode.replace(" ", "").replace(".", "0")
    val opname: String
        get() = name.removePrefix("OP_").replace("__", " ")
}

@Suppress("EnumEntryName")
enum class ControlUnitWire {
    HALT,
    FAULT,
    SLEEP,

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
    LOAD_INTERNAL_B,
    LOAD_FLAG_A,
    LOAD_FLAG_B,
    LOAD_INTERNAL_FLAG_A,
    LOAD_INTERNAL_FLAG_B,
    LOAD_ALU,
    LOAD_ALU_FLAG,

    STORE_A,
    STORE_B,
    STORE_INTERNAL_A,
    STORE_INTERNAL_B,
    STORE_FLAG_A,
    STORE_FLAG_B,
    STORE_INTERNAL_FLAG_A,
    STORE_INTERNAL_FLAG_B,
    STORE_ALU_OP,
    STORE_DISPLAY,

    I2B,
    B2I,
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
    NAND(INT, BOOL), // !(A & B)
    NOR(INT, BOOL), // !(A | B)
    XNOR(INT, BOOL), // !(A ^ B)

    // comparison
    CMP_EQ(BOOL), // A == B
    CMP_NEQ(BOOL), // A != B
    CMP_LT(BOOL), // A < B
    CMP_GT(BOOL), // A > B
    CMP_EQZ(BOOL), // A == 0
    CMP_NEQZ(BOOL), // A != 0
    CMP_LTZ(BOOL), // A < 0
    CMP_GTZ(BOOL), // A > 0
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
            insn.step(LOAD_ALU_FLAG)
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
