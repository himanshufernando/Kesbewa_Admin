package tkhub.project.kesbewa.admin

import android.content.Context
import androidx.multidex.MultiDexApplication

class KesbewaAdmin : MultiDexApplication() {


    //  var locationSettings: LocationSettings? = null


    companion object {
        private var instance: KesbewaAdmin? = null

        //  private var locationSettings: LocationSettings? = null
        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
        /*public fun setLocationSettingsListener(locationsettings: LocationSettings) {
            locationSettings = locationsettings
        }

        public fun getLocationSettingsListener(): LocationSettings? {
            return locationSettings
        }*/

    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        val context: Context = KesbewaAdmin.applicationContext()
    }
}