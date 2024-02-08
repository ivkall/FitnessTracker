package com.example.fitnesstracker

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        sharedPreferences = getSharedPreferences("MyCache", Context.MODE_PRIVATE)

        // Läs in nuvarande parametervärden från minnet
        val weight = MainActivity.sharedPreferences.getFloat("weight", 75f).toString()
        val height = MainActivity.sharedPreferences.getFloat("height", 1.8f).toString()
        val walkingSpeed = MainActivity.sharedPreferences.getFloat("walkingSpeed", 1.4f).toString()
        val stepGoal = MainActivity.sharedPreferences.getInt("stepGoal", 400).toString()

        // Uppdatera textvärden
        findViewById<EditText>(R.id.editTextWeight).setText(weight)
        findViewById<EditText>(R.id.editTextHeight).setText(height)
        findViewById<EditText>(R.id.editTextWalkingSpeed).setText(walkingSpeed)
        findViewById<EditText>(R.id.editTextStepGoal).setText(stepGoal)
    }

    fun saveSettings(view: View) {
        // Hämta inmatade värden på parametrar
        val weight = findViewById<EditText>(R.id.editTextWeight).text.toString().toFloat()
        val height = findViewById<EditText>(R.id.editTextHeight).text.toString().toFloat()
        val walkingSpeed = findViewById<EditText>(R.id.editTextWalkingSpeed).text.toString().toFloat()
        val stepGoal = findViewById<EditText>(R.id.editTextStepGoal).text.toString().toInt()

        // Spara värden i minnet
        val editor = sharedPreferences.edit()
        editor.putFloat("weight", weight)
        editor.putFloat("height", height)
        editor.putFloat("walkingSpeed", walkingSpeed)
        editor.putInt("stepGoal", stepGoal)
        editor.apply()
    }
}