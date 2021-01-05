package tkhub.project.kesbewa.admin.repo

import android.content.Context
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import tkhub.project.kesbewa.admin.data.models.*
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class ProductRepo(context: Context) {


    var mContext = context

    var database: FirebaseDatabase? = FirebaseDatabase.getInstance()
    var productsRef: DatabaseReference? = database?.getReference("Products")
    var productsImage: DatabaseReference? = database?.getReference("ProductImages")


    suspend fun getProduct(): Flow<ArrayList<Products>?> = callbackFlow {
        productsRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var list = ArrayList<Products>()
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(Products::class.java)
                    list.add(post!!)
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
                var id = pro.pro_code+"_"+genarateUniqCode()
                pro.img_id = unxId.toLong()

                println("ssssssssssssssssssssssssssssssssssssss  pro.pro_code : "+pro.pro_code )

                productsImage?.child(id)?.setValue(pro)



                errorAddress.errorMessage = "New Address url successfully"
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