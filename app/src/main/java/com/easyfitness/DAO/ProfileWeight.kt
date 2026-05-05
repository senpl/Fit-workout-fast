package com.easyfitness.DAO

import java.util.Date

/* DataBase Object */
class ProfileWeight(// Notez que l'identifiant est un long
    var id: Long, val date: Date?, val weight: Float, val profilId: Long
)
