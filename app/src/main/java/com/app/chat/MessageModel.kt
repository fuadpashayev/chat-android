package com.app.chat


class MessageModel {
    var byId:String?=null
    var message:String?=null
    var seen:Int?=null
    var timestamp:Long?=null
    var toId:String?=null
    var id:String?=null
    constructor(){}
    constructor(byId:String?,message:String?,seen:Int?,timestamp:Long?,toId:String?,id:String?){
        this.byId = byId
        this.message = message
        this.seen = seen
        this.timestamp = timestamp
        this.toId = toId
        this.id = id
    }
}