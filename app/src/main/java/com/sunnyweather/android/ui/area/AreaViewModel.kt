package com.sunnyweather.android.ui.area

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.AreaInfo

class AreaViewModel : ViewModel() {
    private val temp = MutableLiveData<Int>()

    var areaList = ArrayList<AreaInfo>()

    var areaListLiveDate = temp.switchMap {
        Repository.getAllAreas()
    }

    fun getAllAreas() {
        temp.value = 1
    }
}