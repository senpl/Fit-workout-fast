package com.easyfitness.DAO

import java.util.Date

/* DataBase Object */
class Cardio(
    pDate: Date?,
    pExercice: String?,
    pDistance: Float,
    pDuration: Long,
    pProfile: Profile?,
    pTime: String?,
    pDistanceUnit: Int,
) : ARecord() {
    // Notez que l'identifiant est un long
    val distance: Float
    val duration: Long
    val distanceUnit: Int
    override val date: Date
        get() = TODO("Not yet implemented")
    override var exercise: String?
        get() = TODO("Not yet implemented")
        set(value) {}
    override var exerciseKey: Long
        get() = TODO("Not yet implemented")
        set(value) {}
    override val profil: Profile?
        get() = TODO("Not yet implemented")
    override val profilKey: Long
        get() = TODO("Not yet implemented")
    override val time: String?
        get() = TODO("Not yet implemented")
    override val type: Int
        get() = TODO("Not yet implemented")

    init {
        this.mDate = pDate
        this.mExercise = pExercice
        this.distance = pDistance
        this.duration = pDuration
        this.mProfile = pProfile
        this.mTime = pTime
        this.distanceUnit = pDistanceUnit
        this.mType = DAOMachine.TYPE_CARDIO
    }
}
