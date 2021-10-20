package com.booggii.sensor.ui
import androidx.fragment.app.FragmentPagerAdapter
import android.content.Context;
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class TabsAdapter(private val myContext: Context, fm: FragmentManager, internal var totalTabs: Int) : FragmentPagerAdapter(fm) {

    // this is for fragment tabs
    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                //  val homeFragment: HomeFragment = HomeFragment()
                return StateFragment()
            }
            1 -> {
                //  val homeFragment: HomeFragment = HomeFragment()
                return SettingsFragment()
            }
            else -> {
                return PlotterFragment()
            }
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}