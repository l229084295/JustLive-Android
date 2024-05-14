package com.sunnyweather.android

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.angcyo.tablayout.delegate2.ViewPager2Delegate
import com.blankj.utilcode.util.*
import com.lxj.xpopup.XPopup
import com.sunnyweather.android.logic.model.UpdateInfo
import com.sunnyweather.android.logic.model.UserInfo
import com.sunnyweather.android.ui.about.AboutActvity
import com.sunnyweather.android.ui.area.AreaPopup
import com.sunnyweather.android.ui.area.AreaSingleFragment
import com.sunnyweather.android.ui.follows.FollowsFragment
import com.sunnyweather.android.ui.home.HomeFragment
import com.sunnyweather.android.ui.login.LoginActivity
import com.sunnyweather.android.ui.login.LoginViewModel
import com.sunnyweather.android.ui.search.SearchActivity
import com.sunnyweather.android.ui.setting.SettingActivity
import com.umeng.analytics.MobclickAgent
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityMainBinding
import com.sunnyweather.android.databinding.DialogDonateBinding
import com.sunnyweather.android.databinding.DialogUpdateBinding
import moe.feng.alipay.zerosdk.AlipayZeroSdk


class MainActivity : AppCompatActivity(), AreaSingleFragment.FragmentListener {
    private val viewModel by lazy { ViewModelProvider(this).get(LoginViewModel::class.java) }
    private lateinit var areaPopup : AreaPopup
    private lateinit var viewPager: ViewPager2
    private var isVersionCheck = false
    private lateinit var mMenu: Menu
    private var themeActived = R.style.SunnyWeather
    private var autoDark = true
    private var pureDark = false
    private var mShortcutManager:ShortcutManager? = null
    private var activityMain = this
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        //颜色主题
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        autoDark = sharedPreferences.getBoolean("autoDark", true)
        pureDark = sharedPreferences.getBoolean("pureDark", false)
        if (autoDark) {
            if(SunnyWeatherApplication.isNightMode(this)){
                themeActived = R.style.nightTheme
                sharedPreferences.edit().putInt("theme", themeActived).commit()
            } else {
                themeActived = R.style.SunnyWeather
                sharedPreferences.edit().putInt("theme", themeActived).commit()
            }
        } else {
            themeActived = sharedPreferences.getInt("theme", R.style.SunnyWeather)
        }
        if (pureDark && themeActived == R.style.nightTheme) {
            setTheme(R.style.nightTheme_dark)
        } else {
            setTheme(themeActived)
        }
        setContentView(binding.root)

