possiblePrime = R1
i = R2
checkAgainst = R3

ld ~possiblePrime, ~i, 2, 0
ld *~i, ~possiblePrime
ld ~possiblePrime, 1
checkNextPrime:
    ld ~i, 0
    calc ~possiblePrime, add, ~possiblePrime, 2
    test:
        ld ~checkAgainst, *~i
        jmpif foundPrime, eqz calc add, ~checkAgainst, 0
        jmpif checkNextPrime, eqz calc mod, ~possiblePrime, ~checkAgainst
        calc ~i, add, ~i, 1
        jmp test
    foundPrime:
        ld *~i, ~possiblePrime
        jmp checkNextPrime
