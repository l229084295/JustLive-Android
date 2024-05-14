package com.sunnyweather.android.ui.login

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.blankj.utilcode.util.BarUtils
import com.google.android.material.textfield.TextInputLayout
import com.roger.gifloadinglibrary.GifLoadingView
import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.SunnyWeatherApplication.Companion.context
import com.sunnyweather.android.logic.model.UserInfo
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityRegisterBinding

class RegisterActivity: AppCompatActivity() {
    private val viewModel by lazy { ViewModelProvider(this).get(LoginViewModel::class.java) }
    private val userNameRex = "^[^\\u4e00-\\u9fa5]+\$"
    private val mGifLoadingView = GifLoadingView()
    lateinit var binding: ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        //颜色主题
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        var theme: Int
        val autoDark = sharedPreferences.getBoolean("autoDark", true)
        val pureDark = sharedPreferences.getBoolean("pureDark", false)
        if (autoDark) {
            if(SunnyWeatherApplication.isNightMode(this)){
                theme = R.style.nightTheme
                sharedPreferences.edit().putInt("theme", theme).commit()
            } else {
                theme = R.style.SunnyWeather
                sharedPreferences.edit().putInt("theme", theme).commit()
            }
        } else {
            theme = sharedPreferences.getInt("theme", R.style.SunnyWeather)
        }
        if (pureDark && theme == R.style.nightTheme) {
            setTheme(R.style.nightTheme_dark)
        } else {
            setTheme(theme)
        }
        setContentView(binding.root)
        if (theme != R.style.nightTheme) {
            BarUtils.setStatusBarLightMode(this, true)
        } else {
            BarUtils.setStatusBarLightMode(this, false)
        }
        BarUtils.transparentStatusBar(this)
        viewModel.registerResponseLiveDate.observe(this) { result ->
            val resultData = result.getOrNull()
            if (resultData is UserInfo) {
                "注册成功".showToast(context)
                onBackPressed()
            } else if (resultData is String) {
                mGifLoadingView.dismiss()
                resultData.showToast(context)
            }
        }
        //用户名校验
        binding.userNameContentRegister.doOnTextChanged { text, _, _, _ ->
            val count = text!!.length
            if(count > 0 && !match(userNameRex, text.toString())) {
                binding.usernameTextRegister.error = getString(R.string.userNameError)
            } else if(count in 1..5){
                binding.usernameTextRegister.error = getString(R.string.userNameError6)
            } else if(count > 20) {
                binding.usernameTextRegister.error = getString(R.string.userNameError20)
            } else {
                binding.usernameTextRegister.error = null
            }
        }
        //昵称校验
        binding.nickNameContentRegister.doOnTextChanged { text, _, _, _ ->
            val count = text!!.length
            if(count > 20) {
                binding.nicknameTextRegister.error = getString(R.string.userNameError20)
            } else {
                binding.nicknameTextRegister.error = null
            }
        }
        //密码框校验
        binding.passwordContentRegister.doOnTextChanged { text, _, _, _ ->
            if (text!!.isNotEmpty()) {
                binding.passwordTextRegister.error = null
            }
            if (binding.passwordContentRegister.text.toString() == text.toString()){
                binding.repasswordTextRegister.error = null
            }
        }
        //重复密码校验
        binding.repasswordContentRegister.doOnTextChanged { text, _, _, _ ->
            if (binding.passwordContentRegister.text.toString() != text.toString()) {
                binding.repasswordTextRegister.error = getString(R.string.registerRePasswordError)
            } else {
                binding.repasswordTextRegister.error = null
            }
        }
        //提交注册
        binding.registerBtn.setOnClickListener {
            hideInput(this)
            if (checkAll()){
                val userName = binding.userNameContentRegister.text.toString()
                val password = SunnyWeatherApplication.encodeMD5(binding.passwordContentRegister.text.toString())
                val nickname = binding.nickNameContentRegister.text.toString()
                viewModel.doRegister(userName, password, nickname)
                mGifLoadingView.setImageResource(R.drawable.load_knife)
                mGifLoadingView.show(fragmentManager)
            }
        }
    }

    //提交注册前检验各个输入框是否正确
    private fun checkAll(): Boolean{
        if (!checkIfCorrect(binding.usernameTextRegister, binding.userNameContentRegister)) return false
        if (!checkIfCorrect(binding.nicknameTextRegister, binding.nickNameContentRegister)) return false
        if (!checkIfCorrect(binding.passwordTextRegister, binding.passwordContentRegister)) return false
        if (!checkIfCorrect(binding.repasswordTextRegister, binding.repasswordContentRegister)) return false
        return true
    }
    //判断是否正确
    private fun checkIfCorrect(textInputLayout: TextInputLayout, textView: TextView): Boolean {
        var textInputLayoutCheck = true
        var textViewCheck = true
        if (textView.text.isEmpty()){
            textInputLayout.error = "不能为空"
            textViewCheck = false
        }
        if (textInputLayout.error != null){
            val note = "输入信息存在错误"
            note.showToast(context)
            textInputLayoutCheck = false
        }
        return textInputLayoutCheck && textViewCheck
    }
    //关闭软键盘
    private fun hideInput(activity: Activity) {
        if (activity.currentFocus != null) {
            val inputManager = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        }
    }
    /**
     * @param rex 正则校验规则
     * @param str 要校验的字符串
     * @return 返回校验结果，若满足校验规则，则返回true，否则返回false
     */
    private fun match(rex: String, str: String): Boolean {
        return Regex(rex).matches(str)
    }
    //toast
    private fun String.showToast(context: Context) {
        Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
    }
}