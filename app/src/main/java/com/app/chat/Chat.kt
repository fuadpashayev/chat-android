package com.app.chat


import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.chatbox_layout.view.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.view.*


class Chat : Fragment() {
    var auth:FirebaseAuth?=null
    var user:FirebaseUser?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_chat, container, false)
        val boxes:ArrayList<String> = arrayListOf("test1","test2","aloha")
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        val ChatData:ArrayList<String>? = arrayListOf()

        val query = FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/chats")
//        query.addValueEventListener(object:ValueEventListener{
//            override fun onDataChange(snap: DataSnapshot?) {
//                val data = snap!!.children.iterator()
//
//                for(i in data){
//                    val chatData = i.getValue(ChatBoxModel::class.java)
//                    ChatData!!.add(chatData!!.lastMessage!!)
//                }
//                rootView.chatBoxes.layoutManager = LinearLayoutManager(context)
//                rootView.chatBoxes.adapter = ChatBoxAdapter(ChatData!!)
//
//            }
//
//            override fun onCancelled(p0: DatabaseError?) {}
//        })







        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<ChatBoxModel, ChatBoxViewHolder>(
                ChatBoxModel::class.java,
                R.layout.chatbox_layout,
                ChatBoxViewHolder::class.java,
                query.orderByChild("timestamp")

        ){

            override fun populateViewHolder(viewHolder: ChatBoxViewHolder?, model: ChatBoxModel?, position: Int) {
                rootView.loader.visibility = View.GONE
                viewHolder!!.itemView.chatBoxMessage.text = model!!.lastMessage
                viewHolder.itemView.chatBoxName.text = model.with
                viewHolder.itemView.setOnClickListener {
                    val intent = Intent(activity,Messages::class.java)
                    intent.putExtra("with",model.with)
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
