package com.app.chat


class ChatBoxModel {
    var lastMessage:String?=null
    var timestamp:Long?=null
    var withId:String?=null
    var withName:String?=null
    var photo:String?=null
    var id:String?=null
    var from:String?=null
    var status:Boolean?=null
    var messageCount:Int?=null
    constructor(){}
    constructor(lastMessage:String?,timestamp:Long?,withId:String?,withName:String?,photo:String?,id:String?,from:String?,status:Boolean?,messageCount:Int?){
        this.lastMessage = lastMessage
        this.timestamp = timestamp
        this.withId = withId
        this.withName = withName
        this.photo = photo
        this.id = id
        this.from = from
        this.status = status
        this.messageCount = messageCount
    }
}