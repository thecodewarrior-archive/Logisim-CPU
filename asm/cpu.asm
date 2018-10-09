#cpudef {
    #bits 32

    #tokendef alu_2op 
    {
        add = 0
        sub = 1
        mul = 2
        div = 3
        mod = 4
        shl = 6
        shr = 7
        rotl = 8
        rotr = 9
        bit_and = 11
        bit_or = 12
        bit_xor = 13
        bool_and = 15
        bool_or = 16
        bool_xor = 17
        cmp_eq = 18
        cmp_ne = 19
        cmp_gt = 20
        cmp_lt = 21
        cmp_geq = 22
        cmp_leq = 23
    }
    #tokendef alu_1op 
    {
        neg = 5
        bit_not = 10
        bool_not = 14
    }

    #tokendef alu_cmpop
    {
        eq = 18
        ne = 19
        gt = 20
        lt = 21
        geq = 22
        leq = 23
    }


    #tokendef jmp_cmp
    {
        gtz = 0
        lez = 1
        eqz = 2
        gez = 3
        ltz = 4
        nez = 5
    }

;------------------------------------------------

    ; - Base
    nop -> 32'0x000
    halt -> 32'0x001

    ; - Load
    ld.w *{reg}, {value} -> {
        assert(reg >= 0)
        assert(reg < 32)
        0x002 @ 15'0 @ reg[4:0] @ value[31:0]
    }
    ld.b *{reg}, {value} -> {
        assert(reg >= 0)
        assert(reg < 32)
        assert(value <= 0xff)
        0x003 @ reg[4:0] @ 7'0 @ value[7:0]
    }
    ld.t *{reg1}, *{reg2}, {value1}, {value2} -> {
        assert(reg1 >= 0)
        assert(reg1 < 32)
        assert(reg2 >= 0)
        assert(reg2 < 32)
        assert(value1 <= 0b11111)
        assert(value2 <= 0b11111)
        0x004 @ reg1[4:0] @ reg2[4:0] @ value1[4:0] @ value2[4:0]
    }

    ld *{reg1}, *{reg2}, {value1}, {value2} -> {
        assert(reg1 >= 0)
        assert(reg1 < 32)
        assert(reg2 >= 0)
        assert(reg2 < 32)
        assert(value1 <=  0b01111)
        assert(value2 <=  0b01111)
        assert(value1 >= -0b10000)
        assert(value2 >= -0b10000)
        0x004 @ reg1[4:0] @ reg2[4:0] @ value1[4:0] @ value2[4:0]
    }
    ld *{reg}, {value} -> {
        assert(reg >= 0)
        assert(reg < 32)
        assert(value <=  0x01111111)
        assert(value >= -0x10000000)
        0x003 @ reg[4:0] @ 7'0 @ value[7:0]
    }
    ld *{reg}, {value} -> {
        assert(reg >= 0)
        assert(reg < 32)
        0x002 @ 15'0 @ reg[4:0] @ value[31:0]
    }

    ; - Arithmetic
    calc *{out}, {op: alu_1op}, *{lhs} -> {
        assert(out >= 0)
        assert(out < 32)
        assert(lhs >= 0)
        assert(lhs < 32)
        0x006 @ op[4:0] @ lhs[4:0] @ 5'0 @ out[4:0] 
    }
    calc *{out}, {op: alu_2op}, *{lhs}, *{rhs} -> {
        assert(out >= 0)
        assert(out < 32)
        assert(lhs >= 0)
        assert(lhs < 32)
        assert(rhs >= 0)
        assert(rhs < 32)
        0x005 @ op[4:0] @ lhs[4:0] @ rhs[4:0] @ out[4:0]
    }
    calc *{out}, {op: alu_2op}, *{lhs}, {rhs} -> {
        assert(out >= 0)
        assert(out < 32)
        assert(lhs >= 0)
        assert(lhs < 32)
        assert(rhs <=  0b01111)
        assert(rhs >= -0b10000)
        0x00b @ op[4:0] @ lhs[4:0] @ rhs[4:0] @ out[4:0]
    }
    calc *{out}, {op: alu_2op}, {lhs}, *{rhs} -> {
        assert(out >= 0)
        assert(out < 32)
        assert(rhs >= 0)
        assert(rhs < 32)
        assert(lhs <=  0b01111)
        assert(lhs >= -0b10000)
        0x00c @ op[4:0] @ lhs[4:0] @ rhs[4:0] @ out[4:0]
    }

    ; - Jumps
    jmp {addr} -> {
        assert(addr >= 0)
        0x008 @ 20'0 @ addr[31:0]
    }

    jmpif {addr}, {cmp: jmp_cmp} calc {op: alu_2op}, *{lhs}, *{rhs} -> {
        assert(addr >= 0)
        assert(lhs >= 0)
        assert(lhs < 32)
        assert(rhs >= 0)
        assert(rhs < 32)
        0x008 @ op[4:0] @ lhs[4:0] @ rhs[4:0] @ cmp[4:0] @ addr[31:0]
    }
    jmpif {addr}, {op: alu_cmpop}, *{lhs}, *{rhs} -> {
        assert(addr >= 0)
        assert(lhs >= 0)
        assert(lhs < 32)
        assert(rhs >= 0)
        assert(rhs < 32)
        0x008 @ op[4:0] @ lhs[4:0] @ rhs[4:0] @ 5'5 @ addr[31:0]
    }
    jmpifn {addr}, {op: alu_cmpop}, *{lhs}, *{rhs} -> {
        assert(addr >= 0)
        assert(lhs >= 0)
        assert(lhs < 32)
        assert(rhs >= 0)
        assert(rhs < 32)
        0x008 @ op[4:0] @ lhs[4:0] @ rhs[4:0] @ 5'2 @ addr[31:0]
    }
    jmpif {addr}, {cmp: jmp_cmp} calc {op: alu_2op}, *{lhs}, {rhs} -> {
        assert(addr >= 0)
        assert(lhs >= 0)
        assert(lhs < 32)
        assert(rhs <=  0b01111)
        assert(rhs >= -0b10000)
        0x009 @ op[4:0] @ lhs[4:0] @ rhs[4:0] @ cmp[4:0] @ addr[31:0]
    }
    jmpif {addr}, {cmp: jmp_cmp} calc {op: alu_2op}, {lhs}, *{rhs} -> {
        assert(addr >= 0)
        assert(rhs >= 0)
        assert(rhs < 32)
        assert(lhs <=  0b01111)
        assert(lhs >= -0b10000)
        0x00a @ op[4:0] @ lhs[4:0] @ rhs[4:0] @ cmp[4:0] @ addr[31:0]
    }
}

R0 = 0
R1 = 1
R2 = 2
R3 = 3
R4 = 4
R5 = 5
R6 = 6
R7 = 7
R8 = 8
R9 = 9
R10 = 10
R11 = 11
R12 = 12
R13 = 13
R14 = 14
R15 = 15
R16 = 16
R17 = 17
R18 = 18
R19 = 19
R20 = 20
R21 = 21
R22 = 22
R23 = 23
R24 = 24
R25 = 25
R26 = 26
R27 = 27
R28 = 28
R29 = 29
R30 = 30
R31 = 31
