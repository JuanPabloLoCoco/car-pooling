package it.polito.mad.car_pooling.models

// data class  Model(val name: String= "", val count: Int = 0)
data class Trip (var id: String) {
    companion object {
        val EDIT_TRIP: String = "edit"
        val CREATE_TRIP: String = "create"
        val NEW_TRIP_ID: Int = -1
    }

    var depLocation: String = ""
    var ariLocation: String=""
    var depDate: String = ""
    var depTime: String = ""
    var estDuration: String = ""
    var avaSeat: String = ""
    var price: String = ""
    var additional: String = ""
    var optional: String = ""
    var plate: String = ""
    var imageUri: String = ""
    var owner: String=""
}