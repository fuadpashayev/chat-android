package com.app.chat

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.message_layout.view.*
import java.security.AccessController.getContext


class messageAdapter(var messages:ArrayList<MessageModel>,var withPhoto:String?,var messagesActivity:Activity): RecyclerView.Adapter<MessageViewHolder>(){
    var auth = FirebaseAuth.getInstance()
    var user = auth.currentUser
    var usid:String?=null
    var cell:View?=null
    var data:MessageModel?=null
    override fun getItemCount(): Int {
        return messages.size
    }

     override fun getItemViewType(position: Int): Int {
        return position
     }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        data = messages.get(viewType)
        val layoutInflater = LayoutInflater.from(parent.context)
        val inflaterLayout = if(data!!.byId==user!!.uid) R.layout.message_my_layout else R.layout.message_layout
        cell = layoutInflater.inflate(inflaterLayout,parent,false)


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
        return MessageViewHolder(cell!!)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.view.messageText.text = data!!.message
    }


}


    class MessageViewHolder(val view: View):RecyclerView.ViewHolder(view){

    }
