package com.easyfitness.DAO

import com.easyfitness.utils.UnitConverter
import java.text.DecimalFormat

class Weight(val storedWeight: Float, val storedUnit: Int) {
    fun getWeight(unit: Int): Float {
        var weight = this.storedWeight
        if (unit == UnitConverter.UNIT_LBS) {
            weight = UnitConverter.KgtoLbs(this.storedWeight)
        }
        return weight
    }

    override fun toString(): String {
        val numberFormat = DecimalFormat("#.##")
        return numberFormat.format(storedWeight.toDouble())
    }

    fun getWeightStr(unit: Int): String {
        val numberFormat = DecimalFormat("#.##")
        return numberFormat.format(getWeight(unit).toDouble())
    }
}
