package tkhub.project.kesbewa.admin.data.models

data class User (
    var admin_id: String,
    var admin_level: Int,
    var admin_name: String,
    var admin_number: String,
    var admin_push: String,
    var admin_password: String

) {
    constructor() : this("",
        0, "",
        "", "", ""
    )




}