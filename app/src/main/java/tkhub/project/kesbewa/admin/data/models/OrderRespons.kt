package tkhub.project.kesbewa.admin.data.models


data class OrderRespons (
    var order_id: String,
    var order_code: String,
    var order_user_id: String?,
    var order_android_id: String?,
    var order_date: String?,
    var order_note: String?,
    var order_promo_code: String?,
    var order_promo_code_id: String?,
    var order_payment_type_code: String?,
    var order_payment_type: String?,
    var order_total_qty: Int?,
    var order_discount: Double?,
    var order_subtotal_total: Double?,
    var order_total_price: Double?,
    var order_delivery_chargers: Double?,
    var order_status: String,
    var order_status_code: Int,
    var order_status_note: String,
    var order_delivery_time: String?,
    var delivery_address : DeliveryAddress,
    var user : Customer,
    var itemlist: List<CartItem>?,
    var admin: String

) {
    constructor() : this("", "","","",
        "","", "",
        "", "","",0,0.0
        ,0.0,0.0,0.0,"" ,0,"","3-6",DeliveryAddress(), Customer(),emptyList(),""
    )


}
