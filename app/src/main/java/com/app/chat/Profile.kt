package com.app.chat



import android.app.ActionBar
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_profile.view.*


class Profile : Fragment() {
    var auth:FirebaseAuth?=null
    var user:FirebaseUser?=null
    var myUser:UsersModel?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       val rootView= inflater.inflate(R.layout.fragment_profile, container, false)
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        rootView.loader.visibility = View.VISIBLE
        FirebaseDatabase.getInstance().getReference("users/${user!!.uid}").addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(snap: DataSnapshot?) {
                rootView.loader.visibility = View.GONE
                myUser = snap!!.getValue(UsersModel::class.java)

                val imgHolder = rootView.profileImage
                imgHolder.clipToOutline = true

                Glide.with(context!!)
                        .load(myUser!!.Photo)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .thumbnail(Glide.with(context).load(R.mipmap.loader))
                        .fitCenter()
                        .centerCrop()
                        .crossFade(1000)
                        .into(imgHolder)

                rootView.profileActions.setOnClickListener {
                    val inflaterPop = activity!!.applicationContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val view = inflaterPop.inflate(R.layout.popup,null)
                    val popup = PopupWindow(view,500,LinearLayout.LayoutParams.WRAP_CONTENT)
                    popup.showAsDropDown(rootView.profileActions,-70,-10)
                }

            }

        })

//        rootView.exitChat.setOnClickListener {
//            val timestamp = System.currentTimeMillis()/1000
//            FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/exit").setValue(timestamp)
//            FirebaseAuth.getInstance().signOut()
//            val intent = Intent(activity,MainActivity::class.java)
//            startActivity(intent)
//        }

        return rootView
    }


}
