package tkhub.project.kesbewa.admin.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tkhub.project.kesbewa.admin.data.models.CartItem
import tkhub.project.kesbewa.admin.databinding.ListviewOrderItemBinding


class CustomerPastOrderItemsAdapter : ListAdapter<CartItem,RecyclerView.ViewHolder>(CustomerPastOrderItemDiffCallback()) {
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val ordersitem = getItem(position)
        (holder as MyOrdersCurrentItemViewHolder).bind(ordersitem)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOrdersCurrentItemViewHolder{
        return MyOrdersCurrentItemViewHolder(ListviewOrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    class MyOrdersCurrentItemViewHolder(private val binding: ListviewOrderItemBinding ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cartItem: CartItem) {
            binding.apply { orderitem = cartItem
                executePendingBindings()
            }
        }
    }
}
private class CustomerPastOrderItemDiffCallback : DiffUtil.ItemCallback<CartItem>() {
    override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem.cart_id == newItem.cart_id
    }
    override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem == newItem
    }
}