package tkhub.project.kesbewa.admin.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tkhub.project.kesbewa.admin.data.models.CartItem
import tkhub.project.kesbewa.admin.databinding.ListviewCartitemsBinding


class CartItemAdapter : ListAdapter<CartItem, RecyclerView.ViewHolder>(CartItemDiffCallback()) {
    lateinit var mClickListener: ClickListener
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val cartitem = getItem(position)
        (holder as CartItemViewHolder).bind(cartitem)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CartItemViewHolder(ListviewCartitemsBinding.inflate(LayoutInflater.from(parent.context), parent, false),mClickListener)
    }
    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }
    interface ClickListener {
        fun onClick(cartitemsellect: CartItem, aView: View,position: Int)
    }
    class CartItemViewHolder(private val binding: ListviewCartitemsBinding ,var mClickListener: ClickListener ) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.setClickListener { binding.cartitem?.let { selectedProduct -> mClickListener.onClick(selectedProduct,it,adapterPosition) } }
        }
        fun bind(cart: CartItem) {
            binding.apply { cartitem = cart
                executePendingBindings()
            }

        }
    }




}

private class CartItemDiffCallback : DiffUtil.ItemCallback<CartItem>() {
    override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem.pro_id == newItem.pro_id
    }
    override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem == newItem
    }
}