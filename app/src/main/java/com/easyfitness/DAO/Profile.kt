package com.easyfitness.DAO

import com.easyfitness.utils.Gender
import java.util.Date

/* DataBase Object */
class Profile {
    @JvmField
    var id: Long = 0
    var creationDate: Date? = null
        private set
    var birthday: Date? = null
    var name: String = ""

    /**
     * @return size in centimeter
     */
    var size: Int = 0
    var gender: Int = Gender.MALE
    var photo: String = ""

    constructor(
        mId: Long,
        mDate: Date?,
        pName: String?,
        pSize: Int,
        pBirthday: Date?,
        pPhoto: String?,
        pGender: Int
    ) {
        //super();
        this.id = mId
        this.creationDate = mDate
        this.birthday = pBirthday
        this.size = pSize
        this.name = pName ?: ""
        this.photo = pPhoto ?: ""
        this.gender = pGender
    }

    constructor(pName: String?, pSize: Int, pBirthday: Date?, pGender: Int) {
        //super();
        this.birthday = pBirthday
        this.size = pSize
        this.name = pName ?: ""
        this.gender = pGender
    }

    fun equals(p: Profile?): Boolean {
        var birthdayEquals = false
        if (p == null) return false
        if (this.birthday == null && p.birthday == null) birthdayEquals = true
        else if (this.birthday == null && p.birthday != null) birthdayEquals = false
        else if (this.birthday != null && p.birthday == null) birthdayEquals = false
        else if (p.birthday != this.birthday) birthdayEquals = false

        return birthdayEquals && p.name == this.name && p.size == this.size && p.gender == this.gender && p.photo == this.photo
    }
}
