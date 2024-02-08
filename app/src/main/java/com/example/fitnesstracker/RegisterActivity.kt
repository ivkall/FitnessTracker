package com.example.fitnesstracker

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var option: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        sharedPreferences = getSharedPreferences("MyCache", Context.MODE_PRIVATE)

        // Aktivitetstypen väljs bland radioknappar
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val radioButton = findViewById<RadioButton>(checkedId)
            option = radioButton.text as String
        }
    }

    // Konvertera tid i minuter till antal steg vid gång
    fun timeToSteps(timeMinutes: Float): Int {
        val height = MainActivity.sharedPreferences.getFloat("height", 1.8f).toDouble()
        val walkingSpeed = MainActivity.sharedPreferences.getFloat("walkingSpeed", 1.4f).toDouble()
        val distanceMeters = walkingSpeed * timeMinutes * 60
        val stepLength = height  * 0.415
        return (distanceMeters / stepLength).toInt()
    }

    // Returnera dagens datum
    fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-$month-$day"
    }

    // Sparar aktiviteten och dess tid i minnet
    fun saveRegisterActivity(view: View) {
        val week = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        val currentTime = sharedPreferences.getFloat("$option$week", 0f)
        val editor = sharedPreferences.edit()
        val timeHours = findViewById<EditText>(R.id.editTextHours).text.toString().toFloat()
        val timeMinutes = findViewById<EditText>(R.id.editTextMinutes).text.toString().toFloat()
        val timeSeconds = findViewById<EditText>(R.id.editTextSeconds).text.toString().toFloat()
        val totalTimeMinutes = timeHours / 60 + timeMinutes + timeSeconds * 60
        editor.putFloat("$option$week", currentTime + totalTimeMinutes)
        if (option != "Stilla") {
            val addedSteps = sharedPreferences.getInt("added$option${getCurrentDate()}", 0)
            val newAddedSteps = timeToSteps(totalTimeMinutes)
            println("$currentTime $totalTimeMinutes")
            editor.putInt("added$option${getCurrentDate()}", addedSteps + newAddedSteps)
        }
        editor.apply()
        finish()
    }

}