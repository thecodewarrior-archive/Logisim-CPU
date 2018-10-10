display = R0
possiblePrime = R1
halfPossiblePrime = R4
i = R2
checkAgainst = R3

ld ~possiblePrime, ~i, 2, 0
ld *~i, ~possiblePrime
ld ~possiblePrime, 1
checkNextPrime:
    ld ~i, 0
    calc ~possiblePrime, add, ~possiblePrime, 2
    calc ~halfPossiblePrime, shl, ~possiblePrime, 1
    test:
        ld ~checkAgainst, *~i
        jmpif foundPrime, nez calc cmp_geq, ~checkAgainst, ~halfPossiblePrime
        jmpif checkNextPrime, eqz calc mod, ~possiblePrime, ~checkAgainst
        calc ~i, add, ~i, 1
        jmp test
    foundPrime:
        ld *~i, ~possiblePrime
        calc ~display, add, ~possiblePrime, 0
        jmp checkNextPrime
