package com.easyfitness.DAO

import java.util.Date

interface IRecord {
    var id: Long

    val date: Date

    var exercise: String?

    var exerciseKey: Long

    val profil: Profile?

    val profilKey: Long

    val time: String?

    val type: Int
}
