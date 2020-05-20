package tkhub.project.kesbewa.admin.services.Perfrences

import android.app.Activity
import android.content.Context
import android.provider.Settings
import com.google.gson.Gson
import tkhub.project.kesbewa.admin.KesbewaAdmin
import tkhub.project.kesbewa.admin.data.models.DeliveryAddress
import tkhub.project.kesbewa.admin.data.models.NetworkError
import tkhub.project.kesbewa.admin.data.models.User

import java.util.*
import java.util.regex.Pattern

object AppPrefs {

    const val PREFNAME = "kesbewapref"

    const val KEY_SPLASH_STATUS = "splash_status"
  //  const val KEY_USER_ID = "user_id"
    const val KEY_ORDER = "order_details"
    const val KEY_ADDTOCARTACTIONSTATUS = "add_to_cat_status"
    const val KEY_FRAGMENT_ID = "fragment_id"
    const val KEY_PUSH_TOKEN = "push_token"
    const val KEY_CURRENT_LOCATION = "current_location"
    const val KEY_MANUAL_LOCATION = "manual_location"
    //const val KEY_GUEST_USER_ID = "guest_user_id"
    const val KEY_SELECTED_USER_ADDERSS = "selected_user_address"
    const val KEY_SELECTED_PROMO = "selected_promo"
    const val KEY_USER = "user"
    const val KEY_GUEST_USER = "guest_user"



    //Error Code
    const val ERROR_USER_NAME_EMPTY = "UE"
    const val ERROR_PHONE_NUMBER_EMPTY = "PE"
    const val ERROR_EMAIL_EMPTY = "EE"
    const val ERROR_NIC_EMPTY = "NICE"
    const val ERROR_PASSWORD_EMPTY = "PWE"
    const val ERROR_PASSWORD_INVALID = "PWI"
    const val ERROR_PASSWORD_NOT_MATCH = "PWNM"
    const val ERROR_INTERNET = "IC"
    const val ERROR_SOMETHING_WENT_WRONG = "SW"
    const val ERROR_ACCOUNT_ALREADY_EXISTS = "AAE"

    const val ERROR_INCORRECT_LOGIN = "LOGGEDFAIL"
    const val ERROR_EMPTY_CART = "EC"
    const val ERROR_EMPTY_ADDRESS = "EA"
    const val ERROR_ORDER_NOT_SUCCESSFULLY = "OE"

    const val ERROR_EMPTY_ADDRESS_NUMBER = "EAN"
    const val ERROR_EMPTY_LOCATION = "LOC"
    const val ERROR_EMPTY_STREET_ADDRESS = "ESA"
    const val ERROR_EMPTY_CITY = "ECITY"
    const val ERROR_EMPTY_DISTRICT = "ED"
    const val ERROR_EMPTY_ZIP_CODE = "ZC"
    const val ERROR_INVALID_ZIP_CODE= "ZC"
    const val ERROR_EMPTY_CITYLIST = "ECITYLIST"




    //success code
    const val SUCCESS_USER_SAVE = "USERSAVESUCCESS"
    const val SUCCESS_LOGGING = "LOGGED"
    const val SUCCESS_ORDER_SUCCESSFULLY = "OS"
    const val SUCCESS_ADDRESS_SAVE_SUCCESSFULLY = "ADDRESSSAVESUCCESS"
    const val SUCCESS_ORDER_UPDATED = "ORDERUPDATEDSUCCESS"


    fun setStringKeyValuePrefs(ctx: Context, key: String, value: String) {
        val sharedPref = ctx.getSharedPreferences(PREFNAME,Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(key, value)
            commit()
        }
    }

    fun getStringKeyValuePrefs(ctx: Context, key: String): String {
        val sharedPref = ctx.getSharedPreferences(PREFNAME,Context.MODE_PRIVATE)
        return sharedPref.getString(key,"0").toString()
    }

    fun setUserPrefs(ctx: Context, key: String, value: User) {
        val sharedPref = ctx.getSharedPreferences(PREFNAME,Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString(key, Gson().toJson(value))
            commit()
        }
    }

    fun getUserPrefs(ctx: Context, key: String): User {
        val sharedPref = ctx.getSharedPreferences(PREFNAME,Context.MODE_PRIVATE)
        var defUser = User()

        if(sharedPref.getString(key, null)==null){
            return defUser
        }else{
            return Gson().fromJson(sharedPref.getString(key, null), User::class.java)
        }

    }


    fun genarateUniqCode(): Long {
        val c: Calendar = Calendar.getInstance()
        var numberFromTime =
            c.get(Calendar.MONTH).toString() +
                    c.get(Calendar.DATE).toString() +
                    c.get(Calendar.YEAR).toString() +
                    c.get(Calendar.HOUR).toString() +
                    c.get(Calendar.MINUTE).toString() +
                    c.get(Calendar.SECOND).toString() +
                    c.get(Calendar.MILLISECOND).toString() + ((1..1000).random()).toString()

        return numberFromTime.toLong()
    }



    fun checkValidString(st: String): Boolean {
        if ((st.isNullOrEmpty()) || (st== "null")) {
            return true
        }
        return false
    }

    fun setIntKeyValuePrefs(ctx: Context, key: String, value: Int) {
        val sharedPref = ctx.getSharedPreferences(PREFNAME,Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putInt(key, value)
            commit()
        }
    }

    fun getIntKeyValuePrefs(ctx: Context, key: String): Int {
        val sharedPref = ctx.getSharedPreferences(PREFNAME,Context.MODE_PRIVATE)
        return sharedPref.getInt(key, 0)
    }

    fun getAndroidid():String{
        return Settings.Secure.getString(KesbewaAdmin.applicationContext().contentResolver, Settings.Secure.ANDROID_ID)
    }

    fun encrypPassword(key : String):String{
        var count  = 1
        var encriptordpassword = ""
        for(element in  key.reversed()){
            val remainder = count % 3
            if((count % 3)==0){
                encriptordpassword += element.toInt()
            }else{
                encriptordpassword += element
            }
            count++
        }
        return encriptordpassword.reversed()
    }


    fun errorSomethingWentWrong() : NetworkError {
        return NetworkError(0,
            ERROR_SOMETHING_WENT_WRONG,"Something went wrong please try again !","Error",false,0)
    }
    fun errorNoInternet() : NetworkError{
        return NetworkError(0,
            ERROR_INTERNET,"No internet access please check your connection and retry !","Internet",false,0)
    }


   /* fun setDeliveryAddressToGson(value: DeliveryAddress) : String{
        return Gson().toJson(value)
    }

    fun getLocationKeyValuePrefs(ctx: Context, key: String): CurrentLocation {
        val sharedPref = ctx.getSharedPreferences(PREFNAME,Context.MODE_PRIVATE)
        return Gson().fromJson(sharedPref.getString(key, null), CurrentLocation::class.java)

    }*/
}