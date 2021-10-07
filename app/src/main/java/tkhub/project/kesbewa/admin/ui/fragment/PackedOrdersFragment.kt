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
import androidx.navigation.findNavController
import coil.ImageLoader
import com.google.gson.Gson
import id.ionbit.ionalert.IonAlert
import kotlinx.android.synthetic.main.fragment_packed_orders.view.*
import androidx.lifecycle.Observer
import co.nedim.maildroidx.MaildroidX
import co.nedim.maildroidx.MaildroidXType
import coil.request.ImageRequest

import coil.size.Scale
import kotlinx.android.synthetic.main.dialog_customer_details.*
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.NetworkError
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.services.network.InternetConnection
import tkhub.project.kesbewa.admin.ui.adapters.CustomerPastOrdersAdapter
import tkhub.project.kesbewa.admin.ui.adapters.PackedOrdersAdapter
import tkhub.project.kesbewa.admin.viewmodels.home.HomeViewModels

/**
 * A simple [Fragment] subclass.
 */
class PackedOrdersFragment : Fragment() {

    private val viewmodel: HomeViewModels by viewModels { HomeViewModels.LiveDataVMFactory }
    lateinit var root: View
    var alertDialog: AlertDialog? = null
    private val adapter = PackedOrdersAdapter()


    lateinit var imageLoader: ImageLoader
    lateinit var dialogCustomer: Dialog
    lateinit var dialogReject: Dialog

    lateinit var selectedOrder :OrderRespons

