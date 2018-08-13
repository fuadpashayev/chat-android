package com.app.chat

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import android.support.design.widget.TabLayout



class Home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar!!.hide()


        val adapter = FragmentPager(this, supportFragmentManager)
        viewPager.adapter = adapter
        val tabLayout = sliding_tabs as TabLayout
        tabLayout.setupWithViewPager(viewPager)



    }
    override fun onBackPressed() {}
}
