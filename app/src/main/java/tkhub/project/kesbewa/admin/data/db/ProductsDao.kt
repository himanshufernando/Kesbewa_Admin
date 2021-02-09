package tkhub.project.kesbewa.admin.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import tkhub.project.kesbewa.admin.data.models.Products
import tkhub.project.kesbewa.admin.data.models.Zone

@Dao
interface ProductsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(pro: Products)

    @Query("SELECT * FROM products")
    suspend fun getProducts(): List<Products>


}