package com.app.chat



import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.ProgressDialog
import android.app.ProgressDialog.*
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
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
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.HashMap
import id.zelory.compressor.*
import java.io.File


open class Profile : Fragment() {
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

                val imgHolder = rootView.profileImageOld
                imgHolder.clipToOutline = true

                Glide.with(context!!)
                        .load(myUser!!.Photo)
                        .thumbnail(Glide.with(context).load(R.mipmap.loader))
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .crossFade()
                        .into(imgHolder)

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
        if (data!=null && resultCode != RESULT_CANCELED) {
            val image = data.data
            profileImageOld.fadeOut()
            Glide.with(context!!)
                    .load(image)
                    .thumbnail(Glide.with(context).load(R.mipmap.loader))
                    .fitCenter()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade()
                    .into(profileImageNew)
            profileImageNew.clipToOutline = true
            profileImageNew.fadeIn()

            val path = getPath(context!!,image as Uri)
            var bitmap = image.bitmap()
            val file = File(path)
            bitmap = bitmap.fixOrientation(file)
            uploadImage(bitmap)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun Uri.bitmap():Bitmap {
        val bitmap = MediaStore.Images.Media.getBitmap(activity!!.contentResolver, this)
        return bitmap
    }

    fun Bitmap.scale(width:Int,height:Int,ratio:Boolean=true):Bitmap{
        val aspect = width/this.width.toFloat()
        val newHeight = if(ratio) (this.height*aspect).toInt() else height
        return Bitmap.createScaledBitmap(this,width,newHeight,false)
    }

    fun getStringImage(bitmap: Bitmap):String {
        val compressedBitmap = bitmap.scale(300,300)
        val baos = ByteArrayOutputStream()
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes = baos.toByteArray()
        val codedImage =  Base64.encodeToString(imageBytes, Base64.DEFAULT)
        return codedImage
    }

    fun Bitmap.fixOrientation(imgFile:File):Bitmap{
        val exif = ExifInterface(imgFile.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
        Log.d("EXIF", "Exif: $orientation")
        val matrix = Matrix()
        when (orientation) {
            6 -> matrix.postRotate(90F)
            3 -> matrix.postRotate(180F)
            8 -> matrix.postRotate(270F)
        }
        return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, true) // rotating bitmap
        // rotating bitmap
    }


    private fun uploadImage(bitmap: Bitmap) {
        val UPLOAD_URL = "http://pashayev.info/chat/index.php"
        val loading = show(activity, "Uploading...", "Please wait...", false, false)
        val stringRequest = object : StringRequest(Request.Method.POST, UPLOAD_URL,
                Response.Listener<String> {
                    loading.dismiss()
                    val data = HashMap<String,Any>()
                    data["photo"] = "http://pashayev.info/chat/$it"
                    FirebaseDatabase.getInstance().getReference("users/${user!!.uid}").updateChildren(data)
                },
                Response.ErrorListener {}) {
            override fun getParams(): HashMap<String, String> {
                val image = getStringImage(bitmap)
                val params = HashMap<String, String>()
                params["image"] = image
                return params

            }
        }
        val requestQueue = Volley.newRequestQueue(activity)
        requestQueue.add(stringRequest)

    }

    fun getPath(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column)
        } finally {
            cursor?.close()
        }
    }


    fun dptopx(dp:Int):Int{
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),resources.displayMetrics))
    }

    private fun ImageView.fadeOut() {
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.duration = 500
        fadeOut.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationEnd(animation:Animation) {
                this@fadeOut.visibility = View.GONE
            }
            override fun onAnimationRepeat(animation:Animation) {}
            override fun onAnimationStart(animation:Animation) {}
        })
        this.startAnimation(fadeOut)
    }

    private fun ImageView.fadeIn() {
        val fadeOut = AlphaAnimation(0f, 1f)// 0,1 shows the layout 1,0 hides layout
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.duration = 500// time to show or hide an element
        fadeOut.setAnimationListener(object:Animation.AnimationListener {
            override fun onAnimationEnd(animation:Animation) {
                this@fadeIn.visibility = View.VISIBLE
            }
            override fun onAnimationRepeat(animation:Animation) {}
            override fun onAnimationStart(animation:Animation) {}
        })
        this.startAnimation(fadeOut)
    }


}
