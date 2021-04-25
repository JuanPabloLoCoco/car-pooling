package it.polito.mad.car_pooling.models

// data class  Model(val name: String= "", val count: Int = 0)
data class Trip (var id: Int) {
    companion object {
        val EDIT_TRIP: String = "edit"
        val CREATE_TRIP: String = "create"
    }


    var arrLocation: String=""
    var depAriLocation: String = ""
    var depDateTime: String = ""
    var estDuration: String = ""
    var avaSeat: String = ""
    var price: String = ""
    var additional: String = ""
    var optional: String = ""
    var plate: String = ""
    var trimImageUri: String = ""
}