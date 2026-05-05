package com.easyfitness.DAO

import java.util.Date

/* DataBase Object */
class Fonte(
    pDate: Date?,
    pMachine: String?,
    pSerie: Int,
    pRepetition: Int,
    pPoids: Float,
    pProfile: Profile?,
    pUnit: Int,
    pNote: String?,
    pMachineKey: Long,
    pTime: String?
) : ARecord() {
    // Notez que l'identifiant est un long
    val serie: Int
    val repetition: Int
    val poids: Float
    val unit: Int
    val note: String?

    /*
     * Fonte(Date pDate, String pMachine, int pSerie, int pRepetition, int pPoids, Profile pProfile)
     */
    init {
        this.mDate = pDate
        this.mExercise = pMachine
        this.serie = pSerie
        this.repetition = pRepetition
        this.poids = pPoids
        this.unit = pUnit
        this.note = pNote
        this.mProfile = pProfile
        this.mExerciseId = pMachineKey
        this.mTime = pTime
        this.mType = DAOMachine.TYPE_STRENGTH
    }
}
