package com.easyfitness.DAO

import java.util.Date

/* DataBase Object */
class StaticExercise(
    pDate: Date?,
    pMachine: String?,
    pSerie: Int,
    pSecond: Int,
    pPoids: Float,
    pProfile: Profile?,
    pUnit: Int,
    pMachineKey: Long,
    pTime: String?,
//    override val date: Date,
//    override var exercise: String?,
//    override var exerciseKey: Long,
//    override val profil: Profile?,
//    override val profilKey: Long,
//    override val time: String?,
//    override val type: Int
) : ARecord() {
    // Notez que l'identifiant est un long
    val serie: Int
    val second: Int
    val poids: Float
    val unit: Int
    val note: String? = null
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
     * Fonte(Date pDate, String pMachine, int pSerie, int pSecond, int pPoids, Profile pProfile)
     */
    init {
        this.mDate = pDate
        this.mExercise = pMachine
        this.serie = pSerie
        this.second = pSecond
        this.poids = pPoids
        this.unit = pUnit
        this.mProfile = pProfile
        this.mExerciseId = pMachineKey
        this.mTime = pTime
        this.mType = DAOMachine.TYPE_STATIC
    }
}
