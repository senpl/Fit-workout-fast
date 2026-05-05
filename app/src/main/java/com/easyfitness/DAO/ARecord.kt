package com.easyfitness.DAO

import java.util.Date

/* DataBase Object */
abstract class ARecord : IRecord {
    override var id: Long = 0

    var mDate: Date? = null
    override val date: Date
        get() = mDate ?: Date()

    protected var mExercise: String? = null
    override var exercise: String?
        get() = mExercise
        set(value) {
            this.mExercise = value
        }

    protected var mExerciseId: Long = 0
    override var exerciseKey: Long
        get() = mExerciseId
        set(value) {
            this.mExerciseId = value
        }

    protected var mProfile: Profile? = null
    override val profil: Profile?
        get() = mProfile

    override val profilKey: Long
        get() = mProfile?.id ?: -1

    protected var mTime: String? = null // Time in HH:MM:SS
    override val time: String?
        get() = mTime

    protected var mType: Int = 0 // TYPE_FONTE or TYPE_STATIC or TYPE_CARDIO
    override val type: Int
        get() = mType

    constructor() : super()

    constructor(
        pDate: Date?,
        pMachine: String?,
        pProfile: Profile,
        pMachineKey: Long,
        pTime: String?,
        pType: Int
    ) : super() {
        this.mDate = pDate
        this.mExercise = pMachine
        this.mProfile = pProfile
        this.mExerciseId = pMachineKey
        this.mTime = pTime
        this.mType = pType
    }
}
