package tkhub.project.kesbewa.admin.data.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize



@Parcelize
 data class ProductSize (
    var sizeID: Int?,
    var size: String?,
    var price: Double?,
    var isAvailable : Boolean,
    var stock: Int?,
    var weight: Int?
) : Parcelable {
    constructor(): this(0,"0",0.0,true ,0,0)
}