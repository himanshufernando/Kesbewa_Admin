package tkhub.project.kesbewa.admin.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Products (
    var pro_id: Long,
    var pro_name: String,
    var pro_price: Double,
    var pro_make: String,
    var pro_description: String,
    var pro_cover_img: String,
    var pro_catagory: String,
    var pro_code: String,
    var is_sold_out : Int


): Parcelable { constructor(): this(0,"" ,0.0,"","","","","", 0)

}