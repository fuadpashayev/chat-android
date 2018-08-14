package com.app.chat


class ChatBoxModel {
    var lastMessage:String?=null
    var timestamp:Int?=null
    var withId:String?=null
    var withName:String?=null
    var photo:String?=null
    var id:String?=null
    constructor(){}
    constructor(lastMessage:String?,timestamp:Int?,withId:String?,withName:String?,photo:String?,id:String?){
        this.lastMessage = lastMessage
        this.timestamp = timestamp
        this.withId = withId
        this.withName = withName
        this.photo = photo
        this.id = id
    }
}