package com.app.chat


import android.content.Context
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.message_layout.*
import kotlinx.coroutines.experimental.launch
import java.util.*
import java.text.SimpleDateFormat
import kotlin.collections.HashMap
import android.app.Activity
import android.support.v4.content.ContextCompat.getSystemService
import android.text.Html
import android.view.WindowManager
import android.widget.Toast


class Messages : AppCompatActivity() {
    var withId:String?=null
    var withName:String?=null
    var withPhoto:String?=null
    var chatId:String?=null
    var status:Boolean=true
    var withGender:String?=null
    var auth:FirebaseAuth?=null
    var user:FirebaseUser?=null
    var messageAdapterList = ArrayList<MessageModel>()
    var initialLoad = false
    var changeActivity = false
    var childAdded=false
    var alerting = 0
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
        withGender = data.getString("gender")

        FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/chats/$chatId").addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(snap: DataSnapshot?) {
                val data = snap?.getValue(ChatBoxModel::class.java)
                val status = data?.status
                if(data?.from==user!!.uid && status==false){
                    hideKeyboard(this@Messages)
                    sendBoxArea.visibility = View.GONE
                    requestMyText.text = Html.fromHtml("<font color='#000000'><b>$withName</b></font> has not accepted your request yet.")
                    requestMyBody.visibility = View.VISIBLE
                }else if(data?.from!=user!!.uid && status==false){
                    sendBoxArea.visibility = View.GONE
                    requestText.text = Html.fromHtml("<font color='#000000'><b>$withName</b></font> wants to send you message.")
                    requestBody.visibility = View.VISIBLE
                }else if(status==true){
                    requestBody.visibility = View.GONE
                    messageBox.requestFocus()
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    if(data.from==user!!.uid && alerting==1){
                        requestMyBody.visibility = View.GONE
                        sendBoxArea.visibility = View.VISIBLE
                        Toast.makeText(this@Messages,Html.fromHtml("<font color='#ffffff'><b>$withName</b></font> accepted your request."),Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })


        requestAccept.setOnClickListener {
            alerting++
            val statusData = HashMap<String,Any>()
            statusData["status"]=true
            FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/chats/$chatId").updateChildren(statusData)
            FirebaseDatabase.getInstance().getReference("users/$withId/chats/$chatId").updateChildren(statusData)
            requestBody.visibility = View.GONE
            sendBoxArea.visibility = View.VISIBLE
            messageBox.requestFocus()
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            Toast.makeText(this,Html.fromHtml("You accepted <font color='#ffffff'><b>$withName's</b></font> request"),Toast.LENGTH_SHORT).show()
        }

        requestDecline.setOnClickListener {
            finish()
            FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/chats/$chatId").removeValue()
            FirebaseDatabase.getInstance().getReference("users/$withId/chats/$chatId").removeValue()
            FirebaseDatabase.getInstance().getReference("messages/$chatId").removeValue()
            Toast.makeText(this,Html.fromHtml("You declined <font color='#000000'><b>$withName's</b></font> request"),Toast.LENGTH_SHORT).show()
        }

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



        query.limitToLast(1).addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(data: DataSnapshot?) {
                if(!data!!.exists()){
                    loader.visibility = View.GONE
                    initialLoad = false
                }else {
                    loader.visibility = View.VISIBLE
                    initialLoad = true
                }


            }

        })
        query.limitToLast(20).addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(snap: DataSnapshot?, p1: String?) {
                initialLoad = true
                loader.visibility = View.GONE
                val data = snap!!.getValue(MessageModel::class.java)

                if(!childAdded){
                    messageAdapterList.add(data!!)
                }
                else if(childAdded && data!!.byId!=user!!.uid){
                    messageAdapterList.add(data)
                }
                 messages.adapter!!.notifyDataSetChanged()
                 messages.scrollToPosition(messages.adapter!!.itemCount-1)
            }
            override fun onChildChanged(snap: DataSnapshot?, p1: String?) {}
            override fun onChildRemoved(snap: DataSnapshot?) {
                initialLoad = messageAdapterList.size>0
                loader.visibility = View.GONE
                val data = snap!!.getValue(MessageModel::class.java)



                for(mesData in 0 until messageAdapterList.size-1){
                    var mData = messageAdapterList[mesData]
                    if(data!!.id==mData.id) {
                        val index = messageAdapterList.indexOf(mData)
                        messageAdapterList.removeAt(index)
                        messages.adapter!!.notifyItemRemoved(index)
                    }
                }


                //Log.d("-----Tek1",mesData.message)
                //messageAdapterList.removeAt(1)


            }
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

        })
        messages.setHasFixedSize(true)
        messages.adapter = messageAdapter(messageAdapterList, withPhoto, this@Messages, messages,chatId)
        messages.scrollToPosition(messages.adapter!!.itemCount-1)

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
                val messageOnlineStatusText:String?
                    if(time==0L){
                        messageOnlineStatusText = "on"
                    }else if(tm in 0..60){
                        messageOnlineStatusText = "few moment ago"
                    }else if(tm in 60..3600){
                        val mnt = Math.floor(tm/60.0).toInt()
                        messageOnlineStatusText = "$mnt minutes ago"
                    }else if(tm>3600 && tm<24*3600){
                        val hr = Math.floor(tm/3600.0).toInt()
                        messageOnlineStatusText = "$hr hours ago"
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
            constructor()
            constructor(byId:String?,message:String?,seen:Int?,timestamp:Long?,toId:String?,id:String?){
                this.byId = byId
                this.message = message
                this.seen = seen
                this.timestamp = timestamp
                this.toId = toId
                this.id = id
            }
        }


        var BottomReached = false
        var loading = false
        var pastVisiblesItems:Int
        var visibleItemCount:Int
        var totalItemCount:Int
        val mLayoutManager = LinearLayoutManager(this)
        messages.layoutManager = mLayoutManager
        messages.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView:RecyclerView, dx:Int, dy:Int) {
                val scrollHeight = messages.computeVerticalScrollOffset()
                if(dy<0 && scrollHeight==0 && !loading){
                    loading = true
                    messageLoader.visibility = View.VISIBLE
                    //Log.d("------aaa","HOPPAAA YUKLEME BASLADI")
                    val firstMessage = messageAdapterList[0].timestamp!!.toDouble()
                    FirebaseDatabase.getInstance().getReference("messages/$chatId").orderByChild("timestamp").endAt(firstMessage).limitToLast(20).addListenerForSingleValueEvent(object:ValueEventListener{
                        override fun onCancelled(p0: DatabaseError?) {}
                        override fun onDataChange(snap: DataSnapshot?) {
//                            Log.d("-------a",snap.toString())
                            val newDataList = ArrayList<MessageModel>()
                            for(datas in snap!!.children){
                                val data = datas!!.getValue(MessageModel::class.java)
                                if(data!!.timestamp!=firstMessage.toLong()){
                                    if(messageAdapterList[0].timestamp!=data.timestamp) {
                                        messageLoader.visibility = View.GONE
                                        Log.d("---------a",data.message+" - "+data)
                                        newDataList.add(data)
                                        //messages.adapter!!.notifyDataSetChanged()
                                    }
                                }


                            }
                            newDataList.addAll(messageAdapterList)
                            messages.adapter = messageAdapter(newDataList, withPhoto, this@Messages, messages, chatId)
                            messages.scrollToPosition(newDataList.size-21)
                        }

                    })



                    loading = false
                }
                visibleItemCount = mLayoutManager.childCount
                totalItemCount = mLayoutManager.itemCount
                pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition()
                BottomReached = (visibleItemCount + pastVisiblesItems) >= totalItemCount
            }
        })

        sendBoxArea.addOnLayoutChangeListener { v, _, _, _, _, _, topWas, _, bottomWas ->
            val heightWas = bottomWas - topWas
            if (v.height != heightWas && BottomReached)
                messages.scrollToPosition(messages.adapter!!.itemCount-1)
        }




        sendMessage.setOnClickListener {
            alerting++
            childAdded=true
            val message = messageBox.text.trim().toString()
            if(((initialLoad && messageAdapterList.size>0) || (!initialLoad && messageAdapterList.size<=0)) && message.count()>0) {
                var myUser:UsersModel?=null
                val keyChat:String?=chatId
                val timestamp = System.currentTimeMillis() / 1000
                FirebaseDatabase.getInstance().getReference("users/${user!!.uid}").addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(snap: DataSnapshot?) {
                        myUser = snap!!.getValue(UsersModel::class.java)
                        val uquery = FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/chats/$chatId")
                        val data = HashMap<String, Any>()
                        val messageCount = snap.child("chats/$chatId/messageCount").getValue(Int::class.java)!!
                        data["lastMessage"] = message
                        data["timestamp"] = timestamp
                        data["from"] = user!!.uid
                        data["status"] = if(messageCount==0) myUser!!.Gender==withGender else true
                        data["messageCount"] = messageCount+1
                        uquery.updateChildren(data)


                        val query = FirebaseDatabase.getInstance().getReference("messages/$chatId").push()
                        val key = query.key
                        messageAdapterList.add(MessageModel(user!!.uid, message, 0, timestamp, withId, key))
                        query.setValue(newMessage(user!!.uid, message, 0, timestamp, withId, key))
                        messageBox.text.clear()
                        messages.scrollToPosition(messages.adapter!!.itemCount-1)
                    }

                })

                FirebaseDatabase.getInstance().getReference("users/$withId").addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(snap: DataSnapshot?) {
                        if(!snap!!.child("chats/$chatId").exists()){
                            val query = FirebaseDatabase.getInstance().getReference("users/$withId/chats/$chatId")
                            val status = withGender==myUser!!.Gender
                            query.setValue(ChatBoxModel(message,timestamp,myUser!!.Id,myUser!!.Name,myUser!!.Photo,keyChat,user!!.uid,status,1))
                        }else {
                            val query = FirebaseDatabase.getInstance().getReference("users/$withId/chats/$chatId")
                            val data = HashMap<String, Any>()
                            val messageCount = snap.child("chats/$chatId/messageCount").getValue(Int::class.java)!!
                            data["lastMessage"] = message
                            data["timestamp"] = timestamp
                            data["from"] = user!!.uid
                            data["status"] = if(messageCount==0) myUser!!.Gender==withGender else true
                            data["messageCount"] = snap.child("chats/$chatId/messageCount").getValue(Int::class.java)!!+1
                            query.updateChildren(data)
                        }
                    }

                })




            }
        }



    }

    fun hideKeyboard(activity: Activity) {
        val imm =  activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun dptopx(dp:Int):Int{
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),resources.displayMetrics))
    }

    fun getDate(timestamp: Long): String {
        val date = Date(timestamp * 1000)
        val jdf = SimpleDateFormat("dd.MM.yyyy")
        jdf.timeZone = TimeZone.getTimeZone("GMT+4")
        return jdf.format(date)
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


    private fun updateExit(type:String){
        val query = FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/exit")
        val tm:Int?
        tm = if(type=="exit"){
            val time = (System.currentTimeMillis()/1000).toInt()
            time
        }else 0

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
