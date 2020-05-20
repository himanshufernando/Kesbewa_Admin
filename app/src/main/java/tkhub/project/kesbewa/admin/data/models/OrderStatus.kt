package tkhub.project.kesbewa.admin.data.models

data class OrderStatus (
    var order_status_id: String,
    var order_status_order_id: String,
    var order_status_order_code: String,
    var order_status_note: String,
    var order_status: String,
    var order_status_code: Int,
    var order_status_user: User,
    var order_status_date: String,
    var order_status_time: String

){
    constructor() : this("",
        "", "",
        "", "", 0,User(),"",""
    )
}