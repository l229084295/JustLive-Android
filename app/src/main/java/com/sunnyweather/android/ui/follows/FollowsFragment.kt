package com.sunnyweather.android.ui.follows

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.FragmentFollowsBinding

private const val NUM_PAGES = 2
private var showLogin = false

class FollowsFragment : Fragment() {
    private lateinit var viewPager: ViewPager2
    var binding: FragmentFollowsBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFollowsBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //ViewPager2
        viewPager = binding!!.viewpageFollows
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = 2
        binding!!.viewpageFollows.isUserInputEnabled = false
        //tabLayout
        TabLayoutMediator(binding!!.tabFollows, binding!!.viewpageFollows){tab, position ->
            when(position) {
                0 -> tab.text = "直播中"
                else -> tab.text = "未开播"
            }
        }.attach()
    }

    private inner class ScreenSlidePagerAdapter(fa: Fragment) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = NUM_PAGES
        override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> OnLiveFragment(true)
                1 -> OnLiveFragment(false)
                else -> Fragment()
            }
    }

    fun enableInput() {
        binding?.viewpageFollows?.isUserInputEnabled = true
    }
}