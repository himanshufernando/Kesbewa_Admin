package tkhub.project.kesbewa.admin.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Promo (
    var promo_type: Long,
    var promocode: String,
    var promocode_add_date: Long,
    var promocode_add_user: String,
    var promocode_id: Long,
    var promocode_type_code: String,
    var promocode_validate: Boolean,
    var promocode_value: Double


): Parcelable { constructor(): this(0,"" ,0,"",0,"",false, 0.0)

}