        binding.drawerDarkSwitch.isChecked = (themeActived == R.style.nightTheme)
        BarUtils.addMarginTopEqualStatusBarHeight(binding.drawerNick)
        BarUtils.transparentStatusBar(this)
        BarUtils.setStatusBarLightMode(this, themeActived != R.style.nightTheme)
        setSupportActionBar(binding.mainToolBar)
        initLogin()
        areaPopup = AreaPopup(this, this)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.icon_menu)
            it.setDisplayShowTitleEnabled(false)
        }

        //Drawer
        binding.drawerDarkSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                sharedPreferences.edit().putInt("theme", R.style.nightTheme).commit()
                binding.drawerDarkSwitch.isChecked = true
            } else {
                sharedPreferences.edit().putInt("theme", R.style.SunnyWeather).commit()
                binding.drawerDarkSwitch.isChecked = false
            }
            recreate()
        }
        binding.drawerDarkSwitch.setOnClickListener {
            sharedPreferences.edit().putBoolean("autoDark",false).commit()
        }
        binding.drawerLogout.setOnClickListener {
            SunnyWeatherApplication.clearLoginInfo(this)
            binding.mainFragment.currentItem = 0
        }
        binding.drawerLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.drawerSetting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        binding.drawerSupport.setOnClickListener {
            MaterialDialog(this).show {
                val dialogBinding = DialogDonateBinding.inflate(layoutInflater)
                customView(view = dialogBinding.root)
                var payPlatform = "zfb"
                dialogBinding.donateCancel.setOnClickListener {
                    dismiss()
                }
                dialogBinding.donateSaveCancel.setOnClickListener {
                    dialogBinding.donateContain.visibility = View.VISIBLE
                    dialogBinding.donatePicContain.visibility = View.GONE
                }
                dialogBinding.donateZfb.setOnClickListener {
                    if (AlipayZeroSdk.hasInstalledAlipayClient(context)) {
                        AlipayZeroSdk.startAlipayClient(activityMain, "fkx19479mrxqi6tzhgw0bd0")
                    } else {
                        payPlatform = "zfb"
                        dialogBinding.donatePic.setImageDrawable(resources.getDrawable(R.drawable.zfb_pic))
                        dialogBinding.donateContain.visibility = View.GONE
                        dialogBinding.donatePicContain.visibility = View.VISIBLE
                        dialogBinding.donateSaveZfb.visibility = View.VISIBLE
                        dialogBinding.donateSaveWx.visibility = View.INVISIBLE
                    }
                }
                dialogBinding.donateWx.setOnClickListener {
                    payPlatform = "wx"
                    dialogBinding.donatePic.setImageDrawable(resources.getDrawable(R.drawable.wx_pic))
                    dialogBinding.donateContain.visibility = View.GONE
                    dialogBinding.donatePicContain.visibility = View.VISIBLE
                    dialogBinding.donateSaveZfb.visibility = View.INVISIBLE
                    dialogBinding.donateSaveWx.visibility = View.VISIBLE
                }
                dialogBinding.donateSaveZfb.setOnClickListener {
                    ImageUtils.save2Album(ImageUtils.drawable2Bitmap(resources.getDrawable(R.drawable.zfb_pic)),Bitmap.CompressFormat.JPEG)
                    ToastUtils.showLong("二维码已保存到相册,请打开支付宝扫码使用")
                }
                dialogBinding.donateSaveWx.setOnClickListener {
                    ImageUtils.save2Album(ImageUtils.drawable2Bitmap(resources.getDrawable(R.drawable.wx_pic)),Bitmap.CompressFormat.JPEG)
                    ToastUtils.showLong("二维码已保存到相册,请打开微信扫码使用")
                }
            }
        }
        binding.drawerReport.setOnClickListener {
            val intent = Intent(this, AboutActvity::class.java)
            startActivity(intent)
        }

        //ViewPager2
        viewPager = binding.mainFragment
        viewPager.isUserInputEnabled = false
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewModel.updateResponseLiveData.observe(this) { result ->
            val updateInfo = result.getOrNull()
            if (updateInfo is UpdateInfo) {
                var sharedPref = getSharedPreferences("JustLive", Context.MODE_PRIVATE)
                val ignoreVersion = sharedPref.getInt("ignoreVersion", 0)
                val versionNum =
                    SunnyWeatherApplication.getVersionCode(SunnyWeatherApplication.context)
                if (versionNum >= updateInfo.versionNum || ignoreVersion == updateInfo.versionNum) {
                    if (isVersionCheck) {
                        Toast.makeText(
                            SunnyWeatherApplication.context,
                            "当前已是最新版本^_^",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@observe
                }
                var descriptions = ""
                var index = 1
                for (item in updateInfo.description) {
                    descriptions = "$descriptions$index.$item<br>"
                    index++
                }
                val dialogContent = Html.fromHtml("<div>$descriptions</div>")
                MaterialDialog(this).show {
                    val dialogBinding = DialogUpdateBinding.inflate(layoutInflater)
                    customView(view = dialogBinding.root)
                    dialogBinding.updateDescription.text = dialogContent
                    dialogBinding.updateVersion.text = "版本: ${updateInfo.latestVersion}"
                    dialogBinding.updateSize.text = "下载体积: ${updateInfo.apkSize}"
                    dialogBinding.ignoreBtn.setOnClickListener {
                        var sharedPref =
                            context.getSharedPreferences("JustLive", Context.MODE_PRIVATE)
                        sharedPref.edit().putInt("ignoreVersion", updateInfo.versionNum).commit()
                        Toast.makeText(context, "已忽略", Toast.LENGTH_SHORT).show()
                        cancel()
                    }
                    dialogBinding.versionchecklibVersionDialogCancel.setOnClickListener {
                        dismiss()
                    }
                    dialogBinding.versionchecklibVersionDialogCommit.setOnClickListener {
                        val uri = Uri.parse(updateInfo.updateUrl)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        intent.addCategory(Intent.CATEGORY_BROWSABLE)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                    if (isVersionCheck) {
                        dialogBinding.ignoreBtn.visibility = View.GONE
                    }
                }
            } else if (updateInfo is String) {
                Toast.makeText(this, "用户密码已修改，请重新登录", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        }
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        val title = SunnyWeatherApplication.areaName.value
                        if (title == "all" || title == null) {
                            binding.mainToolBarTitle.text = "全部推荐"
                        } else {
                            binding.mainToolBarTitle.text = title
                        }
                        val drawable = getDrawable(R.drawable.icon_arrow_down)
                        binding.mainToolBarTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
                    }
                    1 -> {
                        binding.mainToolBarTitle.text = "关注"
                        binding.mainToolBarTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    }
                }
            }
        })
        //tabLayout
        ViewPager2Delegate.install(viewPager, binding.tabMain)

        //启动页
        val startPage = sharedPreferences.getString("start_page", "0")
        if (startPage == "1") {
            viewPager.currentItem = 1
        }
        //标题栏的标题click事件
        binding.mainToolBarTitle.setOnClickListener {
            areaPopup = AreaPopup(this, this)
            XPopup.Builder(this)
                .isDestroyOnDismiss(true)
                .autoFocusEditText(false)
                .moveUpToKeyboard(false)
                .popupHeight(ScreenUtils.getAppScreenHeight() * 4 / 5)
                .isViewMode(true)
                .asCustom(areaPopup)
                .show();
        }

        //动态创建shortcuts
        createDynamicShortcut(themeActived)
    }

    override fun onResume() {
        super.onResume()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val newTheme = sharedPreferences.getInt("theme", R.style.SunnyWeather)
        val newPureDark = sharedPreferences.getBoolean("pureDark", false)
        binding.drawerDarkSwitch.isChecked = (themeActived == R.style.nightTheme)
        if (newTheme != themeActived || newPureDark != pureDark){
            recreate()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the options menu from XML
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar, menu)
        mMenu = menu
        SunnyWeatherApplication.isLogin.observe(this, {result ->
            if (result) {
                binding.drawerNick.text = SunnyWeatherApplication.userInfo?.nickName
                binding.drawerUsername.text = SunnyWeatherApplication.userInfo?.userName
                binding.drawerLogout.visibility = View.VISIBLE
                binding.drawerLogin.visibility = View.INVISIBLE
            } else {
                binding.drawerNick.text = "未登录"
                binding.drawerUsername.text = ""
                binding.drawerLogout.visibility = View.INVISIBLE
                binding.drawerLogin.visibility = View.VISIBLE
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> binding.mainDrawerLayout.openDrawer(GravityCompat.START)
            R.id.menu_search -> {
                val intent = Intent(this, SearchActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    override fun onFragment(areaType: String, areaName:String) {
        binding.mainToolBarTitle.text = areaName
        SunnyWeatherApplication.areaType.value = areaType
        SunnyWeatherApplication.areaName.value = if (areaName == "全部推荐") "all" else areaName
        areaPopup.dismiss()
    }

    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> HomeFragment()
                1 -> FollowsFragment()
                else -> Fragment()
            }
    }
    fun toFirst(){
        viewPager.currentItem = 0
    }

    /**
     * 动态创建shortcuts
     * 设置,搜索
     */
    @TargetApi(Build.VERSION_CODES.N_MR1)
    private fun createDynamicShortcut(themeActived:Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            if (mShortcutManager == null) {
                mShortcutManager = getSystemService(ShortcutManager::class.java)
            }

            //设置
            val settingIntent = Intent(this, SettingActivity::class.java)
            settingIntent.action = "android.intent.action.VIEW"
            val settingIcon:Icon = if (themeActived != R.style.nightTheme) {
                Icon.createWithResource(this, R.drawable.shortcut_settings_24)
            }else{
                Icon.createWithResource(this, R.drawable.shortcut_settings_night_24)
            }
            val settingShortcut = ShortcutInfo.Builder(this, "setting")
                .setIcon(settingIcon)
                .setShortLabel(getString(R.string.shortcuts_setting))
                .setLongLabel(getString(R.string.shortcuts_setting))
                .setIntent(settingIntent)
                .build()

            //搜索
            val searchIntent = Intent(this, SearchActivity::class.java)
            searchIntent.action = "android.intent.action.VIEW"
            val searchIcon:Icon = if (themeActived != R.style.nightTheme) {
                Icon.createWithResource(this, R.drawable.shortcut_search)
            }else{
                Icon.createWithResource(this, R.drawable.shortcut_search_night)
            }
            val searchShortcut = ShortcutInfo.Builder(this, "search")
                .setIcon(searchIcon)
                .setShortLabel(getString(R.string.shortcuts_search))
                .setLongLabel(getString(R.string.shortcuts_search))
                .setIntent(searchIntent)
                .build()
            mShortcutManager!!.dynamicShortcuts = arrayOf(settingShortcut,searchShortcut).toMutableList()
        }
    }

    private fun initLogin(){
        var sharedPref = this.getSharedPreferences("JustLive", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "").toString()
        val password = sharedPref.getString("password", "").toString()
        val hsa = sharedPref.contains("")
        viewModel.loginResponseLiveDate.observe(this, { result ->
            val userInfo = result.getOrNull()
            if (userInfo is UserInfo) {
                MobclickAgent.onProfileSignIn(userInfo.userName)//友盟账号登录
                SunnyWeatherApplication.userInfo = userInfo
                SunnyWeatherApplication.isLogin.value = true
            } else if(userInfo is String){
                SunnyWeatherApplication.clearLoginInfo(this)
                Toast.makeText(this, "用户密码已修改，请重新登录", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
        if (password.length > 1) {
            viewModel.doLogin(username, password)
        }
        viewModel.checkVersion()
    }
}