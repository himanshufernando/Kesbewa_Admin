package tkhub.project.kesbewa.admin.data.models

data class ProductImage (
    var img_id: Long?,
    var img_url: String?,
    var pro_id : String,
    var pro_code : String
)
{
    constructor(): this(0,"","","" )
}