package com.app.chat


import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.message_my_layout.view.*
import java.util.*
import java.text.SimpleDateFormat


class Messages : AppCompatActivity() {
    var withId:String?=null
    var withName:String?=null
    var withPhoto:String?=null
    var chatId:String?=null
    var auth:FirebaseAuth?=null
    var user:FirebaseUser?=null
    var messageAdapterList = ArrayList<MessageModel>()
    var initalLoad = false
    var changeActivity = false
    var childAdded=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        auth = FirebaseAuth.getInstance()
        user=auth!!.currentUser
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
            if(messageAdapterList.size>0) {
                if (messageAdapterList.get(childAdded).timestamp != a!!.timestamp) {
                    messageAdapterList.add(a!!)
                }
                childAdded++
            }else{
                messageAdapterList.add(a!!)
            }


            }
            override fun onChildChanged(snap: DataSnapshot?, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot?) {}
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

        })
        messages.setHasFixedSize(true)
        messages.adapter = messageAdapter(messageAdapterList, withPhoto, this@Messages, messages)

        updateExit("enter")
        backToHome.setOnClickListener {
            backToHome()
        }



        val statusQuery = FirebaseDatabase.getInstance().getReference("users/$withId/exit")

        statusQuery.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(data: DataSnapshot?) {
                val time = data!!.getValue(Long::class.java)!!
                val current = System.currentTimeMillis()/1000
                val tm = (current - time).toInt()
                var messageOnlineStatusText:String?=""
                    if(time==0L){
                        messageOnlineStatusText = "on"
                    }else if(tm>0 && tm<60){
                        messageOnlineStatusText = "few moment ago"
                    }else if(tm>60 && tm<3600){
                        val mnt = Math.floor(tm/60.0).toInt()
                        messageOnlineStatusText = "${mnt} minutes ago"
                    }else if(tm>3600 && tm<24*3600){
                        val hr = Math.floor(tm/3600.0).toInt()
                        messageOnlineStatusText = "${hr} hours ago"
                    }else if(tm>24*3600){
                        val tmm = getDate(time)
                        messageOnlineStatusText = tmm
                    }else{
                        messageOnlineStatusText = "on"
                    }
                if(messageOnlineStatusText!="on"){
                    messageOnlineStatus.visibility = View.VISIBLE
                    messageOnlineStatus.text = messageOnlineStatusText
                }else{
                    messageOnlineStatus.visibility = View.VISIBLE
                    messageOnlineStatus.text = "Online"
                }

            }

            override fun onCancelled(p0: DatabaseError?) {}

        })

        class newMessage {
            var byId:String?=null
            var message:String?=null
            var seen:Int?=null
            var timestamp:Long?=null
            var toId:String?=null
            var id:String?=null
            constructor(){}
            constructor(byId:String?,message:String?,seen:Int?,timestamp:Long?,toId:String?,id:String?){
                this.byId = byId
                this.message = message
                this.seen = seen
                this.timestamp = timestamp
                this.toId = toId
                this.id = id
            }
        }

        sendMessage.setOnClickListener {
            val message = messageBox.text.trim().toString()

            messageAdapterList.add(MessageModel(user!!.uid,message,0,(System.currentTimeMillis()/1000).toInt(),withId,"null"))
            if(initalLoad)
                messages.adapter!!.notifyDataSetChanged()
            messages.scrollToPosition(messages.adapter!!.itemCount-1)

            val query = FirebaseDatabase.getInstance().getReference("messages/${chatId}").push()
            val key = query.key
            query.setValue(newMessage(user!!.uid,message, 0,System.currentTimeMillis()/1000,withId,key))
            messageBox.text.clear()

        }
    }

    fun getDate(timestamp: Long): String {
        val date = Date(timestamp * 1000L)
        // format of the date
        val jdf = SimpleDateFormat("dd.MM.yyyy")
        jdf.timeZone = TimeZone.getTimeZone("GMT-4")
        val java_date = jdf.format(date)
        return java_date
    }

    override fun onResume() {
        super.onResume()
        updateExit("enter")
    }

    override fun onPause() {
        super.onPause()
        if(!changeActivity)
            updateExit("exit")
    }


    fun updateExit(type:String){
        val query = FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/exit")
        var tm:Int?=null
        if(type=="exit"){
            val time = (System.currentTimeMillis()/1000).toInt()
            tm = time
        }else{
            tm = 0
        }
        query.setValue(tm)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backToHome()
    }

    fun backToHome(){
        changeActivity=true
        finish()
    }
}
