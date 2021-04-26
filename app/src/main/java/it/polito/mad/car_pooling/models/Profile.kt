package it.polito.mad.car_pooling.models

data class Profile (var fullName: String){
    var nickName: String = ""
    var email: String = ""
    var location: String = ""
    var phoneNumber: String = ""
    var birthday: String = ""
    var imageUri: String = ""
    var id: Int = 0
}