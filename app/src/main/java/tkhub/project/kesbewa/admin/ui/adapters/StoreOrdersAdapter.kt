package tkhub.project.kesbewa.admin.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.databinding.ListviewPackedOrdersBinding
import tkhub.project.kesbewa.admin.databinding.ListviewStoreOrdersBinding


class StoreOrdersAdapter : ListAdapter<OrderRespons, RecyclerView.ViewHolder>(StoreOrdersDiffCallback()) {

    lateinit var mClickListener: ClickListener

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val orderItem = getItem(position)
        (holder as StoreItemViewHolder).bind(orderItem)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return StoreItemViewHolder(ListviewStoreOrdersBinding.inflate(LayoutInflater.from(parent.context), parent, false),mClickListener)
    }
    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }
    interface ClickListener {
        fun onClick(orderRespons: OrderRespons, aView: View)
    }
    class StoreItemViewHolder(private val binding: ListviewStoreOrdersBinding ,var mClickListener: ClickListener ) :

        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.setClickListener { binding.storeordersitem?.let { selectedProduct ->
                mClickListener.onClick(selectedProduct,it)
            } }
        }
        fun bind(orderRespons: OrderRespons) {
            binding.apply { storeordersitem = orderRespons
                executePendingBindings()
            }

        }
    }

}

private class StoreOrdersDiffCallback : DiffUtil.ItemCallback<OrderRespons>() {
    override fun areItemsTheSame(oldItem: OrderRespons, newItem: OrderRespons): Boolean {
        return oldItem.order_code == newItem.order_code
    }
    override fun areContentsTheSame(oldItem: OrderRespons, newItem: OrderRespons): Boolean {
        return oldItem == newItem
    }
}