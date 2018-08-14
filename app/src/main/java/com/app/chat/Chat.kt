package com.app.chat


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.chatbox_layout.view.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class Chat : Fragment() {
    var auth:FirebaseAuth?=null
    var user:FirebaseUser?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_chat, container, false)
        val boxes:ArrayList<String> = arrayListOf("test1","test2","aloha")
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser






        val query = FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/chats")

        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<ChatBoxModel, ChatBoxViewHolder>(
                ChatBoxModel::class.java,
                R.layout.chatbox_layout,
                ChatBoxViewHolder::class.java,
                query.orderByChild("timestamp")

        ){

            override fun populateViewHolder(viewHolder: ChatBoxViewHolder?, model: ChatBoxModel?, position: Int) {
                rootView.loader.visibility = View.GONE
                viewHolder!!.itemView.chatBoxMessage.text = model!!.lastMessage
                val imgHolder = viewHolder.itemView.chatPhoto
                imgHolder.setClipToOutline(true)

                Glide.with(context!!)
                        .load(model.photo)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .thumbnail(Glide.with(context!!).load(R.mipmap.loader))
                        .fitCenter()
                        .centerCrop()
                        .crossFade(1000)
                        .into(imgHolder)
                viewHolder.itemView.chatBoxName.text = model.withName
                viewHolder.itemView.setOnClickListener {
                    val intent = Intent(activity,Messages::class.java)
                    intent.putExtra("withId",model.withId)
                    intent.putExtra("withName",model.withName)
                    intent.putExtra("withPhoto",model.photo)
                    intent.putExtra("chatId",model.id)
                    startActivity(intent)
                }
            }

        }

        rootView.chatBoxes.adapter = firebaseRecyclerAdapter





        return rootView
    }





    class ChatBoxViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }


}
