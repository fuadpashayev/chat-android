package com.app.chat

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_home.*
import android.support.design.widget.TabLayout
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.popup.view.*


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


        settingActions.setOnClickListener {
            val inflaterPop = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflaterPop.inflate(R.layout.popup,null)
            val popup = PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            popup.animationStyle = R.style.PopupAnimation
            popup.showAsDropDown(settingActions,0,-100,Gravity.NO_GRAVITY)
            view.closeProfileActions.setOnClickListener {
                popup.dismiss()
            }

            view.exitChat.setOnClickListener {
                val timestamp = System.currentTimeMillis()/1000
                FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/exit").setValue(timestamp)
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
        }


        updateExit("enter")


    }

    override fun onResume() {
        super.onResume()
        updateExit("enter")
    }

    fun updateExit(type:String){
        val query = FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/exit")
        var tm:Int?
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
