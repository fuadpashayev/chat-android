package com.app.chat

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.chatbox_layout.view.*
import java.sql.Timestamp

class ChatBoxModel {
    var lastMessage:String?=null
    var timestamp:Int?=null
    var with:String?=null
    constructor(){}
    constructor(lastMessage:String?,timestamp:Int?,with:String?){
        this.lastMessage = lastMessage
        this.timestamp = timestamp
        this.with = with
    }
}