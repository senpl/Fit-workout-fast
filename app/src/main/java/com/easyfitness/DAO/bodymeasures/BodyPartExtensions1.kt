package com.easyfitness.DAO.bodymeasures

import com.easyfitness.R

/* DataBase Object */
object BodyPartExtensions {
    const val ABDOMINAUX: Int = 0
    const val ADDUCTEURS: Int = 1
    const val BICEPS: Int = 2
    const val TRICEPS: Int = 3
    const val DELTOIDS: Int = 4
    const val MOLLETS: Int = 5
    const val PECTORAUX: Int = 6
    const val DORSEAUX: Int = 7
    const val QUADRICEPS: Int = 8
    const val ISCHIOJAMBIERS: Int = 9
    const val LEFTBICEPS: Int = 10
    const val RIGHTBICEPS: Int = 11
    const val LEFTTHIGH: Int = 12
    const val RIGHTTHIGH: Int = 13
    const val LEFTCALVES: Int = 14
    const val RIGHTCALVES: Int = 15
    const val WAIST: Int = 16
    const val NECK: Int = 17
    const val BEHIND: Int = 18
    const val WEIGHT: Int = 19
    const val FAT: Int = 20
    const val BONES: Int = 21
    const val WATER: Int = 22
    const val MUSCLES: Int = 23
    const val TRAPEZIUS: Int = 24
    const val OBLIQUES: Int = 25
    const val SHOULDERS: Int = 26

    const val TYPE_MUSCLE: Int = 0
    const val TYPE_WEIGHT: Int = 1

    fun getBodyStringID(pBodyID: Int): Int {
        when (pBodyID) {
            ABDOMINAUX -> return R.string.abdominaux
            ADDUCTEURS -> return R.string.adducteurs
            BICEPS -> return R.string.biceps
            TRICEPS -> return R.string.triceps
            DELTOIDS -> return R.string.deltoids
            MOLLETS -> return R.string.mollets
            PECTORAUX -> return R.string.pectoraux
            DORSEAUX -> return R.string.dorseaux
            QUADRICEPS -> return R.string.quadriceps
            ISCHIOJAMBIERS -> return R.string.ischio_jambiers
            LEFTBICEPS -> return R.string.left_arm
            RIGHTBICEPS -> return R.string.right_arm
            LEFTTHIGH -> return R.string.left_thigh
            RIGHTTHIGH -> return R.string.right_thigh
            LEFTCALVES -> return R.string.left_calves
            RIGHTCALVES -> return R.string.right_calves
            WAIST -> return R.string.waist
            NECK -> return R.string.neck
            TRAPEZIUS -> return R.string.trapezius
            OBLIQUES -> return R.string.obliques
            SHOULDERS -> return R.string.shoulders
            BEHIND -> return R.string.behind
            WEIGHT -> return R.string.weightLabel
            FAT -> return R.string.fatLabel
            BONES -> return R.string.bonesLabel
            WATER -> return R.string.waterLabel
            MUSCLES -> return R.string.musclesLabel
        }

        return -1
    }

    fun getBodyLogoID(pBodyID: Int): Int {
        when (pBodyID) {
            ABDOMINAUX -> return R.drawable.ic_chest
            ADDUCTEURS -> return R.drawable.ic_leg
            BICEPS -> return R.drawable.ic_arm
            TRICEPS -> return R.drawable.ic_arm
            DELTOIDS -> return R.drawable.ic_chest
            MOLLETS -> return R.drawable.ic_leg
            PECTORAUX -> return R.drawable.ic_chest_measure
            DORSEAUX -> return R.drawable.ic_chest
            QUADRICEPS -> return R.drawable.ic_leg
            ISCHIOJAMBIERS -> return R.drawable.ic_leg
            LEFTBICEPS -> return R.drawable.ic_arm_measure
            RIGHTBICEPS -> return R.drawable.ic_arm_measure
            LEFTTHIGH -> return R.drawable.ic_tight_measure
            RIGHTTHIGH -> return R.drawable.ic_tight_measure
            LEFTCALVES -> return R.drawable.ic_calve_measure
            RIGHTCALVES -> return R.drawable.ic_calve_measure
            WAIST -> return R.drawable.ic_waist_measure
            NECK -> return R.drawable.ic_neck
            BEHIND -> return R.drawable.ic_buttock_measure
            TRAPEZIUS -> return R.drawable.ic_neck
            OBLIQUES -> return R.string.obliques
            SHOULDERS -> return R.drawable.ic_neck
        }

        return -1
    }
}
