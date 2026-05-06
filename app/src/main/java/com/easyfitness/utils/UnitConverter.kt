package com.easyfitness.utils

class UnitConverter {
    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    fun milliSecondsToTimer(milliseconds: Long): String {
        var finalTimerString = ""
        var secondsString = ""

        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000).toInt()
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours.toString() + ":"
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds
        } else {
            secondsString = "" + seconds
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString

        // return timer string
        return finalTimerString
    }

    /**
     * Function to get Progress percentage
     *
     * @param currentDuration
     * @param totalDuration
     */
    fun getProgressPercentage(currentDuration: Long, totalDuration: Long): Int {
        val percentage: Double?

        val currentSeconds = (currentDuration / 1000).toInt().toLong()
        val totalSeconds = (totalDuration / 1000).toInt().toLong()

        // calculating percentage
        percentage = ((currentSeconds.toDouble()) / totalSeconds) * 100

        // return percentage
        return percentage.toInt()
    }

    /**
     * Function to change progress to timer
     *
     * @param progress      -
     * @param totalDuration returns current duration in milliseconds
     */
    fun progressToTimer(progress: Int, totalDuration: Int): Int {
        var totalDuration = totalDuration
        var currentDuration = 0
        totalDuration = totalDuration / 1000
        currentDuration = (((progress.toDouble()) / 100) * totalDuration).toInt()

        // return current duration in milliseconds
        return currentDuration * 1000
    }

    companion object {
        const val UNIT_KG: Int = 0
        const val UNIT_LBS: Int = 1
        const val UNIT_STONES: Int = 2

        const val UNIT_KM: Int = 0
        const val UNIT_MILES: Int = 1

        /*
     * convert Kg to Lbs
     */
        fun weightConverter(pWeight: Float, pUnitIn: Int, pUnitOut: Int): Float {
            when (pUnitIn) {
                UNIT_KG -> when (pUnitOut) {
                    UNIT_LBS -> return KgtoLbs(pWeight)
                    UNIT_KG -> return pWeight
                    else -> return pWeight
                }

                UNIT_LBS -> when (pUnitOut) {
                    UNIT_KG -> return LbstoKg(pWeight)
                    UNIT_LBS -> return pWeight
                    else -> return pWeight
                }

                else -> return pWeight
            }
        }

        fun KgtoLbs(pKg: Float): Float {
            return pKg / 0.45359237.toFloat()
        }

        fun LbstoKg(pLbs: Float): Float {
            return pLbs * 0.45359237.toFloat()
        }

        fun KmToMiles(pKm: Float): Float {
            return pKm * 1.609344.toFloat()
        }

        fun MilesToKm(pMiles: Float): Float {
            return pMiles / 1.609344.toFloat()
        }
    }
}
