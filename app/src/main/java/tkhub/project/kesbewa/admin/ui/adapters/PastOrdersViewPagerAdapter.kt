package tkhub.project.kesbewa.admin.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import tkhub.project.kesbewa.admin.ui.fragment.CompleteOrdersFragment
import tkhub.project.kesbewa.admin.ui.fragment.RejectOrdersFragment


const val COM_ORDERS_PAGE_INDEX = 0
const val REJECT_ORDERS_PAGE_INDEX = 1


class PastOrdersViewPagerAdapter (fragment: Fragment) : FragmentStateAdapter(fragment) {
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        COM_ORDERS_PAGE_INDEX to { CompleteOrdersFragment() },
        REJECT_ORDERS_PAGE_INDEX to { RejectOrdersFragment() }

    )
    override fun getItemCount() = tabFragmentsCreators.size
    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }

}