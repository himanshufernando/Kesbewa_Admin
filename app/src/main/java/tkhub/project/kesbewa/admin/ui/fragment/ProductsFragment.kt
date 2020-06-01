package tkhub.project.kesbewa.admin.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.NavHostFragment

import kotlinx.android.synthetic.main.fragment_delivery_orders.view.*
import kotlinx.android.synthetic.main.fragment_new_orders.view.*
import kotlinx.android.synthetic.main.fragment_products.view.*

import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.data.models.Products
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.ui.adapters.DeliveryOrdersAdapter
import tkhub.project.kesbewa.admin.ui.adapters.NewOrdersAdapter
import tkhub.project.kesbewa.admin.ui.adapters.ProductsAdapter
import tkhub.project.kesbewa.admin.viewmodels.products.ProductsViewModels

/**
 * A simple [Fragment] subclass.
 */
class ProductsFragment : Fragment() {
    private val viewmodel: ProductsViewModels by viewModels { ProductsViewModels.LiveDataVMFactory }
    lateinit var root: View
    private val adapter = ProductsAdapter()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root =  inflater.inflate(R.layout.fragment_products, container, false)

        AppPrefs.setIntKeyValuePrefs(context!!, AppPrefs.KEY_FRAGMENT_ID,5)

        root.recyclerView_products.adapter = adapter

        adapter.setOnItemClickListener(object : ProductsAdapter.ClickListener {
            override fun onClick(product: Products, aView: View) {
                val bundle = bundleOf("Product" to product)
                NavHostFragment.findNavController(this@ProductsFragment).navigate(R.id.fragmentProductToEdit,bundle)

            }
        })


        viewmodel.productsList.observe(viewLifecycleOwner) { response ->
            when (response) {
                is KesbewaResult.Success -> {
                   var list =response.data
                    println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa dcd: "+list)
                    adapter.submitList(list.reversed())
                }
                is KesbewaResult.ExceptionError.ExError -> {
                    Toast.makeText(
                        activity,
                        response.exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is KesbewaResult.LogicError.LogError -> {
                 //   errorAlertDialog(response.exception)
                }
            }
        }




        return root
    }

}
