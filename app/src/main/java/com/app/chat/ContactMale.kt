package com.app.chat


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_contacts.view.*
import kotlinx.android.synthetic.main.user_layout.view.*

class ContactMale : Fragment() {
    private var auth: FirebaseAuth? = null
    private var user: FirebaseUser?=null
    var myUser:UsersModel?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_contact_all, container, false)


        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        FirebaseDatabase.getInstance().getReference("users/${user!!.uid}").addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(snap: DataSnapshot?) {
                myUser = snap!!.getValue(UsersModel::class.java)
            }
        })
        val query = FirebaseDatabase.getInstance().getReference("users")


        query!!.orderByChild("gender").equalTo("Male").limitToLast(2).addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onDataChange(snap: DataSnapshot?) {
                if(!snap!!.exists() || (snap.childrenCount==1L && myUser!!.Gender=="Male")){
                    rootView.loader.visibility = View.GONE
                    rootView.noContacts.visibility = View.VISIBLE
                }else rootView.noContacts.visibility = View.GONE
            }

        })

        val contactsAdapter = object : FirebaseRecyclerAdapter<UsersModel, Contacts.UsersViewHolder>(
                UsersModel::class.java,
                R.layout.user_layout,
                Contacts.UsersViewHolder::class.java,
                query.orderByChild("gender").equalTo("Male")

        ){

            override fun populateViewHolder(viewHolder: Contacts.UsersViewHolder?, model: UsersModel?, position: Int) {
                rootView.loader.visibility = View.GONE
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
                        rootView.loader.visibility = View.VISIBLE
                        FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/chats").orderByChild("withId").equalTo(model.Id).addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onCancelled(p0: DatabaseError?) {}
                            override fun onDataChange(snap: DataSnapshot?) {
                                if(snap!!.exists()){
                                    for(snappo in snap.children){
                                        chatId = snappo.child("id").getValue(String::class.java)!!
                                    }
                                }else{
                                    val query = FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/chats").push()
                                    chatId=query.key
                                    val timestamp = System.currentTimeMillis()/100
                                    val status = model.Gender==myUser!!.Gender
                                    query.setValue(ChatBoxModel("",timestamp,model.Id,model.Name,model.Photo,chatId,user!!.uid,true))
                                }
                                rootView.loader.visibility = View.GONE
                                intent.putExtra("chatId",chatId)
                                intent.putExtra("gender",model.Gender)
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


}
