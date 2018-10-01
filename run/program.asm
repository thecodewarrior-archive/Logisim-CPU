STORE CONST 1 REG_A
check_next:
STORE ALU INC REG_A
STORE CONST 1 REG_B
test:
    STORE ALU_SWAP INC REG_B
    JMP_IF ALU_EQZ REM >is_prime
JMP >test

is_prime: JMP_IF ALU CMP_EQ >display
JMP >check_next
display:
DISPLAY REG_A
JMP >check_next
HALT
