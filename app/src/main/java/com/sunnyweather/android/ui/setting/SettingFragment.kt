package com.sunnyweather.android.ui.setting

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.blankj.utilcode.util.AppUtils
import com.efs.sdk.base.newsharedpreferences.SharedPreferencesUtils.getSharedPreferences
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.UpdateInfo
import com.sunnyweather.android.ui.about.AboutActvity
import com.sunnyweather.android.ui.login.LoginViewModel
import com.sunnyweather.android.R

class SettingFragment : PreferenceFragmentCompat() {
    private val viewModel by lazy { ViewModelProvider(this).get(LoginViewModel::class.java) }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)
        val signaturePreference: SwitchPreferenceCompat? = findPreference("dayNight")
        val autoDark: SwitchPreferenceCompat? = findPreference("autoDark")
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val versionPreferences = findPreference<Preference>("version")
        val aboutPreferences = findPreference<Preference>("about_activity")
        val donatePreferences = findPreference<Preference>("donate")
        val pureDarkPreference: SwitchPreferenceCompat? = findPreference("pureDark")
        signaturePreference?.isChecked =
            sharedPreferences.getInt("theme", R.style.SunnyWeather) != R.style.SunnyWeather
        signaturePreference?.setOnPreferenceChangeListener { _, newValue  ->
            if (newValue as Boolean) {
                sharedPreferences.edit().putInt("theme", R.style.nightTheme).commit()
                signaturePreference.isChecked = true
                activity?.recreate()
            } else {
                sharedPreferences.edit().putInt("theme", R.style.SunnyWeather).commit()
                signaturePreference.isChecked = false
                activity?.recreate()
            }
            true
        }
        pureDarkPreference?.setOnPreferenceChangeListener { _, _ ->
            activity?.recreate()
            true
        }
        autoDark?.setOnPreferenceChangeListener { _, newValue ->
            if (newValue as Boolean) {
                if(SunnyWeatherApplication.isNightMode(requireActivity())){
                    sharedPreferences.edit().putInt("theme", R.style.nightTheme).commit()
                    signaturePreference?.isChecked = true
                    activity?.recreate()
                } else {
                    sharedPreferences.edit().putInt("theme", R.style.SunnyWeather).commit()
                    signaturePreference?.isChecked = false
                    activity?.recreate()
                }
            }
            true
        }
        signaturePreference?.setOnPreferenceClickListener {
            sharedPreferences.edit().putBoolean("autoDark", false).commit()
        }

        versionPreferences?.summary = "当前版本:" + AppUtils.getAppVersionName()
        versionPreferences?.setOnPreferenceClickListener {
            // do something
            viewModel.checkVersion()
            true
        }
        aboutPreferences?.setOnPreferenceClickListener {
            // 打开关于页面
            val intent = Intent(requireContext(), AboutActvity::class.java)
            requireContext().startActivity(intent)
            true
        }


        viewModel.updateResponseLiveData.observe(this) { result ->
            val updateInfo = result.getOrNull()
            if (updateInfo is UpdateInfo) {
                var sharedPref = getSharedPreferences(requireContext(), "JustLive")
                val ignoreVersion = sharedPref.getInt("ignoreVersion", 0)
                val versionNum = SunnyWeatherApplication.getVersionCode(SunnyWeatherApplication.context)
                if (versionNum >= updateInfo.versionNum) {
                    Toast.makeText(SunnyWeatherApplication.context, "当前已是最新版本^_^", Toast.LENGTH_SHORT).show()
                    return@observe
                }
                var descriptions = ""
                var index = 1
                for (item in updateInfo.description) {
                    descriptions = "$descriptions$index.$item<br>"
                    index++
                }

            } else if (updateInfo is String) {
                Toast.makeText(requireContext(), "用户密码已修改，请重新登录", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }
}