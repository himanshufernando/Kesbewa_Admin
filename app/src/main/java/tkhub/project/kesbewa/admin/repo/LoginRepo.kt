package tkhub.project.kesbewa.admin.repo

import android.content.Context
import com.google.firebase.database.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import tkhub.project.kesbewa.admin.data.models.NetworkError
import tkhub.project.kesbewa.admin.data.models.User
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.services.network.InternetConnection

class LoginRepo (context: Context) {

    var mContext = context

    var database: FirebaseDatabase? = FirebaseDatabase.getInstance()
    var userRef: DatabaseReference? = database?.getReference("Admin")

    var appPref = AppPrefs
    var databaseEmptyRef = database?.reference


    suspend fun loginUser(user: User): Flow<NetworkError> = callbackFlow {
        userRef?.keepSynced(false)
        var errorLogin = NetworkError()

        when {
            (AppPrefs.checkValidString(user.admin_number)) -> {
                errorLogin.errorMessage = "Enter your phone number !"
                errorLogin.errorCode = appPref.ERROR_PHONE_NUMBER_EMPTY
                offer(errorLogin)
            }
            (AppPrefs.checkValidString(user.admin_password)) -> {
                errorLogin.errorMessage = "Enter your password !"
                errorLogin.errorCode = appPref.ERROR_PASSWORD_EMPTY
                offer(errorLogin)
            }
            (!InternetConnection.checkInternetConnection()) -> {
                offer(appPref.errorNoInternet())
            }
            else -> {
                var pushid = AppPrefs.getStringKeyValuePrefs(mContext, AppPrefs.KEY_PUSH_TOKEN)

                println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa pushid    :   "+pushid)

                val query: Query =
                    userRef?.orderByChild("admin_password")!!.equalTo((user.admin_number + user.admin_password))
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.childrenCount == 0L) {
                            errorLogin.errorMessage = "Incorrect login, please check your phone number or password !"
                            errorLogin.errorCode = appPref.ERROR_INCORRECT_LOGIN
                            offer(errorLogin)
                        } else {
                            lateinit var logUser: User
                            for (postSnapshot in dataSnapshot.children) {
                                logUser = postSnapshot.getValue(User::class.java)!!
                                AppPrefs.setUserPrefs(mContext, AppPrefs.KEY_USER, logUser)
                            }

                            if (logUser.admin_name.isNullOrEmpty()) {
                                offer(appPref.errorSomethingWentWrong())
                                return
                            } else {

                                databaseEmptyRef!!.child("Admin").child(logUser.admin_id!!).child("admin_push").setValue(pushid)

                                errorLogin.errorMessage = "You are successfully logged in !"
                                errorLogin.errorCode = appPref.SUCCESS_LOGGING
                                offer(errorLogin)
                                return
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        offer(appPref.errorSomethingWentWrong())
                        offer(errorLogin)
                    }
                })

            }

        }

        awaitClose { this.cancel() }
    }


}