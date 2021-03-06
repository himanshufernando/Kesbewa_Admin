package tkhub.project.kesbewa.admin.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import androidx.sqlite.db.SupportSQLiteDatabase
import tkhub.project.kesbewa.admin.data.models.Products
import tkhub.project.kesbewa.admin.data.models.Zone

@Database(entities = [Zone::class, Products::class], version = 1, exportSchema = false)
abstract class AppDatabase  : RoomDatabase() {

    abstract fun zoneDao() : ZoneDao
    abstract fun productsDao() : ProductsDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "kesbaewaadmindb")
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }
                })
                .build()
        }
    }


}