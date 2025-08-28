package com.example.homeal_app.ui.Shopping

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ShoppingViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Test (bis)"
    }
    val text: LiveData<String> = _text
}