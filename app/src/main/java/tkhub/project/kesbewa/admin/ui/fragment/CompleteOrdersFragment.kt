package tkhub.project.kesbewa.admin.ui.fragment

import android.Manifest
import android.R.attr.phoneNumber
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import coil.ImageLoader
import coil.request.ImageRequest

import coil.size.Scale
import com.google.gson.Gson
import kotlinx.android.synthetic.main.dialog_customer_details.*
import kotlinx.android.synthetic.main.fragment_complete_orders.view.*
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.NetworkError
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.services.network.InternetConnection
import tkhub.project.kesbewa.admin.ui.adapters.CompleteOrdersAdapter
import tkhub.project.kesbewa.admin.viewmodels.past.PastViewModels


/**
 * A simple [Fragment] subclass.
 */
class CompleteOrdersFragment : Fragment() {

    private val viewmodel: PastViewModels by viewModels { PastViewModels.LiveDataVMFactory }
    lateinit var root: View
    var alertDialog: AlertDialog? = null
    private val adapter = CompleteOrdersAdapter()

    lateinit var imageLoader: ImageLoader
    lateinit var dialogCustomer: Dialog





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_complete_orders, container, false)

        root.recyclerView_complete_orders.adapter = adapter



        adapter.setOnItemClickListener(object : CompleteOrdersAdapter.ClickListener {
            override fun onClick(orderRespons: OrderRespons, aView: View, adapterPosition: Int) {
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
                        view?.findNavController()?.navigate(R.id.fragmentPastToMap, bundle)
                    }

                }

            }
        })


        root.swiperefresh_complete_orders.setOnRefreshListener {
            if (!viewmodel.completeOrdersResponse.hasObservers()) {
                completeOrdersResponseObserver()
            }

            viewmodel.getCompleteOrders()
        }


        return root
    }
    override fun onResume() {
        super.onResume()

        if(!InternetConnection.checkInternetConnection()){
            errorAlertDialog(AppPrefs.errorNoInternet())
        }
        if (!viewmodel.completeOrdersResponse.hasObservers()) {
            completeOrdersResponseObserver()
        }

        viewmodel.getCompleteOrders()
    }

    override fun onPause() {
        super.onPause()
        viewmodel.completeOrdersResponse.removeObservers(viewLifecycleOwner)


    }
    override fun onStop() {
        if (::imageLoader.isInitialized) {
            imageLoader.shutdown()
        }
        viewmodel.completeOrdersResponse.removeObservers(viewLifecycleOwner)
        super.onStop()
    }
    fun completeOrdersResponseObserver(){
        viewmodel.completeOrdersResponse.observe(viewLifecycleOwner, Observer {response ->
            root.layout_loading_complete_orders.visibility = View.GONE
            root.swiperefresh_complete_orders.isRefreshing = false
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
            if (checkPermissions()) {
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


    private fun checkPermissions() =
        checkSelfPermission(context as Activity, Manifest.permission.CALL_PHONE) == PERMISSION_GRANTED
}
