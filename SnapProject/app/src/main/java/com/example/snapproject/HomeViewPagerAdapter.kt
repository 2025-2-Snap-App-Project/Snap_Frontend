package com.example.snapproject

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.snapproject.Fragment.MenuFirstFragment
import com.example.snapproject.Fragment.MenuSecondFragment
import com.example.snapproject.model.HomeMenuTab

class HomeViewPagerAdapter(private val activity: FragmentActivity) :
    FragmentStateAdapter(activity) {
    private val fragments = ArrayList<HomeMenuTab>()

    init {
        fragments.add(
            HomeMenuTab(MenuFirstFragment.newInstance(), 0),
        )
        fragments.add(
            HomeMenuTab(MenuSecondFragment.newInstance(), 1),
        )
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position].fragment
    }
}
