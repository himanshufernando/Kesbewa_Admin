package tkhub.project.kesbewa.admin.repo

import android.content.Context
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import tkhub.project.kesbewa.admin.data.db.ProductsDao
import tkhub.project.kesbewa.admin.data.models.*
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.services.network.InternetConnection
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class ProductRepo(val productsDao: ProductsDao, context: Context) {


    var mContext = context

    var database: FirebaseDatabase? = FirebaseDatabase.getInstance()
    var productsRef: DatabaseReference? = database?.getReference("Products")
    var productsImage: DatabaseReference? = database?.getReference("ProductImages")

    var appPref = AppPrefs
    suspend fun getProduct(): List<Products> {
        return productsDao.getProducts()
    }


    suspend fun getProductOnline(): Flow<ArrayList<ProductsModel>?> = callbackFlow {

        productsRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var list = ArrayList<ProductsModel>()
                for (postSnapshot in dataSnapshot.children) {
                    var item = ProductsModel().apply {
                        pro_id = postSnapshot.child("pro_id").value as Long
                        pro_name = postSnapshot.child("pro_name").value as String
                        pro_price = (postSnapshot.child("pro_price").value as Long).toDouble()
                        pro_make = postSnapshot.child("pro_make").value as String
                        pro_description = postSnapshot.child("pro_description").value as String
                        pro_cover_img = postSnapshot.child("pro_cover_img").value as String
                        pro_catagory = postSnapshot.child("pro_catagory").value as String
                        pro_code = postSnapshot.child("pro_code").value as String
                        sold_out = postSnapshot.child("sold_out").value as Boolean
                        pro_sort = (postSnapshot.child("pro_sort").value as Long).toInt()
                        pro_video = postSnapshot.child("pro_video").value as String
                        pro_note = postSnapshot.child("pro_note").value as String
                        pro_price_regular =
                            (postSnapshot.child("pro_price_regular").value as Long).toDouble()
                        pro_per_item_cost = postSnapshot.child("pro_per_item_cost").value as String
                        pro_weight = (postSnapshot.child("pro_weight").value as Long).toDouble()
                        pro_stock = (postSnapshot.child("pro_stock").value as Long).toInt()

                        var productSizeList = ArrayList<ProductSize>()

                        var countID = 1
                        for (itemSize in postSnapshot.child("size").children) {
                            var itemSize = ProductSize().apply {
                                sizeID = countID
                                size = itemSize.child("size").value as String
                                price = (itemSize.child("price").value as Long).toDouble()
                                isAvailable = itemSize.child("isAvailable").value as Boolean
                                stock = (itemSize.child("stock").value as Long).toInt()
                                weight = (itemSize.child("weight").value as Long).toInt()

                            }
                            productSizeList.add(itemSize)
                            countID++
                        }
                        size = productSizeList

                    }


                    list.add(item!!)
                }
                offer(list)

            }

            override fun onCancelled(error: DatabaseError) {
                offer(null)
            }
        })

        awaitClose { this.cancel() }

    }


    suspend fun addProductImage(pro: ProductImage): Flow<NetworkError> = callbackFlow {

        var errorAddress = NetworkError()
        when {
            AppPrefs.checkValidString(pro.img_url!!) -> {
                errorAddress.errorMessage = "Enter URLr!"
                errorAddress.errorCode = "Empty URL"
                offer(errorAddress)

            }
            else -> {

                var unxId = genarateUniqCode()
                var id = pro.pro_code + "_" + genarateUniqCode()
                pro.img_id = unxId.toLong()

                println("ssssssssssssssssssssssssssssssssssssss  pro.pro_code : " + pro.pro_code)

                productsImage?.child(id)?.setValue(pro)



                errorAddress.errorMessage = "New Address url successfully"
                errorAddress.errorCode = "New URL"
                offer(errorAddress)
            }


        }



        awaitClose { this.cancel() }
    }


    suspend fun updateSoldOut(pro: Products): Flow<NetworkError> = callbackFlow {

        println("ssssssssssssssssssssssssssssss   0000  " + pro.pro_id.toString())
        println("ssssssssssssssssssssssssssssss   03030303  " + pro.sold_out)

        var errorAddress = NetworkError()
        when {
            (!InternetConnection.checkInternetConnection()) -> {
                offer(appPref.errorNoInternet())
            }

            else -> {

                productsRef?.child(pro.pro_id.toString())?.child("sold_out")
                    ?.setValue(pro.sold_out)

                println("ssssssssssssssssssssssssssssss   111111  " + pro.pro_id.toString())
                println("ssssssssssssssssssssssssssssss   22222  " + pro.sold_out)


                productsRef?.child(pro.pro_id.toString())?.child("sold_out")?.setValue(pro.sold_out)
                    ?.addOnSuccessListener {

                        println("ssssssssssssssssssssssssssssss   4444")
                    }
                    ?.addOnFailureListener {
                        println("sssssssssssssssssssssssssssssss   5555")
                    }


                errorAddress.errorMessage = "successfully"
                errorAddress.errorCode = "New URL"
                offer(errorAddress)
            }


        }



        awaitClose { this.cancel() }
    }


    fun genarateUniqCode(): String {
        val c: Calendar = Calendar.getInstance()
        var numberFromTime =
            c.get(Calendar.MONTH).toString() +
                    c.get(Calendar.DATE).toString() +
                    c.get(Calendar.HOUR).toString() +
                    c.get(Calendar.MINUTE).toString() +
                    c.get(Calendar.SECOND).toString() +
                    c.get(Calendar.MILLISECOND).toString() +
                    ((1..10000).random()).toString()

        return numberFromTime
    }

}