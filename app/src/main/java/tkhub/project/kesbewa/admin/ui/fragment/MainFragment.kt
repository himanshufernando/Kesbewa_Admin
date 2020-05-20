package tkhub.project.kesbewa.admin.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment

import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs

/**
 * A simple [Fragment] subclass.
 */
class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var inflaterMain = inflater.inflate(R.layout.fragment_main, container, false)

        if(AppPrefs.getUserPrefs(context!!,AppPrefs.KEY_USER).admin_id.isNullOrEmpty()){
            NavHostFragment.findNavController(this).navigate(R.id.fragmentMainToLogin)
        }else{
            NavHostFragment.findNavController(this).navigate(R.id.fragmentMainToHome)
        }


        return inflaterMain
    }

}
