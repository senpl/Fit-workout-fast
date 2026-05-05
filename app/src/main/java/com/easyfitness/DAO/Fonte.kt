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
    pTime: String?,
) : ARecord() {
    // Notez que l'identifiant est un long
    val serie: Int
    val repetition: Int
    val poids: Float
    val unit: Int
    val note: String?
//    override val date: Date
//        get() = TODO("Not yet implemented")
//    override var exercise: String?
//        get() = TODO("Not yet implemented")
//        set(value) {}
//    override var exerciseKey: Long
//        get() = TODO("Not yet implemented")
//        set(value) {}
//    override val profil: Profile?
//        get() = TODO("Not yet implemented")
//    override val profilKey: Long
//        get() = TODO("Not yet implemented")
//    override val time: String?
//        get() = TODO("Not yet implemented")
//    override val type: Int
//        get() = TODO("Not yet implemented")

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
