package tkhub.project.kesbewa.admin.ui.fragment

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import coil.ImageLoader
import coil.request.LoadRequest
import coil.size.Scale
import com.google.gson.Gson
import id.ionbit.ionalert.IonAlert
import kotlinx.android.synthetic.main.dialog_customer_details.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_new_orders.view.*
import kotlinx.android.synthetic.main.fragment_search_orders.view.*

import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.NetworkError
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.databinding.FragmentSearchOrdersBinding
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.services.network.InternetConnection
import tkhub.project.kesbewa.admin.ui.activity.MainActivity
import tkhub.project.kesbewa.admin.ui.adapters.CustomerPastOrdersAdapter
import tkhub.project.kesbewa.admin.ui.adapters.NewOrdersAdapter
import tkhub.project.kesbewa.admin.ui.adapters.SearchOrdersAdapter
import tkhub.project.kesbewa.admin.viewmodels.past.PastViewModels

/**
 * A simple [Fragment] subclass.
 */
class SearchOrdersFragment : Fragment() {

    private val viewmodel: PastViewModels by viewModels { PastViewModels.LiveDataVMFactory }
    lateinit var binding: FragmentSearchOrdersBinding
    lateinit var root: View
    var alertDialog: AlertDialog? = null
    private val adapter = SearchOrdersAdapter()
    private val adapterCustomerPast = CustomerPastOrdersAdapter()
    lateinit var dialogCustomer: Dialog
    lateinit var imageLoader: ImageLoader

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_orders, container, false)
        binding.serachOrders = viewmodel

        AppPrefs.setIntKeyValuePrefs(context!!, AppPrefs.KEY_FRAGMENT_ID,4)

        binding.root.recyclerView_orrders.adapter = adapter




        binding.root.imageview_navigation_search.setOnClickListener {
            (activity as MainActivity).openDrawer()
        }



        adapter.setOnItemClickListener(object : SearchOrdersAdapter.ClickListener {
            override fun onClick(orderRespons: OrderRespons, aView: View) {
                when (aView.id) {
                    R.id.imageview_customer_details -> {
                        if (::dialogCustomer.isInitialized) {
                            if (dialogCustomer.isShowing) {
                                dialogCustomer.dismiss()
                            }
                        }
                        dialogCustomerDetails(orderRespons)
                    }

                    R.id.imageview_address_details -> {
                        val bundle = bundleOf("deliveryAddress" to  Gson().toJson(orderRespons.delivery_address))
                        view?.findNavController()?.navigate(R.id.fragmentSearchToMap,bundle)
                    }


                }

            }
        })






        return binding.root
    }

    override fun onStop() {
        if (::imageLoader.isInitialized) {
            imageLoader.shutdown()
        }

        viewmodel.filterdOrders.removeObservers(viewLifecycleOwner)
        viewmodel.ordersByCode.removeObservers(viewLifecycleOwner)
        viewmodel.pastOrders.removeObservers(viewLifecycleOwner)

        super.onStop()
    }
    override fun onResume() {
        super.onResume()

        if(!InternetConnection.checkInternetConnection()){
            errorAlertDialog(AppPrefs.errorNoInternet())
        }
        if (!viewmodel.ordersByCode.hasObservers()) {
            ordersByCodeObserver()
        }

    }
    override fun onPause() {
        super.onPause()
        viewmodel.filterdOrders.removeObservers(viewLifecycleOwner)
        viewmodel.ordersByCode.removeObservers(viewLifecycleOwner)
        viewmodel.pastOrders.removeObservers(viewLifecycleOwner)

    }



    fun filterdOrdersObserver(){


        viewmodel.filterdOrders.observe(viewLifecycleOwner, Observer {response ->
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




    fun ordersByCodeObserver(){

        viewmodel.ordersByCode.observe(viewLifecycleOwner, Observer {response ->
            when (response) {
                is KesbewaResult.Success -> {
                    viewmodel.orderList.value = response.data

                    if (!viewmodel.filterdOrders.hasObservers()) {
                        filterdOrdersObserver()
                    }

                    viewmodel.filterList()
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





    fun pastOrdersObserver(){


        viewmodel.pastOrders.observe(viewLifecycleOwner, Observer {response ->
            when (response) {
                is KesbewaResult.Success -> {
                    var list =response.data
                    adapterCustomerPast.submitList(list.reversed())
                }
                is KesbewaResult.ExceptionError.ExError -> {
                    Toast.makeText(
                        activity,
                        response.exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is KesbewaResult.LogicError.LogError -> {
                    Toast.makeText(
                        activity,
                        response.exception.errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
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


    private fun dialogCustomerDetails(orderRespons: OrderRespons) {

        if (!viewmodel. pastOrders.hasObservers()) {
            pastOrdersObserver()
        }
        viewmodel.getUserPastOrders(orderRespons.user.user_id)


        dialogCustomer = Dialog(requireContext())
        dialogCustomer.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogCustomer.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogCustomer.setContentView(R.layout.dialog_customer_details)
        dialogCustomer.setCancelable(true)
        dialogCustomer.appCompatTextView2.text = orderRespons.user.user_name
        dialogCustomer.appCompatTextView4.text = orderRespons.user.user_phone
        dialogCustomer.appCompatTextView6.text = orderRespons.user.user_email
        dialogCustomer.appCompatTextView8.text = orderRespons.user.user_nic
        dialogCustomer.appCompatTextView10.text = orderRespons.user.user_code
        dialogCustomer.recyclerView_customer_past_orders.adapter = adapterCustomerPast


        dialogCustomer.img_call.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    context as Activity,
                    Manifest.permission.CALL_PHONE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val dial = "tel:${orderRespons.user.user_phone}"
                startActivity(Intent(Intent.ACTION_CALL, Uri.parse(dial)))
            } else {
                Toast.makeText(
                    requireContext(),
                    "Permission Call Phone denied",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        imageLoader = ImageLoader.Builder(requireContext())
            .placeholder(R.drawable.ic_profile_users)
            .error(R.drawable.ic_profile_users)
            .build()
        val request = LoadRequest.Builder(requireContext())
            .data(orderRespons.user.user_pro_pic)
            .target(
                onStart = { placeholder ->
                    dialogCustomer.imageview_customer.setImageDrawable(placeholder)
                },
                onSuccess = { result ->
                    dialogCustomer.imageview_customer.setImageDrawable(result)
                },
                onError = { error ->
                    dialogCustomer.imageview_customer.setImageDrawable(error)
                }
            )
            .scale(Scale.FILL)
            .build()
        imageLoader.execute(request)
        dialogCustomer.show()
    }


}
