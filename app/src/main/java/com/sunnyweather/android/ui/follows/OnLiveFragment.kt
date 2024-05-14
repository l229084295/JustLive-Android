package com.sunnyweather.android.ui.follows

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.scwang.smart.refresh.header.ClassicsHeader
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.RoomInfo
import com.sunnyweather.android.ui.roomList.RoomListAdapter
import com.sunnyweather.android.ui.roomList.SpaceItemDecoration
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.FragmentRoomlistBinding

class OnLiveFragment(private val isLive: Boolean) : Fragment() {
    constructor() : this(true)
    private val viewModel by lazy { ViewModelProvider(this).get(FollowViewModel::class.java) }
    private lateinit var adapterOn: RoomListAdapter
    var binding: FragmentRoomlistBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRoomlistBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (!viewModel.inited) {
            viewModel.isLive = this.isLive
        }
        var cardNum = ScreenUtils.getAppScreenWidth()/ ConvertUtils.dp2px(195F)
        if (cardNum < 2) cardNum = 2
        val layoutManager = GridLayoutManager(context, cardNum)
        binding?.apply {
            recyclerView.addItemDecoration(SpaceItemDecoration(10))
            recyclerView.layoutManager = layoutManager
            adapterOn = RoomListAdapter(this@OnLiveFragment, viewModel.roomList)
            recyclerView.adapter = adapterOn
            //下拉刷新，加载更多
            refreshHome.setRefreshHeader(ClassicsHeader(context))
            refreshHome.finishLoadMoreWithNoMoreData()
            refreshHome.setOnRefreshListener {
                if (!SunnyWeatherApplication.isLogin.value!!) {
                    refreshHome.finishRefresh() //传入false表示刷新失败
                    refreshHome.finishLoadMoreWithNoMoreData()
                    return@setOnRefreshListener
                }
                viewModel.getRoomsOn(SunnyWeatherApplication.userInfo?.uid)
            }

            SunnyWeatherApplication.isLogin.observe(viewLifecycleOwner) { result ->
                if (!viewModel.inited && result) {
                    viewModel.inited = true
                    progressBarRoomList.isVisible = true
                    viewModel.clearRoomList()
                    adapterOn.notifyDataSetChanged()
                    viewModel.getRoomsOn(SunnyWeatherApplication.userInfo?.uid)
                } else if (!result) {
                    viewModel.inited = false
                    viewModel.clearRoomList()
                    adapterOn.notifyDataSetChanged()
                }
            }
            viewModel.userRoomListLiveDate.observe(viewLifecycleOwner) { result ->
                val rooms = result.getOrNull()
                if (rooms is ArrayList<*>) {
                    viewModel.clearRoomList()
                    sortRooms(rooms as List<RoomInfo>)
                    adapterOn.notifyDataSetChanged()
                    progressBarRoomList.isGone = true
                    refreshHome.finishRefresh() //传入false表示刷新失败
                    refreshHome.finishLoadMoreWithNoMoreData()
                    (this@OnLiveFragment.parentFragment as FollowsFragment).enableInput()
                } else if (rooms is String) {
                    Toast.makeText(context, rooms, Toast.LENGTH_SHORT).show()
                    result.exceptionOrNull()?.printStackTrace()
                }
            }

            if (SunnyWeatherApplication.isLogin.value!!){
                progressBarRoomList.isVisible = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SunnyWeatherApplication.isLogin.removeObservers(viewLifecycleOwner)
        viewModel.userRoomListLiveDate.removeObservers(viewLifecycleOwner)
    }

    private fun sortRooms(roomList: List<RoomInfo>) {
        for (roomInfo in roomList) {
            if (viewModel.isLive == (roomInfo.isLive == 1) && !roomInfo.ownerName.isNullOrEmpty()) {
                viewModel.roomList.add(roomInfo)
            }
        }
    }
}