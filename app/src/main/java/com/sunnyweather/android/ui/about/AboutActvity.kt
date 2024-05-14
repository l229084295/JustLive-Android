package com.sunnyweather.android.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ToastUtils
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityAboutBinding
import java.lang.Exception

class AboutActvity: AppCompatActivity(){

    lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        var themeActived: Int
        val autoDark = sharedPreferences.getBoolean("autoDark", true)
        val pureDark = sharedPreferences.getBoolean("pureDark", false)
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
        binding.apply {
            BarUtils.transparentStatusBar(this@AboutActvity)
            BarUtils.addMarginTopEqualStatusBarHeight(aboutAbout)
            BarUtils.setStatusBarLightMode(this@AboutActvity, themeActived != R.style.nightTheme)

            aboutVersion.text = "版本号:" + AppUtils.getAppVersionName()

            aboutPic.setImageDrawable(AppUtils.getAppIcon())

            aboutKuan.setOnClickListener {
                try {
                    val intent = Intent()
                    intent.setClassName("com.coolapk.market", "com.coolapk.market.view.AppLinkActivity");
                    intent.action = "android.intent.action.VIEW";
                    intent.data = Uri.parse("coolmarket://u/645623")
                    startActivity(intent)
                } catch (e:Exception) {
                    ToastUtils.showShort("启动失败")
                }
            }
            aboutWeibo.setOnClickListener {
                var intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://weibo.com/u/5211151565"))
                startActivity(intent)
            }
            aboutQq.setOnClickListener {
                var intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://qm.qq.com/cgi-bin/qm/qr?k=_2KootdkU0ikLiFhBCQMJKW7PjHzySZ8&authKey=2dGp04G04G/+a1KHiIjqjpg1Se+/TgpQ5yzpEbMkzP9Y6lkrFdReKdtBtg6xC+Cs&noverify=0"))
                this@AboutActvity.startActivity(intent)
            }
            aboutGithub.setOnClickListener {
                var intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/guyijie1211"))
                this@AboutActvity.startActivity(intent)
            }
            aboutWeb.setOnClickListener {
                var intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://live.yj1211.work"))
                this@AboutActvity.startActivity(intent)
            }
            aboutBack.setOnClickListener {
                this@AboutActvity.onBackPressed()
            }
        }
    }
}