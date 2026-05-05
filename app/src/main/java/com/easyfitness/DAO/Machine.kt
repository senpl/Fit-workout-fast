package com.easyfitness.DAO

/* DataBase Object */
class Machine(
    var name: String, var description: String?, // Cardio or Fonte
    var type: Int, pBodyParts: String?, pPicture: String?, pFavorite: Boolean?
) {
    // Notez que l'identifiant est un long
    @JvmField
    var id: Long = 0
    var picture: String? = null
    private var mBodyParts: String?
    var favorite: Boolean?

    init {
        this.type = type
        this.picture = pPicture
        this.mBodyParts = pBodyParts
        this.favorite = pFavorite
    }

    var bodyParts: String?
        get() {
            if (mBodyParts == null) return ""
            else return mBodyParts
        }
        set(bodyParts) {
            mBodyParts = bodyParts
        }

    override fun toString(): String {
        return this.name
    }
}
