package com.sunnyweather.android.ui.follows

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.RoomInfo

class FollowViewModel : ViewModel(){
    private val uidLiveData = MutableLiveData<String>()
    var roomList = ArrayList<RoomInfo>()
    var inited = false
    var isLive: Boolean = false

    val userRoomListLiveDate = uidLiveData.switchMap {
            uid -> Repository.getRoomsOn(uid)
    }

    fun getRoomsOn(uid: String?) {
        if(uid != null) {
            uidLiveData.value = uid!!
        }
    }

    fun clearRoomList() {
        roomList.clear()
    }
}