package com.sunnyweather.android.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Owner

class SearchViewModel : ViewModel(){
    class SearchRequest (val platform: String, val keyWords: String, val uid: String)

    private val searchWordLiveData = MutableLiveData<SearchRequest>()
    var ownersList = ArrayList<Owner>()
    val ownerListLiveData = searchWordLiveData.switchMap {
        value -> Repository.search(value.platform, value.keyWords, value.uid)
    }

    fun search(platform: String, keyWords: String, isLive: String) {
        searchWordLiveData.value = SearchRequest(platform, keyWords, isLive)
    }

    fun clearList() {
        ownersList.clear()
    }
}