package com.easyfitness

class DrawerItem {
    var itemName: String? = null
    var imgResID: Int = 0
    var img: String? = null
    var title: String? = null
    var isSpinner: Boolean = false
    var isActive: Boolean = false

    constructor(itemName: String?, imgResID: Int, isActive: Boolean) {
        this.itemName = itemName
        this.imgResID = imgResID
        this.isActive = isActive
    }

    constructor(isSpinner: Boolean) {
        this.isSpinner = isSpinner
    }

    constructor(title: String?) {
        this.title = title
    }
}
