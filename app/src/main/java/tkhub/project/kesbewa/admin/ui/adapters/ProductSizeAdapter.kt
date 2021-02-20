package tkhub.project.kesbewa.admin.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tkhub.project.kesbewa.admin.data.models.ProductSize
import tkhub.project.kesbewa.admin.databinding.ListviewProductsIndetailsBinding

class ProductSizeAdapter : ListAdapter<ProductSize, RecyclerView.ViewHolder>(ProductSizeItemDiffCallback()) {


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val ordersitem = getItem(position)
        (holder as OrdersItemViewHolder).bind(ordersitem)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersItemViewHolder{
        return OrdersItemViewHolder(ListviewProductsIndetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    class OrdersItemViewHolder(private val binding: ListviewProductsIndetailsBinding ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(cartItem: ProductSize) {
            binding.apply { item = cartItem
                executePendingBindings()
            }

        }
    }
}

private class ProductSizeItemDiffCallback : DiffUtil.ItemCallback<ProductSize>() {
    override fun areItemsTheSame(oldItem: ProductSize, newItem: ProductSize): Boolean {
        return oldItem.sizeID == newItem.sizeID
    }
    override fun areContentsTheSame(oldItem: ProductSize, newItem: ProductSize): Boolean {
        return oldItem == newItem
    }
}