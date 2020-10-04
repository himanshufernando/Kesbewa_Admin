package tkhub.project.kesbewa.admin.ui.fragment

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer

import androidx.navigation.fragment.NavHostFragment

import kotlinx.android.synthetic.main.fragment_delivery_orders.view.*
import kotlinx.android.synthetic.main.fragment_new_orders.view.*
import kotlinx.android.synthetic.main.fragment_products.view.*
import kotlinx.android.synthetic.main.fragment_search_orders.view.*

import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.NetworkError
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.data.models.Products
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.services.network.InternetConnection
import tkhub.project.kesbewa.admin.ui.activity.MainActivity
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
    var alertDialog: AlertDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root =  inflater.inflate(R.layout.fragment_products, container, false)

        AppPrefs.setIntKeyValuePrefs(context!!, AppPrefs.KEY_FRAGMENT_ID,5)

        root.recyclerView_products.adapter = adapter


        root.imageview_navigation_product.setOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        adapter.setOnItemClickListener(object : ProductsAdapter.ClickListener {
            override fun onClick(product: Products, aView: View) {
                val bundle = bundleOf("Product" to product)
                NavHostFragment.findNavController(this@ProductsFragment).navigate(R.id.fragment_product_image,bundle)
            }
        })






        return root
    }

    override fun onResume() {
        super.onResume()

        if (!InternetConnection.checkInternetConnection()) {
            errorAlertDialog(AppPrefs.errorNoInternet())
        }
        if (!viewmodel.productsList.hasObservers()) {
            productsListObserver()
        }
        viewmodel.getProducts()
    }

    override fun onStop() {
        viewmodel.productsList.removeObservers(viewLifecycleOwner)
        super.onStop()
    }

    fun productsListObserver() {
        viewmodel.productsList.observe(viewLifecycleOwner, Observer {response ->
            when (response) {
                is KesbewaResult.Success -> {
                    var list =response.data
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
                      errorAlertDialog(response.exception)
                }
            }
        })

    }

    private fun errorAlertDialog(networkError: NetworkError) {
        if (alertDialog != null) {
            if (alertDialog!!.isShowing) {
                alertDialog!!.dismiss()
            }
        }
        alertDialog = activity?.let {
            val builder = androidx.appcompat.app.AlertDialog.Builder(it)
            builder.setTitle(networkError.errorTitle)
            builder.setMessage(networkError.errorMessage)
            builder.setCancelable(false)
            builder.apply {
                setPositiveButton(
                    R.string.ok,
                    DialogInterface.OnClickListener { _, _ -> return@OnClickListener })
            }
            builder.create()
            builder.show()

        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
