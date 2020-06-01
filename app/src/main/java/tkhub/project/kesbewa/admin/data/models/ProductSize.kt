package tkhub.project.kesbewa.admin.data.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


 data class ProductSize (
    var size: String?,
    var price: Double?,
    var isAvailable : Boolean
)  {
    constructor(): this("0",0.0,true )
}