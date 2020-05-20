package tkhub.project.kesbewa.admin.data.models

data class Customer(
    var user_id: String?,
    var user_code: String?,
    var user_name: String?,
    var user_phone: String?,
    var user_email: String?,
    var user_nic: String?,
    var user_login: String?,
    var user_pro_pic: String?,
    var user_android_id: String?,
    var user_type: Long?,
    var push_id: String?,
    var user_confirm_password: String

) {
    constructor() : this("",
        "", "",
        "", "", "",
        "", "","",0,"",""
    )

}
