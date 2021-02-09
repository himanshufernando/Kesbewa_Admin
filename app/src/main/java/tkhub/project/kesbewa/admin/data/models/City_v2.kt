package tkhub.project.kesbewa.admin.data.models
data class City_v2 (
    var id: String,
    var district_id: String,
    var name_en: String,
    var postcode: String,
    var latitude: String,
    var longitude: String,
    var area: String
) {


    constructor() : this("", "",
        "","","",
        "",""
    )

}
