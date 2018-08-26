package com.app.chat

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import android.support.design.widget.TabLayout
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase



class Home : AppCompatActivity() {
    var auth:FirebaseAuth?=null
    var user:FirebaseUser?=null
    var changeActivity=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar!!.hide()


        val adapter = FragmentPager(this, supportFragmentManager)
        viewPager.adapter = adapter
        val tabLayout = sliding_tabs as TabLayout
        tabLayout.setupWithViewPager(viewPager)
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser


        updateExit("enter")


    }

    override fun onResume() {
        super.onResume()
        updateExit("enter")
    }

    fun updateExit(type:String){
        val query = FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/exit")
        var tm:Int?=null
        tm = if(type=="exit"){
            val time = (System.currentTimeMillis()/1000).toInt()
            time
        }else{
            0
        }
        query.setValue(tm)
    }

    override fun onPause() {
        super.onPause()
        if(!changeActivity)
            updateExit("exit")
    }



    override fun onBackPressed() {}
}
