display = R0
possiblePrime = R1
halfPossiblePrime = R4
i = R2
checkAgainst = R3
primeCount = R6

ld [ ~possiblePrime, 2 ] [ ~primeCount, 1 ]
ld *~i, ~possiblePrime
ld ~possiblePrime, 1
checkNextPrime:
    ld ~i, 0
    calc ~possiblePrime, add ( ~possiblePrime, 2 )
    calc ~halfPossiblePrime, shr ( ~possiblePrime, 1 )
    test:
        ld ~checkAgainst, *~i
        calc ~i, add ( ~i, 1 )
        jmpif checkNextPrime, eqz calc mod ( ~possiblePrime, ~checkAgainst )
        jmpif test, eqz calc cmp_geq ( ~checkAgainst, ~halfPossiblePrime )
    foundPrime:
        ld *~primeCount, ~possiblePrime
        calc ~primeCount, add ( ~primeCount, 1 )
        ld ~display, ~possiblePrime
        jmp checkNextPrime
