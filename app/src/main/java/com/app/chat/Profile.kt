package com.app.chat


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.view.*


class Profile : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       val rootView= inflater.inflate(R.layout.fragment_profile, container, false)
        rootView.exitChat.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            activity!!.finish()
        }
        return rootView
    }


}
