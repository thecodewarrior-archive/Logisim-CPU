STORE CONST 1 REG_A
check_next:
STORE ALU INC REG_A
STORE CONST 1 REG_B
test:
    STORE ALU_SWAP INC REG_B
    JMP_IF ALU CMP_EQ >display
    JMP_IF ALU_EQZ REM >check_next
JMP >test
display:
DISPLAY REG_A
JMP >check_next
HALT
