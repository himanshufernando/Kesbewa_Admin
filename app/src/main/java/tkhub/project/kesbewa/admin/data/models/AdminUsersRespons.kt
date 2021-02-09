package tkhub.project.kesbewa.admin.data.models


data class AdminUsersRespons (
    var admin_user_id: String,
    var admin_user: String,
    var admin_user_date: String,
    var delivery_address : DeliveryAddress,
    var admin_user_user : Customer


) {
    constructor() : this("", "","",DeliveryAddress(), Customer())

}
