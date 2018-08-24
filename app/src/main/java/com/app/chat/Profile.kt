package com.app.chat



import android.app.ActionBar
import android.app.Activity
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.popup.view.*
import java.io.*


class Profile : Fragment() {
    var auth:FirebaseAuth?=null
    var user:FirebaseUser?=null
    var myUser:UsersModel?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       val rootView= inflater.inflate(R.layout.fragment_profile, container, false)
        auth = FirebaseAuth.getInstance()
        user = auth!!.currentUser
        rootView.loader.visibility = View.VISIBLE
        FirebaseDatabase.getInstance().getReference("users/${user!!.uid}").addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {}
            override fun onDataChange(snap: DataSnapshot?) {
                rootView.loader.visibility = View.GONE
                myUser = snap!!.getValue(UsersModel::class.java)

                val imgHolder = rootView.profileImage
                imgHolder.clipToOutline = true

//                Glide.with(context!!)
//                        .load(myUser!!.Photo)
//                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
//                        .thumbnail(Glide.with(context).load(R.mipmap.loader))
//                        .fitCenter()
//                        .centerCrop()
//                        .crossFade(1000)
//                        .into(imgHolder)

                rootView.profileActions.setOnClickListener {
                    val inflaterPop = activity!!.applicationContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val view = inflaterPop.inflate(R.layout.popup,null)
                    val popup = PopupWindow(view,LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT)
                    popup.animationStyle = R.style.PopupAnimation
                    popup.showAsDropDown(rootView.profileActions,500,500,100)
                    view.closeProfileActions.setOnClickListener {
                        popup.dismiss()
                    }
                    activity!!.sliding_tabs.setOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
                        override fun onTabReselected(tab: TabLayout.Tab?) {}
                        override fun onTabUnselected(tab: TabLayout.Tab?) {}
                        override fun onTabSelected(tab: TabLayout.Tab?) {
                            popup.dismiss()
                        }

                    })
                    view.exitChat.setOnClickListener {
                        val timestamp = System.currentTimeMillis()/1000
                        FirebaseDatabase.getInstance().getReference("users/${user!!.uid}/exit").setValue(timestamp)
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(activity,MainActivity::class.java)
                        startActivity(intent)
                    }
                }


            }

        })

        rootView.imageActions.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, 1)
        }



        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 1 && resultCode === Activity.RESULT_OK) {
            val image = data!!.data
            val path = getPath(context!!,image)
            val bitmap = BitmapFactory.decodeFile(path)
           profileImage.setImageBitmap(bitmap)
            //val outputStream = ByteArrayOutputStream()
            //bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream)


//                Log.d("-------b1",compress.toString())

            //Log.d("-------b",path.toString())
            Log.d("-------b2",bitmap.toString())

        }
    }

    fun getPath(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor!!.moveToFirst()
            return cursor!!.getString(column_index)
        } finally {
            if (cursor != null) {
                cursor!!.close()
            }
        }
    }


    fun dptopx(dp:Int):Int{
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),resources.displayMetrics))
    }


}
