package tkhub.project.kesbewa.admin.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tkhub.project.kesbewa.admin.data.models.Zone

@Dao
interface ZoneDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCart(zone: Zone)

    @Query("SELECT * FROM zone ")
    suspend fun selectZones(): List<Zone>

    @Query("SELECT * FROM zone WHERE area = :userArea ")
    suspend fun getZoneFromArea(userArea : String): Zone



}