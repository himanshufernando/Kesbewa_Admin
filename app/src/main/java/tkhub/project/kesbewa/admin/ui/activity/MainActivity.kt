package tkhub.project.kesbewa.admin.ui.activity

import android.Manifest
import android.Manifest.permission.CALL_PHONE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.installations.FirebaseInstallations
import kotlinx.android.synthetic.main.nav_header_main.view.*
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs

class MainActivity : FragmentActivity() ,NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navController: NavController
    lateinit var navView: NavigationView

    companion object {
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 35
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        setContentView(R.layout.activity_main)

        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa FirebaseInstallations token    :   "+ task.result)

            } else {
                println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa FirebaseInstallations token   fail  :   ")
            }
        }




        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)

        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)



        if (!checkPermissions()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions()
            }
        } else {

        }


        navView.itemIconTintList = null;
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main_drawer, menu)
        return true
    }

    private fun checkPermissions() = ContextCompat.checkSelfPermission(this, CALL_PHONE) == PERMISSION_GRANTED

    private fun checkPermissionsStorage() = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermissions() {
        requestPermissions(
            arrayOf(CALL_PHONE,WRITE_EXTERNAL_STORAGE),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSIONS_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PermissionChecker.PERMISSION_GRANTED)) {

                } else {
                    Toast.makeText(this, "Oops! Permission Denied!!", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }



    fun setDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        var user = AppPrefs.getUserPrefs(this, AppPrefs.KEY_USER)
        val headerLayout: View = navView.getHeaderView(0)
        headerLayout.textview_name.text = user.admin_name
    }

    fun removeDrawer() {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
    fun openDrawer(){
        drawerLayout.openDrawer(GravityCompat.START)
    }
    fun closeDrawer(){
        drawerLayout.closeDrawer(GravityCompat.START)
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        closeDrawer()
        var fragmetID = AppPrefs.getIntKeyValuePrefs(this, AppPrefs.KEY_FRAGMENT_ID)
        when (item.itemId) {
            R.id.nav_home ->{
                if(fragmetID!=2){
                    navController?.navigate(R.id.fragmentPastToHome)}
                }

            R.id.nav_past ->{
                if(fragmetID!=3){
                    navController?.navigate(R.id.fragmentMainToPast)}
            }

            R.id.nav_search ->{
                if(fragmetID!=4){
                    navController?.navigate(R.id.fragment_search_order)}
            }

            R.id.nav_products ->{
                if(fragmetID!=5){
                    navController?.navigate(R.id.fragment_products)}
            }

        }
        return true
    }
}
