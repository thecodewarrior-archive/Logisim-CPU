package co.thecodewarrior.logisimcpu

import java.util.TreeMap

class HexFile: TreeMap<Long, Long>() {
    override fun toString(): String {
        return HexStringBuilder(this).build()
    }

    private class HexStringBuilder(val file: HexFile) {
        var out = "v2.0 raw\n"
        var nextAddress = 0u
        var value = 0u
        var count = 0u

        fun build(): String {
            file.forEach { (address, value) ->
                add(address.toUInt(), value.toUInt())
            }
            push()
            return out
        }

        private fun add(address: UInt, value: UInt) {
            if(address > nextAddress) {
                addValues(0u, address - nextAddress)
            }
            addValues(value, 1u)
            nextAddress = address + 1u
        }

        private fun addValues(value: UInt, count: UInt) {
            if(this.value != value) push()
            this.value = value
            this.count += count
        }

        private fun push() {
            if(count == 0u) return
            if(count != 1u) {
                out += "$count*"
            }

            out += value.toString(16)

            out += " "

            value = 0u
            count = 0u
        }
    }
}