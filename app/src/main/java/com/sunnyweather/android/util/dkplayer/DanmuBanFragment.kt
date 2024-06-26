package com.sunnyweather.android.util.dkplayer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.UserInfo
import com.sunnyweather.android.ui.liveRoom.LiveRoomActivity
import com.sunnyweather.android.ui.login.LoginActivity
import com.sunnyweather.android.ui.login.LoginViewModel
import com.sunnyweather.android.databinding.FragmentDanmuBannedBinding

class DanmuBanFragment: Fragment() {
    private val viewModel by lazy { ViewModelProvider(this).get(LoginViewModel::class.java) }
    private var banContents =  ArrayList<String>()
    private var isSelectedArray =  ArrayList<String>()
    private var loadedBan = false
    var binding: FragmentDanmuBannedBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDanmuBannedBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (SunnyWeatherApplication.isLogin.value!!) {
            binding?.activeBan?.isChecked = SunnyWeatherApplication.userInfo!!.isActived == "1"
            val banContentsString = SunnyWeatherApplication.userInfo!!.allContent
            val isSelectString = SunnyWeatherApplication.userInfo!!.selectedContent
            if (banContentsString != ""){
                if (banContentsString.contains(";")){
                    banContents = banContentsString.split(";") as ArrayList<String>
                } else {
                    banContents.add(banContentsString)
                }
            }
            if (isSelectString != ""){
                if (isSelectString.contains(";")) {
                    isSelectedArray = isSelectString.split(";") as ArrayList<String>
                } else {
                    isSelectedArray.add(isSelectString)
                }

            }
            addClips()
        }
        if (SunnyWeatherApplication.isLogin.value!!) {
            binding?.activeBan?.setOnCheckedChangeListener { buttonView, isChecked ->
                if (SunnyWeatherApplication.isLogin.value!!) {
                    saveBanActive(isChecked)
                    val context = context as LiveRoomActivity
                    context.changeBanActive(isChecked)
                    if (isChecked) {
                        Toast.makeText(context, "开启屏蔽", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "关闭屏蔽", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            binding?.activeBan?.visibility = View.INVISIBLE
            binding?.activeBanTxt?.visibility = View.VISIBLE
            binding?.activeBanTxt?.setOnClickListener {
                //没登录去登陆
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("提醒")
                    .setMessage("登录后开启弹幕屏蔽")
                    .setCancelable(true)
                    .setNegativeButton("取消") { _, _ ->

                    }
                    .setPositiveButton("登录") { _, _ ->
                        val intent = Intent(SunnyWeatherApplication.context, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    .show()
            }
        }

        binding?.addBanBtn?.setOnClickListener {
            //没登录去登陆
            if (!SunnyWeatherApplication.isLogin.value!!) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("提醒")
                    .setMessage("登录后添加弹幕屏蔽")
                    .setCancelable(true)
                    .setNegativeButton("取消") { _, _ ->

                    }
                    .setPositiveButton("登录") { _, _ ->
                        val intent = Intent(SunnyWeatherApplication.context, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    .show()
            }else {
                val text = binding?.addBanContent?.text.toString() //新增屏蔽内容
                if (text.isEmpty()) {
                    Toast.makeText(context, "不能为空", Toast.LENGTH_SHORT).show()
                } else if (banContents.contains(text)) {
                    Toast.makeText(context, "已存在", Toast.LENGTH_SHORT).show()
                } else {
                    banContents.add(text)
                    isSelectedArray.add(text)
                    saveBanInfo(banContents, isSelectedArray)
                    val chip = createNewChip(text, true)
                    binding?.banChipGroup?.addView(chip)
                    binding?.addBanContent?.setText("")
                    binding?.addBanTextField?.clearFocus()
                }
            }
        }
        SunnyWeatherApplication.isLogin.observe(viewLifecycleOwner) { result ->
            if (!loadedBan && result) {
                binding?.activeBan?.visibility = View.VISIBLE
                binding?.activeBanTxt?.visibility = View.GONE
                val isActive = SunnyWeatherApplication.userInfo!!.isActived == "1"
                val context = context as LiveRoomActivity
                binding?.activeBan?.isChecked = isActive
                context.changeBanActive(isActive)
                binding?.activeBan?.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (SunnyWeatherApplication.isLogin!!.value!!) {
                        saveBanActive(isChecked)
                        context.changeBanActive(isChecked)
                        if (isChecked) {
                            Toast.makeText(context, "开启屏蔽", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "关闭屏蔽", Toast.LENGTH_SHORT).show()
                        }
                    } else {

                    }
                }
                val banContentsString = SunnyWeatherApplication.userInfo!!.allContent
                val isSelectString = SunnyWeatherApplication.userInfo!!.selectedContent
                if (banContentsString != "") {
                    if (banContentsString.contains(";")) {
                        banContents = banContentsString.split(";") as ArrayList<String>
                    } else {
                        banContents.add(banContentsString)
                    }
                }
                if (isSelectString != "") {
                    if (isSelectString.contains(";")) {
                        isSelectedArray = isSelectString.split(";") as ArrayList<String>
                    } else {
                        isSelectedArray.add(isSelectString)
                    }

                }
                addClips()
            }
        }
        viewModel.updateUserInfoLiveDate.observe(viewLifecycleOwner) { result ->
            val temp = result.getOrNull()
            if (temp is UserInfo) {
                SunnyWeatherApplication.userInfo = temp
                Log.i("test", temp.toString())
                Toast.makeText(context, "屏蔽规则更新成功", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, temp.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i("test", "onPause")
    }

    //根据list生产clips
    private fun addClips() {
        loadedBan = true
        for (banContent in banContents) {
            val chip = createNewChip(banContent, isSelectedArray.contains(banContent))
            binding?.banChipGroup?.addView(chip)
        }
    }

    //创建chip
    private fun createNewChip(banContent: String, checked: Boolean): Chip{
        val chip = Chip(context)
        chip.isCloseIconVisible = true
        chip.text = banContent
        chip.isCheckable = true
        chip.isChecked = checked
        //点击删除事件
        chip.setOnCloseIconClickListener {
            banContents.remove(banContent)
            if (chip.isChecked) {
                isSelectedArray.remove(banContent)
            }
            binding?.banChipGroup?.removeView(chip)
            saveBanInfo(banContents, isSelectedArray)
        }
        //选中事件
        chip.setOnCheckedChangeListener { chip, isChecked ->
            if (isChecked) {
                isSelectedArray.add(chip.text.toString())
            } else {
                isSelectedArray.remove(chip.text.toString())
            }
            saveBanInfo(banContents, isSelectedArray)
        }
        return chip
    }

    //保存弹幕屏蔽信息
    private fun saveBanInfo(banArray: ArrayList<String>, isSelectedArray: ArrayList<String>) {
        val context = context as LiveRoomActivity
        if (!SunnyWeatherApplication.isLogin.value!!) {
            return
        }
        val userInfo = SunnyWeatherApplication.userInfo
        userInfo!!.allContent = banArray.joinToString(";")
        userInfo!!.selectedContent = isSelectedArray.joinToString(";")
        context.banChanged(isSelectedArray)
        viewModel.changeUserInfo(userInfo)
    }

    private fun saveBanActive(isActive: Boolean){
        val userInfo = SunnyWeatherApplication.userInfo
        userInfo!!.isActived = when(isActive){
            true -> "1"
            false -> "0"
        }
        viewModel.changeUserInfo(userInfo)
    }
}