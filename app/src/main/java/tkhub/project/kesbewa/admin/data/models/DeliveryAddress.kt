package tkhub.project.kesbewa.admin.data.models


data class DeliveryAddress (
    var address_id: String?="",
    var user_id: String?,
    var user_code: String?,
    var user_lat: Double?,
    var user_lon: Double?,
    var user_address_number: String,
    var user_address_one: String,
    var user_address_two: String?,
    var user_address_city: String,
    var user_address_district: String?,
    var user_address_province: String?,
    var user_address_zip: String,
    var user_isSelect :Boolean = false


) {
    constructor() : this("","",
        "", 0.0,0.0,"","","","","","",
        "",false
    )

}
