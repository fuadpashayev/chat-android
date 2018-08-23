package com.app.chat


import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.text.Html
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
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


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

                var gender:String?=null
                FirebaseDatabase.getInstance().getReference("users/${model!!.withId}").addListenerForSingleValueEvent(object:ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {}
                    override fun onDataChange(snap: DataSnapshot?) {
                        val data = snap!!.child("gender").getValue(String::class.java)
                        gender = data
                    }

                })



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

                viewHolder.itemView.setOnLongClickListener {
                    val dialog = AlertDialog.Builder(activity,R.style.DialogTheme)
                    val items = arrayOf<String>("Clear Chat","Delete Chat")
                    dialog.setCancelable(true)
                    dialog.setNegativeButton("Cancel",{_,_->})
                    dialog.setItems(items){_,item->
                        when(item){
                            0->{
                                val inDialog = AlertDialog.Builder(activity,R.style.DialogTheme)
                                inDialog.setCancelable(true)
                                inDialog.setTitle(Html.fromHtml("<font color='#333'>Are you sure to clear chat?</font>"))
                                inDialog.setNegativeButton("Cancel",{_,_->})
                                inDialog.setPositiveButton("Yes") {_,_->
                                    val data = HashMap<String,Any>()
                                    data["lastMessage"] = ""
                                    FirebaseDatabase.getInstance().getReference("messages/${model.id}").removeValue()
                                    FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/chats/${model.id}").updateChildren(data)
                                    FirebaseDatabase.getInstance().getReference("users/${model.withId}/chats/${model.id}").updateChildren(data)
                                }
                                inDialog.create().show()
                            }
                            1->{
                                val inDialog = AlertDialog.Builder(activity,R.style.DialogTheme)
                                inDialog.setCancelable(true)
                                inDialog.setTitle(Html.fromHtml("<font color='#333'>Are you sure to delete chat?</font>"))
                                inDialog.setNegativeButton("Cancel",{_,_->})
                                inDialog.setPositiveButton("Yes") {_,_->
                                    FirebaseDatabase.getInstance().getReference("messages/${model.id}").removeValue()
                                    FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/chats/${model.id}").removeValue()
                                    FirebaseDatabase.getInstance().getReference("users/${model.withId}/chats/${model.id}").removeValue()
                                }
                                inDialog.create().show()
                            }
                        }
                    }
                    dialog.create().show()


                    true
                }

                viewHolder.itemView.setOnClickListener {
                    (activity as Home).changeActivity = true
                    val intent = Intent(activity,Messages::class.java)
                    intent.putExtra("withId",model.withId)
                    intent.putExtra("withName",model.withName)
                    intent.putExtra("withPhoto",model.photo)
                    intent.putExtra("chatId",model.id)
                    intent.putExtra("gender",gender)



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
