package com.app.chat

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.message_layout.view.*
import java.text.SimpleDateFormat
import java.util.*


class messageAdapter(var messagesList:ArrayList<MessageModel>,var withPhoto:String?,var messagesActivity:Activity,var messages:RecyclerView): RecyclerView.Adapter<MessageViewHolder>(){
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
        Log.d("-------aaa","create")
        data = messagesList.get(viewType)
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
        messages.scrollToPosition(messages.adapter!!.itemCount - 1)
        if(viewType == messagesList.size-1){
            val animation = AnimationUtils.loadAnimation(messagesActivity, R.anim.slide_top)
            holder.view.messageBubble.startAnimation(animation)
        }

        return holder
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
       // Log.d("-------aaa",messagesList.size.toString())
       // Log.d("-------aaa1",messages.adapter!!.itemCount.toString())
        holder.view.messageText.text = messagesList.get(position).message
        holder.view.messageTime.text = date("HH:mm",messagesList.get(position).timestamp!!.toLong())

    }
    fun date(pattern:String,timestamp: Long): String {
        val date = Date(timestamp * 1000L)
        val jdf = SimpleDateFormat(pattern)
        jdf.timeZone = TimeZone.getTimeZone("GMT-4")
        val java_date = jdf.format(date)
        return java_date
    }


}


    class MessageViewHolder(val view: View):RecyclerView.ViewHolder(view){

    }
