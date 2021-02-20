package tkhub.project.kesbewa.admin.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.play.core.splitcompat.f
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


@Parcelize
data class ProductsModel (
    var pro_id: Long,
    var pro_name: String,
    var pro_price: Double,
    var pro_make: String,
    var pro_description: String,
    var pro_cover_img: String,
    var pro_catagory: String,
    var pro_code: String,
    var sold_out: Boolean = false,
    var pro_sort : Int,
    var pro_video : String = "",
    var pro_note : String = "",
    var pro_price_regular : Double ,
    var is_sold_out : Int,
    var pro_per_item_cost : String = "",
    var pro_weight : Double,
    var pro_stock : Int,
    var size : ArrayList<ProductSize>,
    var is_order_details_expain : Boolean


): Parcelable { constructor(): this(0,"" ,0.0,"","","","","", false,0,""
    ,"",0.0,0,"",0.0,0, ArrayList<ProductSize>(),false)

}