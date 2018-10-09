possiblePrime = R0
checkAgainst = R1
display = R2

ld *possiblePrime, *display, 1, 2
checkNextPrime:
    calc *possiblePrime, add, *possiblePrime, 2
    ld *checkAgainst, 1
    test:
        calc *checkAgainst, add, *checkAgainst, 2
        jmpif foundDivisor, eqz calc mod, *possiblePrime, *checkAgainst
        jmp test
    foundDivisor:
        jmpif foundPrime, eq, *possiblePrime, *checkAgainst
        jmp checkNextPrime
    foundPrime:
        calc *display, add, *possiblePrime, 0
    jmp checkNextPrime
