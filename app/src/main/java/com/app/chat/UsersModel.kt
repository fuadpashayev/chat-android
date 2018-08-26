package com.app.chat

class UsersModel {
    var Email:String?=null
    var Gender:String?=null
    var Id:String?=null
    var Name:String?=null
    var Photo:String?="http://pashayev.info/chat/images/noavatar.png"
    var Exit:Int?=null

    constructor(){}
    constructor(Email:String?,Gender:String?,Id:String?,Name:String?,Photo:String?="http://pashayev.info/chat/images/noavatar.png",Exit:Int?=0){
        this.Email = Email
        this.Gender = Gender
        this.Id = Id
        this.Name = Name
        this.Photo = Photo
        this.Exit = Exit
    }


}