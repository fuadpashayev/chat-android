package com.app.chat


import android.content.Context
import android.graphics.PointF
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.tasks.OnSuccessListener
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
    var initialLoad = false
    var changeActivity = false
    var childAdded=false
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
        messageBox.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        query.limitToLast(1).addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(data: DataSnapshot?) {
                if(!data!!.exists()){
                    loader.visibility = View.GONE
                }else {
                    loader.visibility = View.VISIBLE
                    initialLoad = true
                }
            }

        })
        query.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(snap: DataSnapshot?, p1: String?) {
                initialLoad = true
                loader.visibility = View.GONE
                val data = snap!!.getValue(MessageModel::class.java)
                Log.d("-------a",childAdded.toString()+" - "+data!!.message+" - "+(data!!.byId!=user!!.uid))
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
            override fun onChildRemoved(p0: DataSnapshot?) {}
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

        })
        messages.setHasFixedSize(true)
        messages.adapter = messageAdapter(messageAdapterList, withPhoto, this@Messages, messages)
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
                   // Log.d("------aaa","HOPPAAA YUKLEME BASLADI")
                    loading = false
                }
                visibleItemCount = mLayoutManager.childCount
                totalItemCount = mLayoutManager.itemCount
                pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition()
                BottomReached = (visibleItemCount + pastVisiblesItems) >= totalItemCount
            }
        })

        sendBoxArea.addOnLayoutChangeListener(object:View.OnLayoutChangeListener{
           override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, leftWas: Int, topWas: Int, rightWas: Int, bottomWas: Int) {
                val heightWas = bottomWas - topWas
                if (v.height != heightWas) {
                    val params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,ConstraintLayout.LayoutParams.MATCH_PARENT)
                    val mesHeight = sendBoxArea.height
                    params.setMargins(0,dptopx(60),0,mesHeight)
                    messages.layoutParams = params
                    Log.d("-------a",BottomReached.toString())
                    if(BottomReached)
                        messages.scrollToPosition(messages.adapter!!.itemCount-1)
                }
            }

        })

        sendMessage.setOnClickListener {
            childAdded=true
            val message = messageBox.text.trim().toString()
            if(((initialLoad && messageAdapterList.size>0) || (!initialLoad && messageAdapterList.size<=0)) && message.count()>0) {
                val query = FirebaseDatabase.getInstance().getReference("messages/${chatId}").push()
                val key = query.key
                messageAdapterList.add(MessageModel(user!!.uid, message, 0, System.currentTimeMillis() / 1000, withId, key))
                query.setValue(newMessage(user!!.uid, message, 0, System.currentTimeMillis() / 1000, withId, key))
                messageBox.text.clear()
                messages.scrollToPosition(messages.adapter!!.itemCount-1)
            }
        }
    }

    fun dptopx(dp:Int):Int{
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),resources.displayMetrics))
    }

    fun getDate(timestamp: Long): String {
        val date = Date(timestamp * 1000L)
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
