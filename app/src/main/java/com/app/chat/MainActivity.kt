package com.app.chat

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        openSignIn.setOnClickListener {
            signUpFrame.visibility = View.GONE
            signInFrame.visibility = View.VISIBLE
        }

        openSignUp.setOnClickListener {
            signInFrame.visibility = View.GONE
            signUpFrame.visibility = View.VISIBLE
        }





        signIn.setOnClickListener {
            val login = login_email.text
            val pass = login_pass.text

            if(login.count()>3 && pass.count()>3){
                Log.d("------result","login: $login , pass: $pass")
            }else{
                Log.d("------result","ERRRRROOOORRRR")
            }

        }

    }
}
