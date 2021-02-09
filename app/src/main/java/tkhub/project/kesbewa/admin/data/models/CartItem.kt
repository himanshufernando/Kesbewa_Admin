package tkhub.project.kesbewa.admin.data.models


data class CartItem (
    var cart_id: Long?,
    var pro_id: Long?,
    var pro_name: String,
    var pro_price: Double?,
    var pro_size: String?,
    var pro_image: String?,
    var pro_colour: String?,
    var pro_colour_code: String?,
    var pro_total_qty: Int?,
    var pro_total_price: Double?,
    var cartStatus: Boolean?,
    var pro_code: String?,
    var pro_weight: Double?,
    var pro_stock: Int?
) {
    constructor() : this(0, 0,"",0.0,
        "","", "",
        "", 0,0.0,false,"",0.0,0
    )

}