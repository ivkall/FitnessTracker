package com.example.fitnesstracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import java.util.Calendar
import kotlin.math.pow
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var stepSensor: Sensor? = null

    companion object {
        lateinit var sharedPreferences: SharedPreferences
    }

    private var stepTracker: StepTracker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Läs in användardata
        sharedPreferences = getSharedPreferences("MyCache", Context.MODE_PRIVATE)

        setContentView(R.layout.activity_main)

        // Upprätta sensorer i StepTracker
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepTracker = StepTracker(sharedPreferences, sensorManager, this)
        stepTracker!!.initAccSensor()

        // Visa veckonummer
        val weekOfYear = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        findViewById<TextView>(R.id.aktivitetDiagramTextView).text = "Aktivitet v$weekOfYear"
        findViewById<TextView>(R.id.tidSpenderadTextView).text = "Tid spenderad v$weekOfYear"
    }

    override fun onResume() {
        super.onResume()

        // Lyssnar efter ändringar i steg
        stepTracker?.registerStepSensor()

        // Läs in sparade parametrar
        stepTracker?.loadParameters()

        // Visa stegmål i diagram
        val stepGoal = stepTracker?.getStepGoal()
        findViewById<TextView>(R.id.textViewStepGoal).text = stepGoal.toString()

        // Läs in sparade aktivitetstider och uppdatera total tid
        val duration = stepTracker?.getDurations()

        // Visa totala aktivitetstider
        findViewById<TextView>(R.id.textViewTimeSitting).text = "${String.format("%.1f", duration?.get("sit"))} min"
        findViewById<TextView>(R.id.textViewTimeWalking).text = "${String.format("%.1f", duration?.get("walk"))} min"
        findViewById<TextView>(R.id.textViewTimeRunning).text = "${String.format("%.1f", duration?.get("run"))} min"

        // Uppdatera nuvarande steg och kalorier
        stepTracker?.updateStepCount()
    }

    override fun onPause() {
        super.onPause()
        // Avregistrera sensorlyssnare när appen inte är igång
        stepTracker?.unregisterListener()
    }

    // Visar steg och kalorier i hemskärmens översta vy
    fun updateStepAndCalText(steps: Int, cals: Int?) {
        findViewById<TextView>(R.id.stepCountTextView).text = "$steps steg"
        findViewById<TextView>(R.id.calorieCountTextView).text = "$cals kcal"
    }

    // Visa registreringsalternativ
    fun showBottomSheet(view: View) {
        val bottomSheetFragment = BottomSheetFragment()
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    // Visa menyn högst upp
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Menyknappen öppnar inställningarna
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_button -> {
                startSettingsActivity()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Öppna inställningar
    fun startSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun getBar(barId: String): View? {
        return findViewById<View>(resources.getIdentifier(barId, "id", packageName))
    }

    fun setViewText(id: Int, text: String) {
        findViewById<TextView>(id).text = text
    }

}