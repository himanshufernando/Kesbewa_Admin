package tkhub.project.kesbewa.admin.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

class ProductColor (
    var color_name: String?,
    var color_code: String?,
    var isAvailable : Boolean
)  {
    constructor(): this("0","0",true )
}