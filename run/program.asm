MOV BYTE 1 0
MOV TINY 1 10 TINY 2 20
check_next:
ADD 0 10 0
MOV BYTE 1 1
test:
    ADD 1 10 1
    MOD 0 1 3
    JMP >is_prime IF 3 EQ 4
JMP >test

is_prime: JMP >display IF 0 EQ 1
JMP >check_next
display:
ADD 0 4 2
JMP >check_next
