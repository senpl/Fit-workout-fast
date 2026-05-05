package com.easyfitness.DAO.bodymeasures

import java.util.Date

/* DataBase Object */
class BodyMeasure(// Notez que l'identifiant est un long
    var id: Long, val date: Date?,
    /**
     * @return long Body Part ID
     */
    val bodyPartID: Int, val bodyMeasure: Float, val profileID: Long
)
