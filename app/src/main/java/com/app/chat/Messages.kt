package com.app.chat


import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_message.*

class Messages : AppCompatActivity() {
    var withId:String?=null
    var withName:String?=null
    var withPhoto:String?=null
    var chatId:String?=null
    var messageAdapterList = ArrayList<MessageModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        supportActionBar!!.hide()
        val data = this.intent.extras
        withId = data.getString("withId")
        withName = data.getString("withName")
        withPhoto = data.getString("withPhoto")
        chatId = data.getString("chatId")
        messagePhoto.clipToOutline = true
        messageName.text = withName
        Glide.with(this)
                .load(withPhoto)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .thumbnail(Glide.with(this).load(R.mipmap.loader))
                .fitCenter()
                .centerCrop()
                .crossFade(1000)
                .into(messagePhoto)

        val query = FirebaseDatabase.getInstance().getReference("messages/$chatId")
        query.addChildEventListener(object:ChildEventListener{


            override fun onChildAdded(snap: DataSnapshot?, p1: String?) {


                val a = snap!!.getValue(MessageModel::class.java)
                messageAdapterList.add(a!!)


                messages.adapter = messageAdapter(messageAdapterList,withPhoto,this@Messages)


            }

            override fun onChildChanged(snap: DataSnapshot?, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot?) {}
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

        })

    }
}
