package com.app.chat

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabSign.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0->{
                        signIn.visibility = View.VISIBLE
                        signUp.visibility = View.GONE
                    }
                    1->{
                        signUp.visibility = View.VISIBLE
                        signIn.visibility = View.GONE
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?){}
            override fun onTabUnselected(tab: TabLayout.Tab?){}
        })

    }
}
