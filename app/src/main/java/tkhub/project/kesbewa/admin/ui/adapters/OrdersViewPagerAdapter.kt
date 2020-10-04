package tkhub.project.kesbewa.admin.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import tkhub.project.kesbewa.admin.ui.fragment.*


const val NEW_ORDERS_PAGE_INDEX = 0
const val CONFIRMED_ORDERS_PAGE_INDEX = 1
const val PACKED_ORDERS_PAGE_INDEX = 2
const val DELIVERY_ORDERS_PAGE_INDEX = 3
const val DELIVERED_ORDERS_PAGE_INDEX = 4
const val STORE_ORDERS_PAGE_INDEX = 5

class OrdersViewPagerAdapter (fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        NEW_ORDERS_PAGE_INDEX to { NewOrdersFragment() },
        CONFIRMED_ORDERS_PAGE_INDEX to { ConfirmedOrdersFragment() },
        PACKED_ORDERS_PAGE_INDEX to { PackedOrdersFragment() },
        DELIVERY_ORDERS_PAGE_INDEX to { DeliveryOrderFragment() },
        DELIVERED_ORDERS_PAGE_INDEX to { DeliveredOrdersFragment() },
        STORE_ORDERS_PAGE_INDEX to { StoreFragment() }
    )
    override fun getItemCount() = tabFragmentsCreators.size
    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }

}