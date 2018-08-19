package com.app.chat



import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.os.Handler
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.widget.Toast
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.FirebaseDatabase



class MainActivity : AppCompatActivity() {
    var auth:FirebaseAuth?=null
    var user:FirebaseUser?=null
    fun loader(){
        if(loader.visibility == View.GONE)
            loader.visibility = View.VISIBLE
        else
            loader.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.hide()
        auth = FirebaseAuth.getInstance()
        user=auth?.currentUser

        if(user==null) {

            openSignIn.setOnClickListener {
                signUpFrame.animate().translationX(signInFrame.width.toFloat())
                Handler().postDelayed({
                    signInFrame.animate().translationX(0f)
                    reg_email.text.clear()
                    reg_pass.text.clear()
                    reg_name.text.clear()
                },100)
            }

            openSignUp.setOnClickListener {
                signInFrame.animate().translationX(signInFrame.width.toFloat())
                Handler().postDelayed({
                    signUpFrame.animate().translationX(0f)
                    login_email.text.clear()
                    login_pass.text.clear()
                },100)
            }


            fun hideKeyboard() {
                val view = this.currentFocus
                if (view != null) {
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }

            showPassword.setOnClickListener {
               if(login_pass.inputType == 129) {
                   showPassword.setImageResource(R.drawable.ic_hide)
                   login_pass.inputType = 1
               }else{
                   showPassword.setImageResource(R.drawable.ic_show)
                   login_pass.inputType = 129
               }
            }


            signIn.setOnClickListener {
                val login = login_email.text.trim().toString()
                val pass = login_pass.text.trim().toString()

                if (login.count() > 3 && pass.count() > 3) {
                    hideKeyboard()
                    loader()
                    auth!!.signInWithEmailAndPassword(login, pass).addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                        if (task.isSuccessful) {
                            user = auth!!.currentUser
                            Toast.makeText(this, "You Signed In.", Toast.LENGTH_SHORT).show()
                            startHome()
                            login_email.text.clear()
                            login_pass.text.clear()
                        } else
                            Toast.makeText(this, "Email or Password is Incorrect.",Toast.LENGTH_SHORT).show()
                        loader()
                    })

                } else {
                    Toast.makeText(this, "You should fill all the fields.",Toast.LENGTH_SHORT).show()
                }

            }

            signUp.setOnClickListener {
                val login = reg_email.text.trim().toString()
                val pass = reg_pass.text.trim().toString()
                val gender = reg_gender.selectedItem.toString()
                val name = reg_name.text.trim().toString()

                if (login.count() > 3 && pass.count() > 3 && gender.count()<=6 && name.count() > 3) {
                    hideKeyboard()
                    loader()
                    auth!!.createUserWithEmailAndPassword(login, pass)
                            .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                                if (task.isSuccessful) {
                                    user = auth!!.currentUser
                                    val usersRef = FirebaseDatabase.getInstance().getReference("users/${user!!.uid}")
                                    val newUser = UsersModel(login,gender,user!!.uid,name)
                                    usersRef.setValue(newUser)
                                    startHome()
                                    Toast.makeText(this, "You Successfully Registered.", Toast.LENGTH_SHORT).show()
                                    login_email.text.clear()
                                    login_pass.text.clear()
                                } else
                                    Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show()

                                loader()
                            })

                } else {
                    Toast.makeText(this, "You should fill all the fields.",Toast.LENGTH_SHORT).show()
                }

            }
        }else{
           startHome()
        }

    }

    private fun startHome(){
        val intent = Intent(this,Home::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    override fun onBackPressed() {}
}
