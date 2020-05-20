package tkhub.project.kesbewa.admin.repo

import android.content.Context
import com.google.firebase.database.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import tkhub.project.kesbewa.admin.data.models.NetworkError
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.data.models.OrderStatus
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import java.text.SimpleDateFormat
import java.util.*

class homeRepo(context: Context) {

    var mContext = context

    var database: FirebaseDatabase? = FirebaseDatabase.getInstance()
    var orderRef: DatabaseReference? = database?.getReference("KOrders")
    var orderStatusRef: DatabaseReference? = database?.getReference("Orders_Status")

    var appPref = AppPrefs
    var databaseEmptyRef = database?.reference

    var lastUpdateOrderId = ""
    var lastUpdateOrderStatus = -1


    suspend fun getNewOrders(): Flow<ArrayList<OrderRespons>?> = callbackFlow {
        val query: Query = orderRef?.orderByChild("order_status")!!.equalTo("1")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var list = ArrayList<OrderRespons>()
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(OrderRespons::class.java)
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


    suspend fun getConfirmedOrders(): Flow<ArrayList<OrderRespons>?> = callbackFlow {
        val query: Query = orderRef?.orderByChild("order_status")!!.equalTo("2")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var list = ArrayList<OrderRespons>()
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(OrderRespons::class.java)
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


    suspend fun getPackedOrders(): Flow<ArrayList<OrderRespons>?> = callbackFlow {
        val query: Query = orderRef?.orderByChild("order_status")!!.equalTo("3")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var list = ArrayList<OrderRespons>()
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(OrderRespons::class.java)
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


    suspend fun getDeliveryOrders(): Flow<ArrayList<OrderRespons>?> = callbackFlow {
        val query: Query = orderRef?.orderByChild("order_status")!!.equalTo("4")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var list = ArrayList<OrderRespons>()
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(OrderRespons::class.java)
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

    suspend fun getDeliveredOrders(): Flow<ArrayList<OrderRespons>?> = callbackFlow {
        val query: Query = orderRef?.orderByChild("order_status")!!.equalTo("5")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var list = ArrayList<OrderRespons>()
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(OrderRespons::class.java)
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


    suspend fun getComplteOrders(): Flow<ArrayList<OrderRespons>?> = callbackFlow {
        val query: Query = orderRef?.orderByChild("order_status")!!.equalTo("6")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var list = ArrayList<OrderRespons>()
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(OrderRespons::class.java)
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


    suspend fun getRejectOrders(): Flow<ArrayList<OrderRespons>?> = callbackFlow {
        val query: Query = orderRef?.orderByChild("order_status")!!.equalTo("0")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var list = ArrayList<OrderRespons>()
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(OrderRespons::class.java)
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


    suspend fun getUserPastOrders(userid: String): Flow<ArrayList<OrderRespons>?> = callbackFlow {
        val query: Query = orderRef?.orderByChild("order_user_id")!!.equalTo(userid)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var list = ArrayList<OrderRespons>()
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(OrderRespons::class.java)
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


    suspend fun updateOrdersStatus(orderRespons: OrderRespons): Flow<NetworkError?> =
        callbackFlow {


            orderRef?.child(orderRespons.order_id)?.child("order_status")
                ?.setValue(orderRespons.order_status)
            orderRef?.child(orderRespons.order_id)?.child("order_status_code")
                ?.setValue(orderRespons.order_status_code)
            orderRef?.child(orderRespons.order_id)?.child("order_status_note")
                ?.setValue(orderRespons.order_status_note)

            var orderUpdateStatus = OrderStatus()

            var user = AppPrefs.getUserPrefs(mContext!!, AppPrefs.KEY_USER)
            var unxId = appPref.genarateUniqCode()

            orderUpdateStatus.order_status_id = unxId.toString()
            orderUpdateStatus.order_status_order_id = orderRespons.order_id
            orderUpdateStatus.order_status_order_code = orderRespons.order_code
            orderUpdateStatus.order_status_note = orderRespons.order_status_note
            orderUpdateStatus.order_status = orderRespons.order_status
            orderUpdateStatus.order_status_code = orderRespons.order_status_code
            orderUpdateStatus.order_status_user = user
            orderUpdateStatus.order_status_date =
                SimpleDateFormat("EEEE, dd MMM yyyy").format(Calendar.getInstance().time)
            orderUpdateStatus.order_status_time =
                SimpleDateFormat("HH:mm").format(Calendar.getInstance().time)

            orderStatusRef?.child(unxId.toString())?.setValue(orderUpdateStatus)

            var networkError = NetworkError()

            networkError.errorMessage = "Order update successfully !"
            networkError.errorCode = appPref.SUCCESS_ORDER_UPDATED


            offer(networkError)

            awaitClose { this.cancel() }

        }


}