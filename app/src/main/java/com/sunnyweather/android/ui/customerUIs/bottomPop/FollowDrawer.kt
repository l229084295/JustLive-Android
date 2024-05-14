package com.sunnyweather.android.ui.customerUIs.bottomPop

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.drake.net.Get
import com.drake.net.utils.scopeNetLife
import com.lxj.xpopup.core.DrawerPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.RoomInfo
import com.sunnyweather.android.R

class FollowDrawer(context: Context, private val lifecycleOwner: LifecycleOwner): DrawerPopupView(context) {

    override fun getImplLayoutId(): Int {
        return R.layout.custom_follow_drawer
    }

    override fun getMaxWidth(): Int {
        return (XPopupUtils.getScreenWidth(context) * 0.5).toInt()
    }

    override fun onCreate() {
        super.onCreate()
        val recyclerView = findViewById<RecyclerView>(R.id.player_follow_recyclerView)
        val playerFollowProgressBar = findViewById<ProgressBar>(R.id.player_follow_progressBar)
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        var adapterFollow = FollowAdapter()

        recyclerView.adapter = adapterFollow
        lifecycleOwner.scopeNetLife { // 创建作用域
            // 这个大括号内就属于作用域内部
            if (SunnyWeatherApplication.isLogin!!.value == true) {
                playerFollowProgressBar.visibility = View.VISIBLE
                val userInfo = SunnyWeatherApplication.userInfo
                val url = "http://yj1211.work:8013/api/live/getRoomsOn?uid=" + userInfo!!.uid
                val data = Get<String>(url).await() // 发起GET请求并返回`String`类型数据
                var result: JSONArray = JSONObject.parseObject(data).getJSONArray("data")
                for (item in result) {
                    item as JSONObject
                    if (item.getInteger("isLive") == 1) {
                        var roomInfo = RoomInfo(item.getString("roomId"),item.getString("platForm"),
                            "",item.getString("ownerHeadPic"),
                            item.getString("ownerName"),
                            item.getString("roomName"),"","",0,0,0,"", item.getBoolean("isRecord"))
                        adapterFollow.addData(roomInfo)
                    }
                }
                playerFollowProgressBar.visibility = View.GONE
            }
        }
    }
}