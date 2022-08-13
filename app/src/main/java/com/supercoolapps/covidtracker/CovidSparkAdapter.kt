package com.supercoolapps.covidtracker

import com.robinhood.spark.SparkAdapter

class CovidSparkAdapter(private val dailyData: List<CovidData>): SparkAdapter() {
    override fun getCount(): Int  = dailyData.size

    override fun getItem(index: Int): Any = dailyData[index]

    override fun getY(index: Int): Float {
        val chosenDayData = dailyData[index]
        return chosenDayData.positiveIncrease.toFloat()
    }
}
