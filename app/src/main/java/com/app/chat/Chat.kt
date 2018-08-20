package com.app.chat


import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.chatbox_layout.view.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.view.*
import java.util.*


class Chat : Fragment() {
    var auth:FirebaseAuth?=null
    var user:FirebaseUser?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_chat, container, false)
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser

        val query = FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/chats")


        query.limitToLast(1).addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onDataChange(snap: DataSnapshot?) {
               if(!snap!!.exists()){
                   rootView.loader.visibility = View.GONE
                   rootView.startNewChat.visibility = View.VISIBLE
               }else rootView.startNewChat.visibility = View.GONE
            }

        })

        rootView.startNewChat.setOnClickListener {
            activity!!.viewPager.setCurrentItem(1,true)
        }

        val firebaseAdapter = object : FirebaseRecyclerAdapter<ChatBoxModel, ChatBoxViewHolder>(
                ChatBoxModel::class.java,
                R.layout.chatbox_layout,
                ChatBoxViewHolder::class.java,
                query.orderByChild("timestamp")

        ){

            override fun populateViewHolder(viewHolder: ChatBoxViewHolder?, model: ChatBoxModel?, position: Int) {
                rootView.loader.visibility = View.GONE
                rootView.startNewChat.visibility = View.GONE
                viewHolder!!.itemView.chatBoxMessage.text = model!!.lastMessage
                if(model.lastMessage=="") viewHolder.itemView.chatBoxMessage.visibility = View.GONE
                else viewHolder.itemView.chatBoxMessage.visibility = View.VISIBLE
                val imgHolder = viewHolder.itemView.chatPhoto
                imgHolder.clipToOutline = true

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
                    (activity as Home).changeActivity = true
                    val intent = Intent(activity,Messages::class.java)
                    intent.putExtra("withId",model.withId)
                    intent.putExtra("withName",model.withName)
                    intent.putExtra("withPhoto",model.photo)
                    intent.putExtra("chatId",model.id)
                    startActivity(intent)
                }
            }

        }

        rootView.chatBoxes.adapter = firebaseAdapter


        return rootView
    }

    fun dptopx(dp:Int):Int{
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),resources.displayMetrics))
    }



    class ChatBoxViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!)


}
