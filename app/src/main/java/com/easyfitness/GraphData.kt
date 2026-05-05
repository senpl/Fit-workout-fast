package com.easyfitness

class GraphData {
    // in days
    @JvmField
    val x: Double
    @JvmField
    val y: Double

    constructor(x: Double, y: Double) {
        this.x = x
        this.y = y
    }

    constructor(x: Double, y: Double, y_unit: Int) {
        this.x = x
        this.y = y
    }
}
