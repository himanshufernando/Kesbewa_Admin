package tkhub.project.kesbewa.admin.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import tkhub.project.kesbewa.admin.data.models.ProductsModel


import tkhub.project.kesbewa.admin.databinding.ListviewProductsBinding


class ProductsAdapter : ListAdapter<ProductsModel, RecyclerView.ViewHolder>(ProductsDiffCallback()) {

    lateinit var mClickListener: ClickListener

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val orderItem = getItem(position)
        (holder as OrdersItemViewHolder).bind(orderItem)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrdersItemViewHolder(ListviewProductsBinding.inflate(LayoutInflater.from(parent.context), parent, false),mClickListener)
    }
    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }
    interface ClickListener {
        fun onClick(product: ProductsModel, aView: View,adapterPosition: Int)
    }
    class OrdersItemViewHolder(private val binding: ListviewProductsBinding ,var mClickListener: ClickListener ) :

        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.setClickListener { binding.item?.let { selectedProduct ->
                mClickListener.onClick(selectedProduct,it,adapterPosition)
            } }
        }
        fun bind(pro: ProductsModel) {
            binding.apply { item = pro
                executePendingBindings()
            }

        }
    }

}

private class ProductsDiffCallback : DiffUtil.ItemCallback<ProductsModel>() {
    override fun areItemsTheSame(oldItem: ProductsModel, newItem: ProductsModel): Boolean {
        return oldItem.pro_id == newItem.pro_id
    }
    override fun areContentsTheSame(oldItem: ProductsModel, newItem: ProductsModel): Boolean {
        return oldItem == newItem
    }
}