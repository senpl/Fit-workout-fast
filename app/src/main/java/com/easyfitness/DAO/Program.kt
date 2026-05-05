package com.easyfitness.DAO

import java.util.Date

class Program(programName: String?, private val profileId: Long,
//              override val date: Date,
//              override var exercise: String?,
//              override var exerciseKey: Long,
//              override val profil: Profile?,
//              override val profilKey: Long,
//              override val time: String?,
//              override val type: Int
) : ARecord() {
    var programName: String? = null
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

    init {
        this.programName = programName
    }
}
