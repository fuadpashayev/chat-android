package com.app.chat


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_profile.view.*


class Profile : Fragment() {
    var auth:FirebaseAuth?=null
    var user:FirebaseUser?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       val rootView= inflater.inflate(R.layout.fragment_profile, container, false)
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        rootView.exitChat.setOnClickListener {
            val timestamp = System.currentTimeMillis()/1000
            FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/exit").setValue(timestamp)
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity,MainActivity::class.java)
            startActivity(intent)
        }
        return rootView
    }


}
