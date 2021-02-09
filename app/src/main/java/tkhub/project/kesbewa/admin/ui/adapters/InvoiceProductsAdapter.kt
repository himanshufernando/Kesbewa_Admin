package tkhub.project.kesbewa.admin.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import tkhub.project.kesbewa.admin.data.models.Products
import tkhub.project.kesbewa.admin.databinding.ListviewInvoiceProductsBinding
import tkhub.project.kesbewa.admin.databinding.ListviewPackedOrdersBinding
import tkhub.project.kesbewa.admin.databinding.ListviewProductsBinding


class InvoiceProductsAdapter : ListAdapter<Products, RecyclerView.ViewHolder>(InvoiceProductsDiffCallback()) {

    lateinit var mClickListener: ClickListener

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val orderItem = getItem(position)
        (holder as OrdersItemViewHolder).bind(orderItem)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrdersItemViewHolder(ListviewInvoiceProductsBinding.inflate(LayoutInflater.from(parent.context), parent, false),mClickListener)
    }
    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }
    interface ClickListener {
        fun onClick(product: Products, aView: View)
    }
    class OrdersItemViewHolder(private val binding: ListviewInvoiceProductsBinding ,var mClickListener: ClickListener ) :

        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.setClickListener { binding.item?.let { selectedProduct ->
                mClickListener.onClick(selectedProduct,it)
            } }
        }
        fun bind(pro: Products) {
            binding.apply { item = pro
                executePendingBindings()
            }

        }
    }

}

private class InvoiceProductsDiffCallback : DiffUtil.ItemCallback<Products>() {
    override fun areItemsTheSame(oldItem: Products, newItem: Products): Boolean {
        return oldItem.pro_id == newItem.pro_id
    }
    override fun areContentsTheSame(oldItem: Products, newItem: Products): Boolean {
        return oldItem == newItem
    }
}