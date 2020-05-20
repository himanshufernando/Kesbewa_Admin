package tkhub.project.kesbewa.admin.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*

import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.databinding.FragmentHomeBinding
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.ui.activity.MainActivity
import tkhub.project.kesbewa.admin.ui.adapters.*
import tkhub.project.kesbewa.admin.viewmodels.home.HomeViewModels

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
      var  binding = inflater.inflate(R.layout.fragment_home, container, false)

        AppPrefs.setIntKeyValuePrefs(context!!, AppPrefs.KEY_FRAGMENT_ID,2)
        (activity as MainActivity).setDrawer()



        binding.view_pager.adapter = OrdersViewPagerAdapter(this)

        TabLayoutMediator( binding.tabs,binding.view_pager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()


        binding.imageview_navigation.setOnClickListener {
            (activity as MainActivity).openDrawer()
        }


        return   binding
    }


    private fun getTabTitle(position: Int): String? {
        return when (position) {
            NEW_ORDERS_PAGE_INDEX -> getString(R.string.viewpager_new_orders)
            CONFIRMED_ORDERS_PAGE_INDEX -> getString(R.string.viewpager_new_confirmed_orders)
            PACKED_ORDERS_PAGE_INDEX -> getString(R.string.viewpager_packed_orders)
            DELIVERY_ORDERS_PAGE_INDEX -> getString(R.string.viewpager_delivery_orders)
            DELIVERED_ORDERS_PAGE_INDEX -> getString(R.string.viewpager_delivered_orders)
            else -> null
        }
    }

}