    var orderID = ""
    var confrimHTML =""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_packed_orders, container, false)


        root.recyclerView_packed_orders.adapter = adapter

        adapter.setOnItemClickListener(object : PackedOrdersAdapter.ClickListener {
            override fun onClick(orderRespons: OrderRespons, aView: View) {
                selectedOrder = orderRespons
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
                                    orderUpdateObserver()
                                }
                                viewmodel.orderStatusUpdate(orderRespons,3,"")

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

        root.swiperefresh_packed_orders.setOnRefreshListener {
            if (!viewmodel.packedOrdersResponse.hasObservers()) {
                packedOrdersResponseObserver()
            }

            viewmodel.getPackedOrders()
        }


        return root
    }
    override fun onStop() {
        super.onStop()
        if (::imageLoader.isInitialized) {
            imageLoader.shutdown()
        }
        viewmodel.orderUpdateResponse.removeObservers(viewLifecycleOwner)
        viewmodel.packedOrdersResponse.removeObservers(viewLifecycleOwner)

    }
    override fun onResume() {
        super.onResume()

        if(!InternetConnection.checkInternetConnection()){
            errorAlertDialog(AppPrefs.errorNoInternet())
        }
        if (!viewmodel.packedOrdersResponse.hasObservers()) {
            packedOrdersResponseObserver()
        }

        viewmodel.getPackedOrders()
    }
    override fun onPause() {
        super.onPause()
        viewmodel.orderUpdateResponse.removeObservers(viewLifecycleOwner)
        viewmodel.packedOrdersResponse.removeObservers(viewLifecycleOwner)

    }

    fun packedOrdersResponseObserver(){

        viewmodel.packedOrdersResponse.observe(viewLifecycleOwner, Observer {response ->
            root.layout_loading_packed_orders.visibility = View.GONE
            root.swiperefresh_packed_orders.isRefreshing = false
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


    fun orderUpdateObserver(){

        viewmodel.orderUpdateResponse.observe(viewLifecycleOwner, Observer {response ->
            root.layout_loading_packed_orders.visibility = View.GONE
            when (response) {
                is KesbewaResult.Success -> {
                    viewmodel.getPackedOrders()
                    Toast.makeText(
                        activity,
                        response.data.errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()

                    orderID = selectedOrder.order_code



                    confrimHTML = "<p>&nbsp;</p>\n" +
                            "<!-- [if gte mso 9]><xml><o:OfficeDocumentSettings><o:AllowPNG/><o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml><![endif]-->\n" +
                            "<p>&nbsp;</p>\n" +
                            "<!-- [if !mso]><!-->\n" +
                            "<p>&nbsp;</p>\n" +
                            "<!--<![endif]-->\n" +
                            "<p>&nbsp;</p>\n" +
                            "<!-- [if !mso]><!-->\n" +
                            "<p>&nbsp;</p>\n" +
                            "<!--<![endif]-->\n" +
                            "<p>&nbsp;</p>\n" +
                            "<!-- [if IE]><div class=\"ie-browser\"><![endif]-->\n" +
                            "<table class=\"nl-container\" style=\"table-layout: fixed; vertical-align: top; min-width: 320px; border-spacing: 0; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt; background-color: #ffffff; width: 100%;\" role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" bgcolor=\"#f6f3f3\">\n" +
                            "<tbody>\n" +
                            "<tr style=\"vertical-align: top;\" valign=\"top\">\n" +
                            "<td style=\"word-break: break-word; vertical-align: top;\" valign=\"top\"><!-- [if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td align=\"center\" style=\"background-color:#f6f3f3\"><![endif]-->\n" +
                            "<div style=\"background-color: transparent;\">\n" +
                            "<div class=\"block-grid mixed-two-up no-stack\" style=\"min-width: 320px; max-width: 600px; overflow-wrap: break-word; word-wrap: break-word; word-break: break-word; margin: 0 auto; background-color: #ffffff;\">\n" +
                            "<div style=\"border-collapse: collapse; display: table; width: 100%; background-color: #ffffff; background-image: url('images/bg-header-1.png'); background-position: center top; background-repeat: no-repeat;\"><!-- [if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:transparent;\"><tr><td align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px\"><tr class=\"layout-full-width\" style=\"background-color:#ffffff\"><![endif]--> <!-- [if (mso)|(IE)]><td align=\"center\" width=\"200\" style=\"background-color:#ffffff;width:200px; border-top: 0px solid transparent; border-left: 0px solid transparent; border-bottom: 0px solid transparent; border-right: 0px solid transparent;\" valign=\"top\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding-right: 0px; padding-left: 0px; padding-top:5px; padding-bottom:5px;\"><![endif]-->\n" +
                            "<div class=\"col num4\" style=\"display: table-cell; vertical-align: top; max-width: 320px; min-width: 200px; width: 200px;\">\n" +
                            "<div class=\"col_cont\" style=\"width: 100% !important;\"><!-- [if (!mso)&(!IE)]><!-->\n" +
                            "<div style=\"border: 0px solid transparent; padding: 5px 0px 5px 0px;\"><!--<![endif]-->\n" +
                            "<div class=\"img-container center autowidth\" style=\"padding-right: 0px; padding-left: 0px;\" align=\"center\"><!-- [if mso]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr style=\"line-height:0px\"><td style=\"padding-right: 0px;padding-left: 0px;\" align=\"center\"><![endif]--><br /><!-- [if mso]></td></tr></table><![endif]--></div>\n" +
                            "<!-- [if (!mso)&(!IE)]><!--></div>\n" +
                            "<!--<![endif]--></div>\n" +
                            "</div>\n" +
                            "<!-- [if (mso)|(IE)]></td></tr></table><![endif]--> <!-- [if (mso)|(IE)]></td><td align=\"center\" width=\"400\" style=\"background-color:#ffffff;width:400px; border-top: 0px solid transparent; border-left: 0px solid transparent; border-bottom: 0px solid transparent; border-right: 0px solid transparent;\" valign=\"top\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding-right: 0px; padding-left: 0px; padding-top:5px; padding-bottom:5px;\"><![endif]-->\n" +
                            "<div class=\"col num8\" style=\"display: table-cell; vertical-align: top; max-width: 320px; min-width: 400px; width: 400px;\">\n" +
                            "<div class=\"col_cont\" style=\"width: 100% !important;\"><!-- [if (!mso)&(!IE)]><!-->\n" +
                            "<div style=\"border: 0px solid transparent; padding: 5px 0px 5px 0px;\"><!--<![endif]-->\n" +
                            "<div>&nbsp;</div>\n" +
                            "<!-- [if (!mso)&(!IE)]><!--></div>\n" +
                            "<!--<![endif]--></div>\n" +
                            "</div>\n" +
                            "<!-- [if (mso)|(IE)]></td></tr></table><![endif]--> <!-- [if (mso)|(IE)]></td></tr></table></td></tr></table><![endif]--></div>\n" +
                            "</div>\n" +
                            "</div>\n" +
                            "<div style=\"background-color: transparent;\">\n" +
                            "<div class=\"block-grid two-up\" style=\"min-width: 320px; max-width: 600px; overflow-wrap: break-word; word-wrap: break-word; word-break: break-word; margin: 0 auto; background-color: #007c83;\">\n" +
                            "<div style=\"border-collapse: collapse; display: table; width: 100%; background-color: #007c83; background-image: url('images/bg-header-w-white.png'); background-position: center top; background-repeat: no-repeat;\"><!-- [if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:transparent;\"><tr><td align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px\"><tr class=\"layout-full-width\" style=\"background-color:#007c83\"><![endif]--> <!-- [if (mso)|(IE)]><td align=\"center\" width=\"300\" style=\"background-color:#007c83;width:300px; border-top: 0px solid transparent; border-left: 0px solid transparent; border-bottom: 0px solid transparent; border-right: 0px solid transparent;\" valign=\"top\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding-right: 10px; padding-left: 10px; padding-top:15px; padding-bottom:15px;\"><![endif]-->\n" +
                            "<div class=\"col num6\" style=\"display: table-cell; vertical-align: top; max-width: 320px; min-width: 300px; width: 300px;\">\n" +
                            "<div class=\"col_cont\" style=\"width: 100% !important;\"><!-- [if (!mso)&(!IE)]><!-->\n" +
                            "<div style=\"border: 0px solid transparent; padding: 15px 10px 15px 10px;\"><!--<![endif]--> <!-- [if mso]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding-right: 10px; padding-left: 10px; padding-top: 5px; padding-bottom: 5px; font-family: 'Trebuchet MS', Tahoma, sans-serif\"><![endif]-->\n" +
                            "<div style=\"color: #ffffff; font-family: 'Montserrat', 'Trebuchet MS', 'Lucida Grande', 'Lucida Sans Unicode', 'Lucida Sans', Tahoma, sans-serif; line-height: 1.2; padding: 5px 10px 5px 10px;\">\n" +
                            "<div class=\"txtTinyMce-wrapper\" style=\"line-height: 1.2; font-size: 12px; font-family: 'Montserrat', 'Trebuchet MS', 'Lucida Grande', 'Lucida Sans Unicode', 'Lucida Sans', Tahoma, sans-serif; color: #ffffff; mso-line-height-alt: 14px;\">\n" +
                            "<p style=\"font-size: 14px; line-height: 1.2; word-break: break-word; text-align: left; font-family: Montserrat, 'Trebuchet MS', 'Lucida Grande', 'Lucida Sans Unicode', 'Lucida Sans', Tahoma, sans-serif; mso-line-height-alt: 17px; margin: 0;\"><strong><span style=\"font-size: 24px;\">Your Order Has Been Passed To Delivery - "+orderID+"</span></strong></p>\n" +
                            "</div>\n" +
                            "</div>\n" +
                            "<!-- [if mso]></td></tr></table><![endif]--> <!-- [if mso]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding-right: 10px; padding-left: 10px; padding-top: 5px; padding-bottom: 5px; font-family: 'Trebuchet MS', Tahoma, sans-serif\"><![endif]-->\n" +
                            "<div style=\"color: #ffffff; font-family: 'Montserrat', 'Trebuchet MS', 'Lucida Grande', 'Lucida Sans Unicode', 'Lucida Sans', Tahoma, sans-serif; line-height: 1.2; padding: 5px 10px 5px 10px;\">\n" +
                            "<div class=\"txtTinyMce-wrapper\" style=\"line-height: 1.2; font-size: 12px; font-family: 'Montserrat', 'Trebuchet MS', 'Lucida Grande', 'Lucida Sans Unicode', 'Lucida Sans', Tahoma, sans-serif; color: #ffffff; mso-line-height-alt: 14px;\">\n" +
                            "<p style=\"font-size: 14px; line-height: 1.2; word-break: break-word; text-align: left; font-family: Montserrat, 'Trebuchet MS', 'Lucida Grande', 'Lucida Sans Unicode', 'Lucida Sans', Tahoma, sans-serif; mso-line-height-alt: 17px; margin: 0;\">Thank you for choosing Kesbewa to reduce the usage of polythene and plastics.</p>\n" +
                            "</div>\n" +
                            "</div>\n" +
                            "<!-- [if mso]></td></tr></table><![endif]--> <!-- [if (!mso)&(!IE)]><!--></div>\n" +
                            "<!--<![endif]--></div>\n" +
                            "</div>\n" +
                            "<!-- [if (mso)|(IE)]></td></tr></table><![endif]--> <!-- [if (mso)|(IE)]></td><td align=\"center\" width=\"300\" style=\"background-color:#007c83;width:300px; border-top: 0px solid transparent; border-left: 0px solid transparent; border-bottom: 0px solid transparent; border-right: 0px solid transparent;\" valign=\"top\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding-right: 0px; padding-left: 0px; padding-top:0px; padding-bottom:0px;\"><![endif]-->\n" +
                            "<div class=\"col num6\" style=\"display: table-cell; vertical-align: top; max-width: 320px; min-width: 300px; width: 300px;\">\n" +
                            "<div class=\"col_cont\" style=\"width: 100% !important;\"><!-- [if (!mso)&(!IE)]><!-->\n" +
                            "<div style=\"border: 0px solid transparent; padding: 0px;\"><!--<![endif]-->\n" +
                            "<div><img style=\"float: right;\" src=\"https://firebasestorage.googleapis.com/v0/b/kesbewa-33ecc.appspot.com/o/Resoures%2F512-x-512%20white.png?alt=media&amp;token=f67a46ec-667b-416a-8591-a033e515ad11\" alt=\"\" width=\"140\" height=\"140\"  /></div>\n" +
                            "<!-- [if (!mso)&(!IE)]><!--></div>\n" +
                            "<!--<![endif]--></div>\n" +
                            "</div>\n" +
                            "<!-- [if (mso)|(IE)]></td></tr></table><![endif]--> <!-- [if (mso)|(IE)]></td></tr></table></td></tr></table><![endif]--></div>\n" +
                            "</div>\n" +
                            "</div>\n" +
                            "<div style=\"background-color: transparent;\">\n" +
                            "<div class=\"block-grid\" style=\"min-width: 320px; max-width: 600px; overflow-wrap: break-word; word-wrap: break-word; word-break: break-word; margin: 0 auto; background-color: #ffffff;\">\n" +
                            "<div style=\"border-collapse: collapse; display: table; width: 100%; background-color: #ffffff;\"><!-- [if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:transparent;\"><tr><td align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px\"><tr class=\"layout-full-width\" style=\"background-color:#ffffff\"><![endif]--> <!-- [if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"background-color:#ffffff;width:600px; border-top: 0px solid transparent; border-left: 0px solid transparent; border-bottom: 0px solid transparent; border-right: 0px solid transparent;\" valign=\"top\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding-right: 0px; padding-left: 0px; padding-top:0px; padding-bottom:0px;\"><![endif]-->\n" +
                            "<div class=\"col num12\" style=\"min-width: 320px; max-width: 600px; display: table-cell; vertical-align: top; width: 600px;\">\n" +
                            "<div class=\"col_cont\" style=\"width: 100% !important;\"><!-- [if (!mso)&(!IE)]><!-->\n" +
                            "<div style=\"border: 0px solid transparent; padding: 0px;\"><!--<![endif]--><!-- [if (!mso)&(!IE)]><!--></div>\n" +
                            "<!--<![endif]--></div>\n" +
                            "</div>\n" +
                            "<!-- [if (mso)|(IE)]></td></tr></table><![endif]--> <!-- [if (mso)|(IE)]></td></tr></table></td></tr></table><![endif]--></div>\n" +
                            "</div>\n" +
                            "</div>\n" +
                            "<div style=\"background-color: transparent;\">\n" +
                            "<div class=\"block-grid\" style=\"min-width: 320px; max-width: 600px; overflow-wrap: break-word; word-wrap: break-word; word-break: break-word; margin: 0 auto; background-color: #ffffff;\">\n" +
                            "<div style=\"border-collapse: collapse; display: table; width: 100%; background-color: #ffffff;\"><!-- [if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:transparent;\"><tr><td align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px\"><tr class=\"layout-full-width\" style=\"background-color:#ffffff\"><![endif]--> <!-- [if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"background-color:#ffffff;width:600px; border-top: 0px solid transparent; border-left: 0px solid transparent; border-bottom: 0px solid transparent; border-right: 0px solid transparent;\" valign=\"top\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding-right: 0px; padding-left: 0px; padding-top:5px; padding-bottom:5px;\"><![endif]-->\n" +
                            "<div class=\"col num12\" style=\"min-width: 320px; max-width: 600px; display: table-cell; vertical-align: top; width: 600px;\">\n" +
                            "<div class=\"col_cont\" style=\"width: 100% !important;\"><!-- [if (!mso)&(!IE)]><!-->\n" +
                            "<div style=\"border: 0px solid transparent; padding: 5px 0px 5px 0px;\"><!--<![endif]-->\n" +
                            "<div class=\"img-container center fixedwidth\" style=\"padding-right: 5px; padding-left: 5px;\" align=\"center\"><!-- [if mso]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr style=\"line-height:0px\"><td style=\"padding-right: 5px;padding-left: 5px;\" align=\"center\"><![endif]-->\n" +
                            "<div style=\"font-size: 1px; line-height: 10px;\">&nbsp;</div>\n" +
                            "<a style=\"outline: none;\" tabindex=\"-1\" href=\"http://www.example.com\" target=\"_blank\" rel=\"noopener\"><img class=\"center fixedwidth\" style=\"text-decoration: none; height: auto; border-width: 0px; border-color: initial; border-image: initial; width: 100%; max-width: 120px; display: block;\" title=\"Your Logo\" src=\"https://firebasestorage.googleapis.com/v0/b/kesbewa-33ecc.appspot.com/o/Resoures%2Fwater%20mark.png?alt=media&amp;token=e306e7c3-4ed9-4914-afb9-0e6923166644\" alt=\"Your Logo\" width=\"120\" align=\"center\" border=\"0\" /></a>\n" +
                            "<div style=\"font-size: 1px; line-height: 10px;\">&nbsp;</div>\n" +
                            "<!-- [if mso]></td></tr></table><![endif]--></div>\n" +
                            "<table class=\"social_icons\" style=\"table-layout: fixed; vertical-align: top; border-spacing: 0px; border-collapse: collapse; height: 82px; width: 100%;\" role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                            "<tbody>\n" +
                            "<tr style=\"vertical-align: top;\" valign=\"top\">\n" +
                            "<td style=\"word-break: break-word; vertical-align: top; padding: 10px 5px; height: 82px;\" valign=\"top\">\n" +
                            "<table class=\"social_table\" style=\"table-layout: fixed; vertical-align: top; border-spacing: 0; border-collapse: collapse; mso-table-tspace: 0; mso-table-rspace: 0; mso-table-bspace: 0; mso-table-lspace: 0;\" role=\"presentation\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\">\n" +
                            "<tbody>\n" +
                            "<tr style=\"vertical-align: top; display: inline-block; text-align: center;\" align=\"center\" valign=\"top\">\n" +
                            "<td style=\"word-break: break-word; vertical-align: top; padding-bottom: 0; padding-right: 10px; padding-left: 10px;\" valign=\"top\"><a href=\"https://www.facebook.com/Kesbewasl/\" target=\"_blank\" rel=\"noopener\"><img style=\"text-decoration: none; height: auto; border-width: 0px; border-color: initial; border-image: initial; display: block;\" title=\"Facebook\" src=\"https://firebasestorage.googleapis.com/v0/b/kesbewa-33ecc.appspot.com/o/Resoures%2Ficons8-facebook-f-64.png?alt=media&amp;token=13f1303d-4749-46e8-8e83-06f5b75b1a5e\" alt=\"Facebook\" width=\"32\" height=\"32\" /></a></td>\n" +
                            "<td style=\"word-break: break-word; vertical-align: top; padding-bottom: 0; padding-right: 10px; padding-left: 10px;\" valign=\"top\"><a href=\"http://kesbewa.com/\" target=\"_blank\" rel=\"noopener\"><img style=\"text-decoration: none; height: auto; border-width: 0px; border-color: initial; border-image: initial; display: block;\" title=\"Instagram\" src=\"https://firebasestorage.googleapis.com/v0/b/kesbewa-33ecc.appspot.com/o/Resoures%2Ficons8-internet-64.png?alt=media&amp;token=11d89612-c754-433e-b688-e6dd9596cbb8\" alt=\"Instagram\" width=\"32\" height=\"32\" /></a></td>\n" +
                            "<td style=\"word-break: break-word; vertical-align: top; padding-bottom: 0; padding-right: 10px; padding-left: 10px;\" valign=\"top\"><img style=\"text-decoration: none; height: auto; border-width: 0px; border-color: initial; border-image: initial; display: block;\" title=\"Medium\" src=\"https://firebasestorage.googleapis.com/v0/b/kesbewa-33ecc.appspot.com/o/Resoures%2Ficons8-phone-64.png?alt=media&amp;token=3f12a8e3-0d01-41c8-a459-259a64dbd95b\" alt=\"0774551646\" width=\"32\" height=\"32\" /></td>\n" +
                            "</tr>\n" +
                            "</tbody>\n" +
                            "</table>\n" +
                            "<p style=\"text-align: center;\">0774551646</p>\n" +
                            "</td>\n" +
                            "</tr>\n" +
                            "</tbody>\n" +
                            "</table>\n" +
                            "<!-- [if mso]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding-right: 10px; padding-left: 10px; padding-top: 10px; padding-bottom: 10px; font-family: Tahoma, sans-serif\"><![endif]-->\n" +
                            "<div style=\"color: #7b7b7b; font-family: Montserrat, Trebuchet MS, Lucida Grande, Lucida Sans Unicode, Lucida Sans, Tahoma, sans-serif; line-height: 1.2; padding: 10px;\">\n" +
                            "<div class=\"txtTinyMce-wrapper\" style=\"line-height: 1.2; font-size: 12px; color: #7b7b7b; font-family: Montserrat, Trebuchet MS, Lucida Grande, Lucida Sans Unicode, Lucida Sans, Tahoma, sans-serif; mso-line-height-alt: 14px;\">\n" +
                            "<p style=\"font-size: 10px; line-height: 1.2; word-break: break-word; text-align: center; mso-line-height-alt: 12px; margin: 0;\"><span style=\"font-size: 10px;\">&copy; 2021 Kesbewa. All Rights Reserved.&nbsp; <a href=\"http://kesbewa.com/API/privacy%20policy/privacy_policy.html\" target=\"_blank\" rel=\"noopener\">Terms and Conditions</a></span></p>\n" +
                            "</div>\n" +
                            "</div>\n" +
                            "<!-- [if mso]></td></tr></table><![endif]-->\n" +
                            "<table class=\"divider\" style=\"table-layout: fixed; vertical-align: top; border-spacing: 0; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt; min-width: 100%; -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%;\" role=\"presentation\" border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n" +
                            "<tbody>\n" +
                            "<tr style=\"vertical-align: top;\" valign=\"top\">\n" +
                            "<td class=\"divider_inner\" style=\"word-break: break-word; vertical-align: top; min-width: 100%; -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%; padding: 0px;\" valign=\"top\">\n" +
                            "<table class=\"divider_content\" style=\"table-layout: fixed; vertical-align: top; border-spacing: 0; border-collapse: collapse; mso-table-lspace: 0pt; mso-table-rspace: 0pt; border-top: 0px solid transparent; height: 20px; width: 100%;\" role=\"presentation\" border=\"0\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\">\n" +
                            "<tbody>\n" +
                            "<tr style=\"vertical-align: top;\" valign=\"top\">\n" +
                            "<td style=\"word-break: break-word; vertical-align: top; -ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%;\" valign=\"top\" height=\"20\">&nbsp;</td>\n" +
                            "</tr>\n" +
                            "</tbody>\n" +
                            "</table>\n" +
                            "</td>\n" +
                            "</tr>\n" +
                            "</tbody>\n" +
                            "</table>\n" +
                            "<!-- [if (!mso)&(!IE)]><!--></div>\n" +
                            "<!--<![endif]--></div>\n" +
                            "</div>\n" +
                            "<!-- [if (mso)|(IE)]></td></tr></table><![endif]--> <!-- [if (mso)|(IE)]></td></tr></table></td></tr></table><![endif]--></div>\n" +
                            "</div>\n" +
                            "</div>\n" +
                            "<div style=\"background-color: transparent;\">\n" +
                            "<div class=\"block-grid\" style=\"min-width: 320px; max-width: 600px; overflow-wrap: break-word; word-wrap: break-word; word-break: break-word; margin: 0 auto; background-color: transparent;\">\n" +
                            "<div style=\"border-collapse: collapse; display: table; width: 100%; background-color: transparent;\"><!-- [if (mso)|(IE)]><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"background-color:transparent;\"><tr><td align=\"center\"><table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"width:600px\"><tr class=\"layout-full-width\" style=\"background-color:transparent\"><![endif]--> <!-- [if (mso)|(IE)]><td align=\"center\" width=\"600\" style=\"background-color:transparent;width:600px; border-top: 0px solid transparent; border-left: 0px solid transparent; border-bottom: 0px solid transparent; border-right: 0px solid transparent;\" valign=\"top\"><table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\"><tr><td style=\"padding-right: 0px; padding-left: 0px; padding-top:5px; padding-bottom:5px;\"><![endif]-->\n" +
                            "<div class=\"col num12\" style=\"min-width: 320px; max-width: 600px; display: table-cell; vertical-align: top; width: 600px;\">\n" +
                            "<div class=\"col_cont\" style=\"width: 100% !important;\"><!-- [if (!mso)&(!IE)]><!-->\n" +
                            "<div style=\"border: 0px solid transparent; padding: 5px 0px 5px 0px;\"><!--<![endif]--><!-- [if (!mso)&(!IE)]><!--></div>\n" +
                            "<!--<![endif]--></div>\n" +
                            "</div>\n" +
                            "<!-- [if (mso)|(IE)]></td></tr></table><![endif]--> <!-- [if (mso)|(IE)]></td></tr></table></td></tr></table><![endif]--></div>\n" +
                            "</div>\n" +
                            "</div>\n" +
                            "<!-- [if (mso)|(IE)]></td></tr></table><![endif]--></td>\n" +
                            "</tr>\n" +
                            "</tbody>\n" +
                            "</table>\n" +
                            "<!-- [if (IE)]></div><![endif]-->"






                    sendUser(selectedOrder)
                    sendAdminKesbewa(selectedOrder)


                    viewmodel.getNewOrders()

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

    fun sendAdminKesbewa(orderRespons: OrderRespons) {
        MaildroidX.Builder()
            .smtp("node236.r-usdatacenter.register.lk")
            .smtpUsername("no-reply@kesbewa.com")
            .smtpPassword("]U7~Ruq0V8fV")
            .port("465")
            .type(MaildroidXType.HTML)
            .to("info.kesbewa@gmail.com")
            .from("no-reply@kesbewa.com")
            .subject(orderRespons.order_code + " Delivery")
            .body(confrimHTML)
            .onCompleteCallback(object : MaildroidX.onCompleteCallback {
                override val timeout: Long = 10000
                override fun onSuccess() {

                }

                override fun onFail(errorMessage: String) {
                }
            })
            .mail()
    }

    fun sendUser(orderRespons: OrderRespons) {


        MaildroidX.Builder()
            .smtp("node236.r-usdatacenter.register.lk")
            .smtpUsername("no-reply@kesbewa.com")
            .smtpPassword("]U7~Ruq0V8fV")
            .port("465")
            .type(MaildroidXType.HTML)
            .to(orderRespons.user.user_email.toString())
            .from("no-reply@kesbewa.com")
            .subject(orderRespons.order_code + " Delivery")
            .body(confrimHTML)
            .onCompleteCallback(object : MaildroidX.onCompleteCallback {
                override val timeout: Long = 10000
                override fun onSuccess() {
                    Toast.makeText(
                        requireContext(),
                        "Invoice Email send to user",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFail(errorMessage: String) {
                    Toast.makeText(
                        requireContext(),
                        "Invoice Email NOT send to user $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            .mail()
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
