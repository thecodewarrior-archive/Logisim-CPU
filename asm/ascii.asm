ld ~R0, 58
loop:
    sformat ~R0, 16
    sout 0xA
    calc ~R0, add ( ~R0, 1 )
jmp loop
