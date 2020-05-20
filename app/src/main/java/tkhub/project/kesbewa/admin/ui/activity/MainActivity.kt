package tkhub.project.kesbewa.admin.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.nav_header_main.view.*
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs

class MainActivity : FragmentActivity() ,NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navController: NavController
    lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)

        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)

        navView.itemIconTintList = null;
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_main_drawer, menu)
        return true
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

        }
        return true
    }
}
