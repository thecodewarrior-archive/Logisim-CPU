#!/bin/sh

customasm -f hexstr -o out.hex cpu.asm $@
sed -i.bak -E 's/(.{8})/\1 /g' out.hex
sed -i.bak '1s/^/v2.0 raw\
/' out.hex
