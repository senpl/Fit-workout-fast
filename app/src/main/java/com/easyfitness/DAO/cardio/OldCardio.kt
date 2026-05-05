package com.easyfitness.DAO.cardio

import com.easyfitness.DAO.Profile
import java.util.Date

/* DataBase Object */
class OldCardio(
    val date: Date?,
    val exercice: String?,
    val distance: Float,
    val duration: Long,
    val profil: Profile?
) {
    // Notez que l'identifiant est un long
    var id: Long = 0
}
