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
        // MenuFirstFragment 추가 (촬영하기 메뉴)
        fragments.add(
            HomeMenuTab(MenuFirstFragment.newInstance(), 0),
        )

        // MenuSecondFragment 추가 (소비기한 메뉴)
        fragments.add(
            HomeMenuTab(MenuSecondFragment.newInstance(), 1),
        )
    }

    // Fragment 개수 반환
    override fun getItemCount(): Int {
        return fragments.size
    }

    // Fragment 생성
    override fun createFragment(position: Int): Fragment {
        return fragments[position].fragment
    }
}
