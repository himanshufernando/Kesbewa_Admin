package tkhub.project.kesbewa.admin.ui.fragment

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import coil.ImageLoader
import coil.request.ImageRequest

import coil.size.Scale
import com.google.gson.Gson
import id.ionbit.ionalert.IonAlert
import kotlinx.android.synthetic.main.dialog_customer_details.*

import kotlinx.android.synthetic.main.fragment_store.view.*
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.NetworkError
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.services.network.InternetConnection
import tkhub.project.kesbewa.admin.ui.adapters.PackedOrdersAdapter
import tkhub.project.kesbewa.admin.ui.adapters.StoreOrdersAdapter
import tkhub.project.kesbewa.admin.viewmodels.home.HomeViewModels


class StoreFragment : Fragment() {

    private val viewmodel: HomeViewModels by viewModels { HomeViewModels.LiveDataVMFactory }
    lateinit var root: View
    var alertDialog: AlertDialog? = null
    private val adapter = StoreOrdersAdapter()


    lateinit var imageLoader: ImageLoader
    lateinit var dialogCustomer: Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_store, container, false)


        root.recyclerView_store_orders.adapter = adapter


        adapter.setOnItemClickListener(object : StoreOrdersAdapter.ClickListener {
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
                        val bundle =
                            bundleOf("deliveryAddress" to Gson().toJson(orderRespons.delivery_address))
                        view?.findNavController()?.navigate(R.id.fragmentHomeToMap, bundle)
                    }

                    R.id.textview_passtodelivery -> {
                        IonAlert(requireContext(), IonAlert.ERROR_TYPE)
                            .setTitleText("Are you sure?")
                            .setContentText("Are you sure, you want to pass to delivery ?")
                            .setConfirmText("Yes")
                            .setConfirmClickListener(IonAlert.ClickListener { sDialog ->
                                if (!viewmodel.orderUpdateResponse.hasObservers()) {
                                    orderUpdateResponseObserver()
                                }
                                viewmodel.orderStatusUpdate(orderRespons,5,"")

                               
                                sDialog.dismissWithAnimation()

                            })
                            .setCancelText("No")
                            .setCancelClickListener(IonAlert.ClickListener { sDialog ->
                                sDialog.dismissWithAnimation()

                            })
                            .show()
                    }
                }
            }
        })

        root.swiperefresh_store_orders.setOnRefreshListener {
            if (!viewmodel.storeOrdersResponse.hasObservers()) {
                storeOrdersResponseObserver()
            }
            viewmodel.getStoreOrders()
        }


        return root



    }

    override fun onStop() {
        if (::imageLoader.isInitialized) {
            imageLoader.shutdown()
        }
        viewmodel.orderUpdateResponse.removeObservers(viewLifecycleOwner)
        viewmodel.storeOrdersResponse.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
    override fun onResume() {
        super.onResume()
        if(!InternetConnection.checkInternetConnection()){
            errorAlertDialog(AppPrefs.errorNoInternet())
        }


        if (!viewmodel.storeOrdersResponse.hasObservers()) {
            storeOrdersResponseObserver()
        }
        viewmodel.getStoreOrders()
    }

    fun storeOrdersResponseObserver(){

        viewmodel.storeOrdersResponse.observe(viewLifecycleOwner, Observer {response ->
            root.layout_loading_store_orders.visibility = View.GONE
            root.swiperefresh_store_orders.isRefreshing = false
            when (response) {
                is KesbewaResult.Success -> {
                    println("qqqqqqqqqqqqqqqqqqqqqqqqqqq11")


                    var list = response.data
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


    fun orderUpdateResponseObserver(){


        viewmodel.orderUpdateResponse.observe(viewLifecycleOwner, Observer {response ->
            root.layout_loading_store_orders.visibility = View.GONE
            when (response) {
                is KesbewaResult.Success -> {
                    if (!viewmodel.storeOrdersResponse.hasObservers()) {
                        storeOrdersResponseObserver()
                    }
                    viewmodel.getStoreOrders()

                    Toast.makeText(
                        activity,
                        response.data.errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()



                }
                is KesbewaResult.ExceptionError.ExError -> {
                    Toast.makeText(
                        activity,
                        response.exception.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is KesbewaResult.LogicError.LogError -> {
                    Toast.makeText(activity, response.exception.errorMessage, Toast.LENGTH_SHORT).show()
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
        val request = ImageRequest.Builder(requireContext())
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
        imageLoader.enqueue(request)
        dialogCustomer.show()
    }

}