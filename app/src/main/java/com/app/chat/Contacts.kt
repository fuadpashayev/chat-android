package com.app.chat


import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.fragment_contacts.view.*
import kotlinx.android.synthetic.main.user_layout.view.*


class Contacts : Fragment() {
    private var auth:FirebaseAuth? = null
    private var user:FirebaseUser?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_contacts, container, false)
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser

        val query = FirebaseDatabase.getInstance().getReference("users")
        val contactsAdapter = object : FirebaseRecyclerAdapter<UsersModel, UsersViewHolder>(
                UsersModel::class.java,
                R.layout.user_layout,
                UsersViewHolder::class.java,
                query

        ){

            override fun populateViewHolder(viewHolder: UsersViewHolder?, model: UsersModel?, position: Int) {
                if(model!!.Id!=user!!.uid) {
                    viewHolder!!.itemView.userName.text = model.Name
                    val imgHolder = viewHolder.itemView.userPhoto
                    imgHolder.clipToOutline = true
                    Glide.with(context!!)
                            .load(model.Photo)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .thumbnail(Glide.with(context!!).load(R.mipmap.loader))
                            .fitCenter()
                            .centerCrop()
                            .crossFade(1000)
                            .into(imgHolder)
                    viewHolder.itemView.setOnClickListener {
                        val parentActivity = (activity as Home)
                        parentActivity.changeActivity = true
                        val intent = Intent(activity,Messages::class.java)
                        intent.putExtra("withId",model.Id)
                        intent.putExtra("withName",model.Name)
                        intent.putExtra("withPhoto",model.Photo)
                        var chatId = "0"
                        FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/chats").orderByChild("withId").equalTo(model.Id).addListenerForSingleValueEvent(object:ValueEventListener{
                            override fun onCancelled(p0: DatabaseError?) {}
                            override fun onDataChange(snap: DataSnapshot?) {
                                if(snap!!.exists()){
                                    for(snappo in snap.children){
                                        chatId = snappo.child("id").getValue(String::class.java)!!

                                    }


                                }
                                intent.putExtra("chatId",chatId)
                                startActivity(intent)
                            }

                        })





                    }
                }else {
                    viewHolder!!.itemView.layoutParams = LinearLayout.LayoutParams(0,0)
                    viewHolder.itemView.visibility = View.GONE
                }
            }

        }

        rootView.userList.adapter = contactsAdapter

        return rootView
    }
    fun dptopx(dp:Int):Int{
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),resources.displayMetrics))
    }


    class UsersViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!)


}
