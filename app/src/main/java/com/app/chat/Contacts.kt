package com.app.chat


import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
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
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_contacts.view.*
import kotlinx.android.synthetic.main.user_layout.view.*


class Contacts : Fragment() {
    private var auth:FirebaseAuth? = null
    private var user:FirebaseUser?=null
    var myUser:UsersModel?=null
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_all -> {
                callFragment("all")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_male -> {
                callFragment("male")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_female -> {
                callFragment("female")
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    fun callFragment(fragmentName:String?){
        val manager = fragmentManager
        val transaction = manager!!.beginTransaction()
        val fragment = when(fragmentName){
            "all"->ContactAll()
            "male"->ContactMale()
            "female"->ContactFemale()
            else->ContactAll()
        }
        transaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out)
        val currentFragment = manager.findFragmentByTag(fragmentName)
        if(currentFragment==null)
            transaction.replace(R.id.main_frame,fragment,fragmentName).commit()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_contacts, container, false)
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        FirebaseDatabase.getInstance().getReference("users/${user!!.uid}").addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(snap: DataSnapshot?) {
                myUser = snap!!.getValue(UsersModel::class.java)
            }
        })
        rootView.contactNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val query = FirebaseDatabase.getInstance().getReference("users")


        query!!.limitToLast(2).addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}

            override fun onDataChange(snap: DataSnapshot?) {
                if(!snap!!.exists() || snap.childrenCount==1L){
                    rootView.loader.visibility = View.GONE
                    rootView.noContacts.visibility = View.VISIBLE
                }else rootView.noContacts.visibility = View.GONE
            }

        })

        val contactsAdapter = object : FirebaseRecyclerAdapter<UsersModel, UsersViewHolder>(
                UsersModel::class.java,
                R.layout.user_layout,
                UsersViewHolder::class.java,
                query

        ){

            override fun populateViewHolder(viewHolder: UsersViewHolder?, model: UsersModel?, position: Int) {
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
                        FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/chats").orderByChild("withId").equalTo(model.Id).addListenerForSingleValueEvent(object:ValueEventListener{
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
                                    query.setValue(ChatBoxModel("",timestamp,model.Id,model.Name,model.Photo,chatId,user!!.uid,true,0))
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
    fun dptopx(dp:Int):Int{
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),resources.displayMetrics))
    }


    class UsersViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!)


}
