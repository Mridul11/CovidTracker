package com.supercoolapps.covidtracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.TextView
import com.google.gson.GsonBuilder
import com.robinhood.spark.SparkView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var sparkView: SparkView
    private lateinit var radioButtonPositive: RadioButton
    private lateinit var radioButtonMax: RadioButton
    private lateinit var tvDateLabel: TextView
    private lateinit var tvMetricLabel:TextView
    private lateinit var perStateDailyData: Map<String, List<CovidData>>
    private lateinit var nationalDailyData: List<CovidData>

    companion object{
        private const val BASE_URL = "https://api.covidtracking.com/v1/"
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvMetricLabel = findViewById(R.id.tvMetricLabel)
        tvDateLabel = findViewById(R.id.tvDateLabel)
        radioButtonPositive = findViewById(R.id.radioButtonPositive)
        radioButtonMax = findViewById(R.id.radioButtonMax)
        sparkView = findViewById(R.id.sparkView)

        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val covidService = retrofit.create(CovidService::class.java)
        covidService.getNationalData().enqueue(object : Callback<List<CovidData>>{
            override fun onResponse(
                call: Call<List<CovidData>>,
                response: Response<List<CovidData>>
            ) {
                Log.e(TAG, "onSuccess! $response")
                val nationalData = response.body()
                if(nationalData == null){
                    Log.w(TAG, "Something went wrong!")
                    return
                }
                nationalDailyData = nationalData.reversed()
                Log.e(TAG, "Update graph with national data")
                updateDisplayWithData(nationalDailyData)
            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG, "onFailure! $t")
            }
        })

        // Fetch the State data
        covidService.getStateData().enqueue(object : Callback<List<CovidData>>{
            override fun onResponse(
                call: Call<List<CovidData>>,
                response: Response<List<CovidData>>
            ) {
                Log.e(TAG, "onSuccess! $response")
                val statesData = response.body()
                if( statesData == null){
                    Log.i(TAG, "Something went wrong!")
                    return
                }
                perStateDailyData = statesData.reversed().groupBy { it.state }
                Log.i(TAG, "Update spinner with state data!")
                // TODO: Update spinner with state names

            }

            override fun onFailure(call: Call<List<CovidData>>, t: Throwable) {
                Log.e(TAG, "onFailure! $t")
                return
            }
        })
    }

    private fun updateDisplayWithData(dailyData: List<CovidData>) {
        // Create a spark adapter
        val adapter = CovidSparkAdapter(dailyData)
        sparkView.adapter = adapter
//        Update radio buttons to select the positive  cases and max by default
//        Display matric for the most recent data
        radioButtonPositive.isChecked = true
        radioButtonMax.isChecked = true
        updateInfoForDate(dailyData.last())
    }

    private fun updateInfoForDate(covidData: CovidData) {
        tvMetricLabel.text = NumberFormat.getInstance().format(covidData.positiveIncrease)
        val outputDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        tvDateLabel.text =  outputDateFormat.format(covidData.dateChecked)
    }
}