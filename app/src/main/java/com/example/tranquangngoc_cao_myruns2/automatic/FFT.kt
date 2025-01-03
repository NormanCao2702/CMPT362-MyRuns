package com.example.tranquangngoc_cao_myruns2.automatic

class FFT(private val n: Int) {
    private val m: Int
    private val cos: DoubleArray
    private val sin: DoubleArray
    private lateinit var window: DoubleArray

    init {
        m = (Math.log(n.toDouble()) / Math.log(2.0)).toInt()
        if (n != 1 shl m)
            throw RuntimeException("FFT length must be power of 2")
        cos = DoubleArray(n / 2)
        sin = DoubleArray(n / 2)
        for (i in 0 until n / 2) {
            cos[i] = Math.cos(-2.0 * Math.PI * i / n)
            sin[i] = Math.sin(-2.0 * Math.PI * i / n)
        }
        makeWindow()
    }

    private fun makeWindow() {
        window = DoubleArray(n)
        for (i in window.indices)
            window[i] = 0.42 - 0.5 * Math.cos(2.0 * Math.PI * i / (n - 1)) + 0.08 * Math.cos(4.0 * Math.PI * i / (n - 1))
    }

    fun getWindow(): DoubleArray {
        return window
    }

    fun fft(x: DoubleArray, y: DoubleArray) {
        var i = 0
        var j = 0
        var k: Int
        var n1: Int
        var n2: Int
        var a: Int
        var c: Double
        var s: Double
        var t1: Double
        var t2: Double

        // Bit-reverse
        n2 = n / 2
        var i1 = 1
        while (i1 < n - 1) {
            n1 = n2
            while (j >= n1) {
                j -= n1
                n1 /= 2
            }
            j += n1
            if (i1 < j) {
                t1 = x[i1]
                x[i1] = x[j]
                x[j] = t1
                t1 = y[i1]
                y[i1] = y[j]
                y[j] = t1
            }
            i1++
        }

        // FFT
        n1 = 0
        n2 = 1
        i = 0
        while (i < m) {
            n1 = n2
            n2 = n2 + n2
            a = 0
            j = 0
            while (j < n1) {
                c = cos[a]
                s = sin[a]
                a += 1 shl m - i - 1
                k = j
                while (k < n) {
                    t1 = c * x[k + n1] - s * y[k + n1]
                    t2 = s * x[k + n1] + c * y[k + n1]
                    x[k + n1] = x[k] - t1
                    y[k + n1] = y[k] - t2
                    x[k] = x[k] + t1
                    y[k] = y[k] + t2
                    k += n2
                }
                j++
            }
            i++
        }
    }
}