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
    /**
     * Halts the computer                   (disables the clock)
     *
     * Syntax: **HALT**
     */
    OP_HALT                           ("0000 0000 0000 0000"),
    /**
     * Jumps immediately to the next instruction
     *
     * Syntax: **NOP**
     */
    OP_NOP                            (".... .... .... ...1"),
    /**
     * Swaps the values in the A and B registers.
     */
    OP_SWAP                           (".... .... .... ..10"),
    /**
     * Swaps the values in the A and B flag registers.
     */
    OP_SWAP_FLAGS                     (".... .... .... ..11"),
    /**
     * Sleeps the computer                   (disables the clock) for the *S* clock cycles
     *
     * Syntax: **SLEEP <INT_SRC>**
     * @see DataSource
     */
    OP_SLEEP                          (".... .... ...1 ssss"),
    /**
     * Moves the program counter to the address specified in *S* and continues execution from there
     *
     * Syntax: **JMP <INT_SRC>**
     * @see DataSource
     */
    OP_JMP                            (".... .... ..10 ssss"),
    /**
     * If the A flags register contains a 1 this instruction moves the program counter to the address specified
     * in *S* and continues execution from there, otherwise it is a noop.
     *
     * Syntax: **JMP_IF <INT_SRC>**
     * @see DataSource
     */
    OP_JMP_IF                         (".... .... ..11 ssss"),
    /**
     * If the A flags register contains a 0 this instruction moves the program counter to the address specified
     * in *S* and continues execution from there, otherwise it is a noop.
     *
     * Syntax: **JMP_IFN <INT_SRC>**
     * @see DataSource
     */
    OP_JMP_IFN                        (".... .... .100 ssss"),
    /**
     * If the current ALU output is zero this instruction moves the program counter to the address specified
     * in *S* and continues execution from there, otherwise it is a noop.
     *
     * Syntax: **JMP_IF ALU_EQZ <INT_ALU_OP> <INT_SRC>**
     * @see DataSource
     */
    OP_JMP_IF__ALU_EQZ                (".... .... .101 ssss"),
    /**
     * If the current ALU output is not zero this instruction moves the program counter to the address specified
     * in *S* and continues execution from there, otherwise it is a noop.
     *
     * Syntax: **JMP_IF ALU_NEQZ <INT_ALU_OP> <INT_SRC>**
     * @see DataSource
     */
    OP_JMP_IF__ALU_NEQZ               (".... .... .110 ssss"),
    /**
     * If the current ALU output flag on this instruction moves the program counter to the address specified
     * in *S* and continues execution from there, otherwise it is a noop.
     *
     * Syntax: **JMP_IF ALU <BOOL_ALU_OP> <INT_SRC>**
     * @see DataSource
     */
    OP_JMP_IF__ALU                    (".... .... .111 ssss"),
    /**
     * Loads the value from *S* into the display register
     *
     * Syntax: **DISPLAY <INT_SRC>**
     * @see DataSource
     */
    OP_DISPLAY                        (".... .... 1000 ssss"),
    /**
     * Reads the value from *S* and writes it into *D*
     *
     * Syntax: **STORE <INT_SRC> <INT_DEST>**
     * @see DataSource
     * @see DataDest
     */
    OP_STORE                          (".... ...1 ssss dddd"),
    /**
     * Reads the flag from *S* and writes it into *D*
     *
     * Syntax: **STORE FLAG <BOOL_SRC> <BOOL_DEST>**
     * @see BoolDataSource
     * @see BoolDataDest
     */
    OP_STORE__FLAG                    (".... ..10 ssss dddd"),
    /**
     * Identical to NOP. This is the first instruction run by the computer, and its purpose is to load the first
     * program instruction from ROM so it can be executed.
     *
     * Syntax: N/A
     */
    OP_BOOTSTRAP                      ("1111 1111 1111 1111");

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

    SWAP_INT,
    SWAP_FLAGS,
    SWAP_INTERNAL_INT,
    SWAP_INTERNAL_FLAGS,
    ALU_INPUT_SWAP,

    STORE_RAM_ADDR,
    STORE_RAM,
    LOAD_RAM,
    NEXT_INSN,
    ALU_EQZERO,
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
    /**
     * Reads the value from the A register.
     *
     * Syntax: **REG_A**
     */
    REG_A {
        override fun invoke(insn: Instruction) {
            insn.step(LOAD_A)
        }
    },
    /**
     * Reads the value from the B register.
     *
     * Syntax: **REG_A**
     */
    REG_B {
        override fun invoke(insn: Instruction) {
            insn.step(LOAD_B)
        }
    },
    /**
     * Selects an operation for the ALU and reads the result
     *
     * Syntax: **ALU <OP>**
     *
     * @see ALUOp
     */
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
    /**
     * Selects an operation for the ALU and reads the result when A and B are swapped
     *
     * Syntax: **ALU_SWAP <OP>**
     *
     * @see ALUOp
     */
    ALU_SWAP {
        override fun invoke(insn: Instruction) {
            insn.payload(ALUOp.values()
                .filter { INT in it.outputs }
                .associate { it.name to ushortArrayOf(it.ordinal.toUShort()) }
            )
            insn.step(LOAD_PROG, STORE_ALU_OP, PROG_NEXT)
            insn.step(ALU_INPUT_SWAP, LOAD_ALU)
        }
    },
    /**
     * Reads a value directly from the program ROM
     *
     * Syntax: **CONST <NUM>** (num supports the standard prefixes for binary, octal, hex, or decimal literals)
     */
    CONST {
        override fun invoke(insn: Instruction) {
            insn.amendEnd = false
            insn.arg()
            insn.step(LOAD_PROG, PROG_NEXT)
        }
    },
    /**
     * Reads the value from the A flag register as a 0/1 value. The behavior of this source is undefined if the flags
     * bus is used simultaneously.
     *
     * Syntax: **FLAG_REG_A**
     */
    FLAG_REG_A {
        override fun invoke(insn: Instruction) {
            insn.step(LOAD_FLAG_A, B2I)
        }
    },
    /**
     * Reads the value from the B flag register as a 0/1 value. The behavior of this source is undefined if the flags
     * bus is used simultaneously.
     *
     * Syntax: **FLAG_REG_B**
     */
    FLAG_REG_B {
        override fun invoke(insn: Instruction) {
            insn.step(LOAD_FLAG_B, B2I)
        }
    },
    /**
     * Reads an address from the program (assembler needs to be able to support non-constant addresses as well later)
     * and returns the specified value from RAM.
     *
     * Syntax: **RAM <NUM>**
     */
    RAM {
        override fun invoke(insn: Instruction) {
            insn.arg()
            insn.step(LOAD_PROG, STORE_RAM_ADDR, PROG_NEXT)
            insn.step(LOAD_RAM)
        }
    },
    ;

    /**
     * Adds one or more steps to the passed instruction such that the value in the source has been loaded
     * onto the bus in the last step.
     */
    abstract operator fun invoke(insn: Instruction)
}

