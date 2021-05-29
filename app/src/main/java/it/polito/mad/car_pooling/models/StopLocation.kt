package it.polito.mad.car_pooling.models

data class StopLocation(var fullAddress : String) {
    var address : String = ""
    var city: String = ""
    var country : String = ""
    var latitude : String = ""
    var longitude : String = ""
}