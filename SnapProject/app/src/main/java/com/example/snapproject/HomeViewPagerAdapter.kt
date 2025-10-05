package com.example.snapproject

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.snapproject.Fragment.FirstMenuFragment
import com.example.snapproject.Fragment.SecondMenuFragment
import com.example.snapproject.model.HomeMenuTab

class HomeViewPagerAdapter(private val activity: FragmentActivity) :
    FragmentStateAdapter(activity) {
    private val fragments = ArrayList<HomeMenuTab>()

    init {
        fragments.add(
            HomeMenuTab(FirstMenuFragment.newInstance(), 0)
        )
        fragments.add(
            HomeMenuTab(SecondMenuFragment.newInstance(), 1)
        )
    }

    fun getNum(position: Int): Int {
        return fragments[position].num
    }

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position].fragment
    }
}
