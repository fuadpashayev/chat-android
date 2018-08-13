package com.app.chat

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class FragmentPager(val mContext: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {


    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            Chat()
        } else if (position == 1) {
            Contacts()
        } else if (position == 2) {
            Profile()
        }else {
            Chat()
        }
    }

    // This determines the number of tabs
    override fun getCount(): Int {
        return 3
    }

    // This determines the title for each tab
    override fun getPageTitle(position: Int): CharSequence? {
        // Generate title based on item position
        when (position) {
            0 -> return "Chat"
            1 -> return "Contacts"
            2 -> return "Profile"
            else -> return null
        }
    }

}