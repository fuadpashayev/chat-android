package com.app.chat

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.widget.Toast
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    var auth:FirebaseAuth?=null
    var user:FirebaseUser?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        user=auth?.currentUser

        if(user==null) {

            openSignIn.setOnClickListener {
                signUpFrame.animate().translationX(signInFrame.width.toFloat())

                Handler().postDelayed({
                    signInFrame.animate().translationX(0f)
                },100)



            }

            openSignUp.setOnClickListener {
                signInFrame.animate().translationX(signInFrame.width.toFloat())

                Handler().postDelayed({
                    signUpFrame.animate().translationX(0f)
                },100)


            }





            signIn.setOnClickListener {
                val login = login_email.text.trim().toString()
                val pass = login_pass.text.trim().toString()

                if (login.count() > 3 && pass.count() > 3) {

                    auth!!.signInWithEmailAndPassword(login, pass).addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                        if (task.isSuccessful) {
                            user = auth!!.getCurrentUser()
                            Toast.makeText(this, "You Signed In.", Toast.LENGTH_SHORT).show()
                            startHome()
                        } else
                            Toast.makeText(this, "Email or Password is Incorrect.",Toast.LENGTH_SHORT).show()
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

                    auth!!.createUserWithEmailAndPassword(login, pass)
                            .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                                if (task.isSuccessful) {
                                    user = auth!!.getCurrentUser()
                                    val usersRef = FirebaseDatabase.getInstance().getReference("users/${user!!.uid}")
                                    val newUser = Users(login,gender,user!!.uid,name)
                                    usersRef.setValue(newUser)
                                    startHome()
                                    Toast.makeText(this, "You Successfully Registered.", Toast.LENGTH_SHORT).show()
                                } else
                                    Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show()


                            })

                } else {
                    Toast.makeText(this, "You should fill all the fields.",Toast.LENGTH_SHORT).show()
                }

            }
        }else{
           startHome()
        }

    }

    fun startHome(){
        val intent = Intent(this,Home::class.java)
        startActivity(intent)
    }



}
