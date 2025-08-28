package com.example.homeal_app.ui.Fridge

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FridgeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Hello new world!"
    }
    val text: LiveData<String> = _text
}