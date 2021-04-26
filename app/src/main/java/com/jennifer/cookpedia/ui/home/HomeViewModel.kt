package com.jennifer.cookpedia.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jennifer.cookpedia.entity.Person

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<Person>()

    fun setSelectedNews(person: Person) {
        _text.value = person
    }
    fun getSelectedNews() = _text.value

    val text: LiveData<Person> = _text
}