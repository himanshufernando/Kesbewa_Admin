package tkhub.project.kesbewa.admin.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_past_orders.view.*
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.ui.activity.MainActivity
import tkhub.project.kesbewa.admin.ui.adapters.COM_ORDERS_PAGE_INDEX
import tkhub.project.kesbewa.admin.ui.adapters.PastOrdersViewPagerAdapter
import tkhub.project.kesbewa.admin.ui.adapters.REJECT_ORDERS_PAGE_INDEX
import tkhub.project.kesbewa.admin.viewmodels.past.PastViewModels

/**
 * A simple [Fragment] subclass.
 */
class PastOrdersFragment : Fragment() {


    lateinit var root: View


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root =  inflater.inflate(R.layout.fragment_past_orders, container, false)

        AppPrefs.setIntKeyValuePrefs(context!!, AppPrefs.KEY_FRAGMENT_ID,3)


        root.view_pager_past.adapter = PastOrdersViewPagerAdapter(this)

        TabLayoutMediator( root.tabs_past,root.view_pager_past) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()

        (activity as MainActivity).setDrawer()


        root.imageview_navigation_past.setOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        return root
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            COM_ORDERS_PAGE_INDEX -> getString(R.string.viewpager_complete_orders)
            REJECT_ORDERS_PAGE_INDEX -> getString(R.string.viewpager_reject_orders)
            else -> null
        }
    }

}
