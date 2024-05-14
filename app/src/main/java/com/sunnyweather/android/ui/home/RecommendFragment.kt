package com.sunnyweather.android.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.ScreenUtils
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.RoomInfo
import com.sunnyweather.android.ui.roomList.RoomListAdapter
import com.sunnyweather.android.ui.roomList.SpaceItemDecoration
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.FragmentRoomlistBinding

class RecommendFragment(val platform: String) : Fragment()  {
    constructor(): this("all")
    private val viewModel by lazy { ViewModelProvider(this, HomeViewModelFactory(platform)).get(HomeViewModel::class.java) }
    private lateinit var adapter: RoomListAdapter
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
        var cardNum = ScreenUtils.getAppScreenWidth()/ConvertUtils.dp2px(195F)
        if (cardNum < 2) cardNum = 2
        val layoutManager = GridLayoutManager(context, cardNum)
        binding?.apply {
            recyclerView.addItemDecoration(SpaceItemDecoration(10))
            recyclerView.layoutManager = layoutManager
            adapter = RoomListAdapter(this@RecommendFragment, viewModel.roomList)
            recyclerView.adapter = adapter
            //下拉刷新，加载更多
            refreshHomeFoot.setFinishDuration(0)//设置Footer 的 “加载完成” 显示时间为0
            refreshHome.setOnRefreshListener {
                viewModel.clearPage()
                viewModel.getRecommend(SunnyWeatherApplication.areaType.value?:"all", SunnyWeatherApplication.areaName.value?:"all", state)
            }
            refreshHome.setOnLoadMoreListener {
                viewModel.getRecommend(SunnyWeatherApplication.areaType.value?:"all", SunnyWeatherApplication.areaName.value?:"all", state)
            }
            //绑定LiveData监听器
            SunnyWeatherApplication.areaName.observe(viewLifecycleOwner) {
                viewModel.clearPage()
                viewModel.clearList()
                progressBarRoomList.isVisible = true
                recyclerView.isGone = true
                viewModel.getRecommend(
                    SunnyWeatherApplication.areaType.value ?: "all",
                    SunnyWeatherApplication.areaName.value ?: "all",
                    state
                )
            }
            viewModel.roomListLiveDate.observe(viewLifecycleOwner) { result ->
                val temp = result.getOrNull()
                var rooms: ArrayList<RoomInfo>? = null
                if (temp != null) rooms = temp as ArrayList<RoomInfo>
                if (rooms != null && rooms.size > 0) {
                    if (refreshHome.isRefreshing) {
                        viewModel.clearList()
                    }
                    viewModel.roomList.addAll(rooms)
                    adapter.notifyDataSetChanged()
                    progressBarRoomList.isGone = true
                    recyclerView.isVisible = true
                    refreshHome.finishRefresh() //传入false表示刷新失败
                    refreshHome.finishLoadMore() //传入false表示加载失败
                } else {
                    progressBarRoomList.isGone = true
                    recyclerView.isVisible = true
                    refreshHome.finishLoadMoreWithNoMoreData()
                    if (viewModel.roomList.size == 0) {
                        state.showEmpty()
                    }
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
            viewModel.getRecommend(SunnyWeatherApplication.areaType.value?:"all", SunnyWeatherApplication.areaName.value?:"all", state)
            progressBarRoomList.isVisible = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SunnyWeatherApplication.areaName.removeObservers(viewLifecycleOwner)
        viewModel.roomListLiveDate.removeObservers(viewLifecycleOwner)
    }
}