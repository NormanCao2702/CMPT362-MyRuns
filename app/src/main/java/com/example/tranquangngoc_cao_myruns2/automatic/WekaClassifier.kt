package com.example.tranquangngoc_cao_myruns2.automatic

class WekaClassifier {
    companion object {
        @Throws(Exception::class)
        fun classify(i: Array<Any?>): Double {
            return N48acd83b0(i)
        }

        private fun N48acd83b0(i: Array<Any?>): Double {
            return when {
                i[0] == null -> 0.0
                (i[0] as Double) <= 13.390311 -> 0.0
                else -> N7f90ac801(i)
            }
        }

        private fun N7f90ac801(i: Array<Any?>): Double {
            return when {
                i[64] == null -> 1.0
                (i[64] as Double) <= 14.534508 -> N588f48d32(i)
                else -> 2.0
            }
        }

        private fun N588f48d32(i: Array<Any?>): Double {
            return when {
                i[4] == null -> 1.0
                (i[4] as Double) <= 14.034383 -> N5c1cffd83(i)
                else -> 1.0
            }
        }

        private fun N5c1cffd83(i: Array<Any?>): Double {
            return when {
                i[7] == null -> 1.0
                (i[7] as Double) <= 4.804712 -> 1.0
                else -> 2.0
            }
        }
    }
}