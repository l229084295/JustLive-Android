package com.sunnyweather.android.util.dkplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.sunnyweather.android.databinding.FragmentDanmuMainBinding

class DanmuMainFragment: Fragment() {

    var binding: FragmentDanmuMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDanmuMainBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding?.danmuViewPager?.adapter = PagerAdapter(this)
        TabLayoutMediator(binding!!.danmuTabLayout, binding!!.danmuViewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "播放设置"
                }
                1 -> {
                    tab.text = "屏蔽设置"
                }
            }
        }.attach()
    }

    class PagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DanmuSettingFragment()
                else -> DanmuBanFragment()
            }
        }
    }
}