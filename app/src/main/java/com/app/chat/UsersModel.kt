package com.app.chat

class UsersModel {
    var Email:String?=null
    var Gender:String?=null
    var Id:String?=null
    var Name:String?=null
    var Photo:String?=null
    var Exit:Int?=null

    constructor(){}
    constructor(Email:String?,Gender:String?,Id:String?,Name:String?,Photo:String?="https://alsosto.org/wp-content/uploads/2017/08/noavatar.png",Exit:Int?=0){
        this.Email = Email
        this.Gender = Gender
        this.Id = Id
        this.Name = Name
        this.Photo = Photo
        this.Exit = Exit
    }


}