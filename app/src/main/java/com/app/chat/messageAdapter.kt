package com.app.chat

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.message_layout.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap
import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v7.widget.LinearLayoutManager
import android.view.MotionEvent
import android.widget.LinearLayout


class messageAdapter(var messagesList:ArrayList<MessageModel>,var withPhoto:String?,var messagesActivity:Activity,var messages:RecyclerView,var chatId:String?): RecyclerView.Adapter<MessageViewHolder>(){

    var auth = FirebaseAuth.getInstance()
    var user = auth.currentUser
    var usid:String?=null
    var cell:View?=null
    var data:MessageModel?=null
    override fun getItemCount(): Int {
        return messagesList.size
    }

     override fun getItemViewType(position: Int): Int {
        return position
     }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        data = messagesList[viewType]
        val layoutInflater = LayoutInflater.from(parent.context)
        val inflaterLayout = if(data!!.byId==user!!.uid) R.layout.message_my_layout else R.layout.message_layout
        cell = layoutInflater.inflate(inflaterLayout,parent,false)
        val holder = MessageViewHolder(cell!!)
        val imgHolder = cell!!.messageCPhoto
        imgHolder.clipToOutline = true
        Glide.with(messagesActivity)
                .load(withPhoto)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .thumbnail(Glide.with(messagesActivity).load(R.mipmap.loader))
                .fitCenter()
                .centerCrop()
                .crossFade(1000)
                .into(imgHolder)
        return holder
    }

    var beforeView:View?=null
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val dataa = messagesList[position]
        holder.view.messageText.text = dataa.message
        holder.view.messageTime.text = date("HH:mm",dataa.timestamp!!.toLong())
        if(position == messagesList.size-1 && dataa.timestamp!!>=(System.currentTimeMillis()/1000)-2){
            val animation = AnimationUtils.loadAnimation(messagesActivity, R.anim.abc_fade_in)
            holder.view.messageBubble.startAnimation(animation)
        }

        holder.view.setOnLongClickListener {
            onLongClick(holder,position)
            true
        }

        holder.view.messageFor.setOnLongClickListener {
            onLongClick(holder,position)
            true
        }


//        holder.view.setOnTouchListener(object:View.OnTouchListener{
//            override fun onTouch(eventView: View?, event: MotionEvent?): Boolean {
//
//                val duration = event!!.eventTime-event.downTime
////                Log.d("--------a",event.toString())
//                if(event.action==MotionEvent.ACTION_DOWN)
//                    holder.view.setBackgroundColor(messagesActivity.resources.getColor(R.color.messageSeleted))
//                else if((event.action==MotionEvent.ACTION_CANCEL || event.action==MotionEvent.ACTION_UP) ) {
//                    holder.view.setBackgroundColor(messagesActivity.resources.getColor(R.color.transparent))
//
//                }
//                if(duration<200 && event.pointerCount==1 && event.action==1 && beforeView!=null)
//                    messagesActivity.closeMessageActions.callOnClick()
//
//                Log.d("-------a",(beforeView==null).toString())
//
//                return false
//            }
//
//        })

        holder.view.messageFor.setOnClickListener {
            holder.view.callOnClick()
        }

        holder.view.setOnClickListener {
            if(beforeView==it)
                messagesActivity.closeMessageActions.callOnClick()
        }


    }

    fun onLongClick(holder:MessageViewHolder,position: Int){
        if(beforeView!=null){
            beforeView!!.setBackgroundResource(R.drawable.message_all_selector)
            beforeView=null
        }
        beforeView = holder.view
        messagesActivity.messageStatusBar.visibility = View.GONE
        messagesActivity.messageActions.visibility = View.VISIBLE
        holder.view.setBackgroundColor(messagesActivity.resources.getColor(R.color.messageSeleted))
        messagesActivity.closeMessageActions.setOnClickListener {
            messagesActivity.messageActions.visibility = View.GONE
            messagesActivity.messageStatusBar.visibility = View.VISIBLE
            holder.view.setBackgroundResource(R.drawable.message_all_selector)
        }
        messagesActivity.copyMessage.setOnClickListener {
            val clipboard = messagesActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            val clip = ClipData.newPlainText("Copied Message", messagesList[position].message)
            clipboard!!.primaryClip = clip
            Toast.makeText(messagesActivity,"Message copied",Toast.LENGTH_SHORT).show()
        }
        messagesActivity.deleteMessage.setOnClickListener {
            val dialog = AlertDialog.Builder(messagesActivity,R.style.DialogTheme)
            dialog.setMessage("Are you sure to delete this message?")
            dialog.setCancelable(true)
            dialog.setNegativeButton("Cancel"){_,_->}
            dialog.setPositiveButton("Yes") { _, _ ->
                FirebaseDatabase.getInstance().getReference("messages/$chatId/${messagesList[position].id}").removeValue()
                Toast.makeText(messagesActivity,"Message deleted",Toast.LENGTH_SHORT).show()
                if(position == messagesList.size-1) {
                    val nlm = messagesList[if(position==0) 0 else position - 1]
                    val newData = HashMap<String?, Any?>()
                    newData["lastMessage"] = nlm.message
                    newData["timestamp"] = nlm.timestamp
                    if(position==0 && messagesList.size==1){
                        newData["lastMessage"] = ""
                    }
                    newData["from"] = if (nlm.byId == user!!.uid) nlm.byId else nlm.toId
                    FirebaseDatabase.getInstance().getReference("users/${nlm.byId}/chats/$chatId").updateChildren(newData)
                    FirebaseDatabase.getInstance().getReference("users/${nlm.toId}/chats/$chatId").updateChildren(newData)
                }
                messagesList.removeAt(position)
                notifyItemRemoved(position)
                if(beforeView!=null){
                    messagesActivity.closeMessageActions.callOnClick()
                    beforeView=null
                }

            }
            dialog.create().show()
        }
    }

    fun date(pattern:String,timestamp: Long): String {
        val date = Date(timestamp * 1000L)
        val jdf = SimpleDateFormat(pattern)
        jdf.timeZone = TimeZone.getTimeZone("GMT+4")
        return jdf.format(date)
    }
    fun dptopx(dp:Int):Int{
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),messagesActivity.resources.displayMetrics))
    }



}


    class MessageViewHolder(val view: View):RecyclerView.ViewHolder(view){

    }
