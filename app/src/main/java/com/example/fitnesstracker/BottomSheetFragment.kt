package com.example.fitnesstracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

class BottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Visa menyn
        val view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false)

        // Menyns knappar som öppnar respektive aktivitet
        val registerButton = view.findViewById<Button>(R.id.button)
        registerButton.setOnClickListener {
            startRegisterActivity()
        }
        val registerMealButton = view.findViewById<Button>(R.id.button2)
        registerMealButton.setOnClickListener {
            startRegisterMealActivity()
        }

        return view
    }

    private fun startRegisterActivity() {
        val intent = Intent(activity, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun startRegisterMealActivity() {
        val intent = Intent(activity, RegisterMealActivity::class.java)
        startActivity(intent)
    }
}