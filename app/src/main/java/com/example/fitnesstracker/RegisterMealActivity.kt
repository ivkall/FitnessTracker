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

class RegisterMealActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var option: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_meal)
        sharedPreferences = getSharedPreferences("MyCache", Context.MODE_PRIVATE)
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-$month-$day"
    }

    // Sparar måltidens kalorier i minnet
    fun saveRegisterMeal(view: View) {
        val today = getCurrentDate()
        val consumedCals = sharedPreferences.getInt("cals$today", 0)
        val mealCals = findViewById<EditText>(R.id.editTextCals).text.toString().toInt()
        val editor = sharedPreferences.edit()
        editor.putInt("newCals$today", mealCals)
        editor.putInt("cals$today", consumedCals + mealCals)
        editor.apply()
        finish()
    }

}