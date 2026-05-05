package com.easyfitness.DAO.bodymeasures

import android.content.Context
import android.graphics.drawable.Drawable

/* DataBase Object */
class BodyPart(
    id: Long,
    pBodyPartId: Int,
    pCustomName: String,
    pCustomPicture: String?,
    pDisplayOrder: Int,
    pType: Int
) {
    /**
     * Return legacy Resource Key.
     * @return
     */
    var bodyPartResKey: Int = 0
        private set
    var customName: String = ""
    var customPicture: String? = ""
    var displayOrder: Int = 0
    var type: Int = BodyPartExtensions.TYPE_MUSCLE
        private set
    var id: Long = 0 // Notez que l'identifiant est un long
        private set

    var lastMeasure: BodyMeasure?

    init {
        this.id = id
        this.bodyPartResKey = pBodyPartId
        this.displayOrder = pDisplayOrder
        this.customName = pCustomName
        this.customPicture = pCustomPicture
        this.type = pType
        this.lastMeasure = null
    }

    fun getName(context: Context): String {
        if (!customName.isEmpty()) return this.customName
        else {
            if (this.bodyPartResKey != -1) return context.getResources().getString(
                BodyPartExtensions.getBodyStringID(
                    this.bodyPartResKey
                )
            )
            else return ""
        }
    }

    fun getPicture(context: Context): Drawable? {
        if (this.bodyPartResKey != -1) if (BodyPartExtensions.getBodyLogoID(this.bodyPartResKey) != -1) return context.getDrawable(
            BodyPartExtensions.getBodyLogoID(
                this.bodyPartResKey
            )
        )

        return null
    }


    val resourceNameID: Int
        /**
         * @return Resource ID of the name of the body part
         */
        get() = BodyPartExtensions.getBodyStringID(id.toInt())

    val resourceLogoID: Int
        /**
         * @return Resource ID of the logo
         */
        get() = BodyPartExtensions.getBodyLogoID(id.toInt())
}
