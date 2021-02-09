package tkhub.project.kesbewa.admin.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zone")
data class Zone(
    @PrimaryKey
    var id: Long?,
    var area: String?,
    var delivery_charges: Int,
    var delivery_time: String

) {
    constructor() : this(0, "0",0, "")
}