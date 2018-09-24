package co.thecodewarrior.logisimcpu

import java.util.SortedMap

class HexFile(private val data: SortedMap<Long, Long> = sortedMapOf()): SortedMap<Long, Long> by data {
    override fun toString(): String {
        return HexStringBuilder().build()
    }

    private inner class HexStringBuilder {
        var out = "v2.0 raw\n"
        var lastAddress = -1L
        var value = 0L
        var count = 0L

        fun build(): String {
            data.forEach { (address, value) ->
                add(address, value)
            }
            push()
            return out
        }

        private fun add(address: Long, value: Long) {
            if(address > lastAddress + 1) {
                addValues(0L, (address-1) - lastAddress)
            }
            addValues(value, 1L)
            lastAddress = address
        }

        private fun addValues(value: Long, count: Long) {
            if(this.value != value) push()
            this.value = value
            this.count += count
        }

        private fun push() {
            if(count == 0L) return
            if(count != 1L) {
                out += "$count*"
            }

            out += value.toString(16)

            out += " "

            value = 0L
            count = 0L
        }
    }
}