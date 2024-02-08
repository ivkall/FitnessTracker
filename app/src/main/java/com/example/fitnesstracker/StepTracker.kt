package com.example.fitnesstracker

import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import java.util.Calendar
import kotlin.math.pow

class StepTracker(preferences: SharedPreferences, manager: SensorManager, main: MainActivity) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var stepCount: Int = 0

    private var weight = 75.0
    private var height = 1.8
    private var walkingSpeed = 1.4
    private var stepGoal = 400

    private var totalWalkingDuration = 0.0
    private var totalRunningDuration = 0.0
    private var totalSittingDuration = 0.0
    private var addedWalkingTime = 0.0
    private var addedRunningTime = 0.0

    private val walkingThreshold = 2.0
    private val runningThreshold = 5.0
    private var startTimeWalking: Long = 0
    private var startTimeRunning: Long = 0

    companion object {
        lateinit var sharedPreferences: SharedPreferences
    }

    private var mainActivity: MainActivity? = null

    init {
        // Läs in användardata
        sharedPreferences = preferences

        sensorManager = manager
        mainActivity = main

        // Upprätta stegsensorn
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            println("no sensor")
        }
    }

    fun registerStepSensor() {
        sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun unregisterListener() {
        sensorManager?.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Används inte
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Uppdatera stegantal när sensorn ändras
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            stepCount = event.values[0].toInt()
            updateStepCount()
        }
    }

    fun loadParameters() {
        weight = sharedPreferences.getFloat("weight", 75f).toDouble()
        height = sharedPreferences.getFloat("height", 1.8f).toDouble()
        walkingSpeed = sharedPreferences.getFloat("walkingSpeed", 1.4f).toDouble()
        stepGoal = sharedPreferences.getInt("stepGoal", 400)
    }

    fun getStepGoal(): Any {
        return stepGoal
    }

    fun getDurations(): Map<String, Double> {
        val week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        val sittingTime = sharedPreferences.getFloat("Stilla$week", 0f)
        val walkingTime = sharedPreferences.getFloat("Gående$week", 0f)
        val runningTime = sharedPreferences.getFloat("Snabbgående/springande$week", 0f)
        addedWalkingTime = walkingTime - totalWalkingDuration
        addedRunningTime = runningTime - totalRunningDuration
        totalSittingDuration = sittingTime.toDouble()
        totalWalkingDuration = walkingTime.toDouble()
        totalRunningDuration = runningTime.toDouble()

        return mapOf(
            "sit" to totalSittingDuration,
            "walk" to totalWalkingDuration,
            "run" to totalRunningDuration
        )
    }

    // Konvertera tid i minuter till antal steg vid gång
    fun timeToSteps(timeMinutes: Float): Int {
        val distanceMeters = walkingSpeed * timeMinutes * 60
        val stepLength = height  * 0.415
        return (distanceMeters / stepLength).toInt()
    }

    // Konvertera antal steg till kalorier som bränns vid gång
    fun stepsToCalories(nbrOfSteps: Int): Int {
        val caloriesPerMinute = 0.035 * weight + walkingSpeed.pow(2.0) * 0.029 * weight / height
        val minutes = nbrOfSteps * height * 0.415 / (walkingSpeed * 60)
        return (minutes * caloriesPerMinute).toInt()
    }

    // Returnera dagens datum
    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-$month-$day"
    }

    // Returnera gårdagens datum
    private fun getYesterdayDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-$month-$day"
    }

    fun updateStepCount() {
        val currentDate = getCurrentDate()
        val yesterdayDate = getYesterdayDate()
        var stepsTakenUntilToday = 0
        if (sharedPreferences.contains("taken$yesterdayDate")) {
            // Om steg rapporterats in för gårdagen hämtas de
            stepsTakenUntilToday = sharedPreferences.getInt("taken$yesterdayDate", 0)
        } else {
            sharedPreferences.edit().putInt("taken$yesterdayDate", stepCount).apply()
        }
        sharedPreferences.edit().putInt("taken$currentDate", stepCount).apply()

        val calendar = Calendar.getInstance()
        val weekDates = ArrayList<String>()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val maxHeight = 180

        for (i in 0..6) {
            // Lägg till varje veckodatum
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            weekDates.add("$year-$month-$day")

            // Steg för dagen är skillnaden mellan totala stegen för dagen och totala stegen för dagen innan
            val totalStepsToday =  sharedPreferences.getInt("taken${weekDates[i]}", stepCount)
            var totalStepsYesterday = 0
            if (i > 0) {
                totalStepsYesterday = sharedPreferences.getInt("taken${weekDates[i - 1]}", totalStepsToday)
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                val date = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
                totalStepsYesterday = sharedPreferences.getInt("taken$date", totalStepsToday)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            val weekDaySteps = totalStepsToday - totalStepsYesterday

            // Uppdatera steplhöjd efter antal steg
            val weekDayBar = mainActivity?.getBar("barView$i")
            if (weekDayBar != null) {
                weekDayBar.layoutParams.height = (weekDaySteps.toDouble() / stepGoal * maxHeight).toInt()
                weekDayBar.requestLayout()
                weekDayBar.invalidate()
            }

            // Hoppa till nästa dag
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        // Räkna ut steg och kalorier för hemskärmens översta vy
        val stepsRunning = sharedPreferences.getInt("addedSnabbgående/springande${getCurrentDate()}", 0)
        val stepsWalking = sharedPreferences.getInt("taken${getCurrentDate()}", 0) + sharedPreferences.getInt("addedGående${getCurrentDate()}", 0)
        val totalSteps = stepsWalking + stepsRunning - stepsTakenUntilToday
        val burnedCalsWalking = stepsToCalories(stepsWalking - stepsTakenUntilToday)
        val burnedCalsRunning = (stepsToCalories(stepsRunning) * 1.5).toInt()
        val consumedCals = sharedPreferences.getInt("cals${getCurrentDate()}", 0)
        val netCals = (burnedCalsWalking + burnedCalsRunning)?.minus(consumedCals)
        println("$totalSteps $stepsTakenUntilToday")
        mainActivity?.updateStepAndCalText(totalSteps, netCals)
    }

    // Lyssnar efter accelerationsändringar
    private val linearAccelerationListener = object : SensorEventListener {

        var lastUpdateTimestamp = System.currentTimeMillis()
        var isSitting = true
        val week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)

        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                // Räkna ut storleken på accelerationen
                val linearAccelerationMagnitude = Math.sqrt(
                    event.values[0].toDouble().pow(2.0) +
                            event.values[1].toDouble().pow(2.0) +
                            event.values[2].toDouble().pow(2.0)
                )

                // Kolla om användaren går eller springer
                if (linearAccelerationMagnitude >= runningThreshold) {
                    // Springer upptäcks
                    if (startTimeRunning == 0L) {
                        startTimeRunning = System.currentTimeMillis()

                        isSitting = false
                    }
                } else if (linearAccelerationMagnitude >= walkingThreshold) {
                    // Går upptäcks
                    if (startTimeWalking == 0L) {
                        startTimeWalking = System.currentTimeMillis()

                        isSitting = false
                    }
                } else {
                    // Varken går eller springer
                    if (startTimeWalking > 0L) {
                        // Slutat gå
                        val walkingDuration = System.currentTimeMillis() - startTimeWalking
                        startTimeWalking = 0L
                        isSitting = true
                        lastUpdateTimestamp = System.currentTimeMillis()

                        // Addera gångtid i minuter
                        totalWalkingDuration += walkingDuration.toDouble() / (1000 * 60)

                        // Visa gångtid
                        mainActivity?.setViewText(R.id.textViewTimeWalking, "${String.format("%.1f", totalWalkingDuration)} min")

                        // Spara gångtid
                        sharedPreferences.edit().putFloat("Gående$week", totalWalkingDuration.toFloat()).apply()
                    }

                    if (startTimeRunning > 0L) {
                        // Slutat springa
                        val runningDuration = System.currentTimeMillis() - startTimeRunning
                        startTimeRunning = 0L
                        isSitting = true
                        lastUpdateTimestamp = System.currentTimeMillis()

                        // Addera springtid i minuter
                        totalRunningDuration += runningDuration.toDouble() / (1000 * 60)

                        // Visa springtid
                        mainActivity?.setViewText(R.id.textViewTimeRunning, "${String.format("%.1f", totalRunningDuration)} min")

                        // Spara springtid
                        sharedPreferences.edit().putFloat("Snabbgående/springande$week", totalRunningDuration.toFloat()).apply()
                    }

                    if (isSitting) {
                        // Sitter
                        totalSittingDuration += (System.currentTimeMillis() - lastUpdateTimestamp).toDouble() / (1000 * 60)
                        lastUpdateTimestamp = System.currentTimeMillis()

                        // Visa sittid
                        mainActivity?.setViewText(R.id.textViewTimeSitting, "${String.format("%.1f", totalSittingDuration)} min")

                        // Spara sittid
                        sharedPreferences.edit().putFloat("Stilla$week", totalSittingDuration.toFloat()).apply()
                    }
                }
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            // Används inte
        }
    }

    // Upprätta accelerationssensorn
    fun initAccSensor() {
        val linearAccelerationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        if (linearAccelerationSensor != null) {
            sensorManager?.registerListener(
                linearAccelerationListener,
                linearAccelerationSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        } else {
            // Accelerationssensorn är inte tillgänglig
        }
    }

}