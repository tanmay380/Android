package com.example.tipcalculator.utils


fun calculateTotalTip(totalBill: Double, tipPercate: Int): Double {
//    Log.d(TAG, "calculateTotalTip: " + totalBill + " " + tipPercate)
    if (totalBill > 1) {
        return (totalBill * tipPercate) / 100
    }
    return 0.0
}

fun calculateTotalPerPerson(
    totalBill: Double,
    splitBy: Int,
    tipPercate: Int
): Double{
    val bill = calculateTotalTip(totalBill, tipPercate) + totalBill
    return (bill/splitBy)
}