enum class DataDest {
    /**
     * Writes the value to the A register
     *
     * Syntax: **REG_A**
     */
    REG_A {
        override fun invoke(insn: Instruction) {
            insn.amend(STORE_A)
        }
    },
    /**
     * Writes the value to the B register
     *
     * Syntax: **REG_B**
     */
    REG_B {
        override fun invoke(insn: Instruction) {
            insn.amend(STORE_B)
        }
    },
    /**
     * Reads an address from the program (assembler needs to be able to support non-constant addresses as well later)
     * and writes to the specified address in RAM.
     *
     * Syntax: **RAM <NUM>**
     */
    RAM {
        override fun invoke(insn: Instruction) {
            insn.amend(STORE_INTERNAL_A)
            insn.arg()
            insn.step(LOAD_PROG, STORE_RAM_ADDR, PROG_NEXT)
            insn.step(LOAD_INTERNAL_A, STORE_RAM)
        }
    },
    ;

    /**
     * Amends the last step so the value on the bus is written to the destination. This method may or may not add
     * additional steps.
     */
    abstract operator fun invoke(insn: Instruction)
}

enum class BoolDataSource {
    /**
     * Reads the value in the A flag register
     *
     * Syntax: **REG_A**
     */
    REG_A {
        override fun invoke(insn: Instruction) {
            insn.step(LOAD_FLAG_A)
        }
    },
    /**
     * Reads the value in the B flag register
     *
     * Syntax: **REG_B**
     */
    REG_B {
        override fun invoke(insn: Instruction) {
            insn.step(LOAD_FLAG_B)
        }
    },
    /**
     * Selects an operation for the ALU and reads the result
     *
     * Syntax: **ALU <OP>**
     *
     * @see ALUOp
     */
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
    /**
     * Selects an operation for the ALU and reads the result when A and B are swapped
     *
     * Syntax: **ALU_SWAP <OP>**
     *
     * @see ALUOp
     */
    ALU_SWAP {
        override fun invoke(insn: Instruction) {
            insn.payload(ALUOp.values()
                .filter { BOOL in it.outputs }
                .associate { it.name to ushortArrayOf(it.ordinal.toUShort()) }
            )
            insn.step(LOAD_PROG, STORE_ALU_OP, PROG_NEXT)
            insn.step(ALU_INPUT_SWAP, LOAD_ALU_FLAG)
        }
    },
    /**
     * Reads a value directly from the program ROM
     *
     * Syntax: **CONST <BOOL>** (BOOL should be either "true" or "false")
     */
    CONST {
        override fun invoke(insn: Instruction) {
            insn.amendEnd = false
            insn.payload(mapOf(
                "true" to ushortArrayOf(1u),
                "false" to ushortArrayOf(0u)
            ))
            insn.step(LOAD_PROG, I2B, PROG_NEXT)
        }
    },
    /**
     * Reads the value from the A register, interpreting non-zero as true. The behavior of this source is undefined
     * if the int bus is used simultaneously.
     *
     * Syntax: **INT_REG_A**
     */
    INT_REG_A {
        override fun invoke(insn: Instruction) {
            insn.step(LOAD_A, I2B)
        }
    },
    /**
     * Reads the value from the B register, interpreting non-zero as true. The behavior of this source is undefined
     * if the int bus is used simultaneously.
     *
     * Syntax: **INT_REG_B**
     */
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
    /**
     * Writes the value to the A flag register
     *
     * Syntax: **REG_A**
     */
    REG_A {
        override fun invoke(insn: Instruction) {
            insn.amend(STORE_FLAG_A)
        }
    },
    /**
     * Writes the value to the B flag register
     *
     * Syntax: **REG_B**
     */
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
