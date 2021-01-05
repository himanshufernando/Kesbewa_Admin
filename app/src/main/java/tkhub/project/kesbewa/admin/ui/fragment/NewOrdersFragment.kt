package tkhub.project.kesbewa.admin.ui.fragment

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import co.nedim.maildroidx.MaildroidX
import co.nedim.maildroidx.MaildroidXType
import coil.ImageLoader
import coil.request.LoadRequest
import coil.size.Scale

import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import id.ionbit.ionalert.IonAlert
import kotlinx.android.synthetic.main.dialog_customer_details.*
import kotlinx.android.synthetic.main.dialog_order_update_note.*
import kotlinx.android.synthetic.main.fragment_new_orders.*
import kotlinx.android.synthetic.main.fragment_new_orders.view.*
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.NetworkError
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.services.network.InternetConnection
import tkhub.project.kesbewa.admin.ui.adapters.CustomerPastOrdersAdapter
import tkhub.project.kesbewa.admin.ui.adapters.NewOrdersAdapter
import tkhub.project.kesbewa.admin.viewmodels.home.HomeViewModels
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class NewOrdersFragment : Fragment() {

    private val viewmodel: HomeViewModels by viewModels { HomeViewModels.LiveDataVMFactory }
    lateinit var root: View
    var alertDialog: AlertDialog? = null
    private val adapter = NewOrdersAdapter()
    private val adapterCustomerPast = CustomerPastOrdersAdapter()

    lateinit var imageLoader: ImageLoader
    lateinit var dialogCustomer: Dialog
    lateinit var dialogReject: Dialog

    lateinit var ionAlert: IonAlert

    var selectedOrderCode: String = ""
    lateinit var filePath: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_new_orders, container, false)

        root.recyclerView_new_orders.adapter = adapter

        adapter.setOnItemClickListener(object : NewOrdersAdapter.ClickListener {
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

                    R.id.textview_confrim -> {
                        IonAlert(requireContext(), IonAlert.ERROR_TYPE)
                            .setTitleText("Are you sure?")
                            .setContentText("Are you sure you want to confirm ?")
                            .setConfirmText("Yes")
                            .setConfirmClickListener(IonAlert.ClickListener { sDialog ->
                               /* if (!viewmodel.orderUpdateResponse.hasObservers()) {
                                    orderUpdate(orderRespons)
                                }
                                viewmodel.orderStatusUpdate(orderRespons, 1, "")*/

                                if (orderRespons.itemlist.size < 20) {
                                    generatePDFOnlyOnePage(orderRespons)
                                } else {
                                    generatePDFMoreThanOnePage(orderRespons)
                                }



                                sDialog.dismissWithAnimation()

                            })
                            .setCancelText("No")
                            .setCancelClickListener(IonAlert.ClickListener { sDialog ->
                                sDialog.dismissWithAnimation()

                            })
                            .show()
                    }

                    R.id.textview_reject -> {
                        if (::dialogReject.isInitialized) {
                            if (dialogReject.isShowing) {
                                dialogReject.dismiss()
                            }
                        }
                        dialogOrderReject(orderRespons)
                    }
                }

            }
        })



        root.swiperefresh_new_orders.setOnRefreshListener {
            if (!viewmodel.newOrders.hasObservers()) {
                newOrdersObserver()
            }
            viewmodel.getNewOrders()
        }


        return root
    }

    override fun onResume() {
        super.onResume()

        if (!InternetConnection.checkInternetConnection()) {
            errorAlertDialog(AppPrefs.errorNoInternet())
        }
        if (!viewmodel.newOrders.hasObservers()) {
            newOrdersObserver()
        }
        viewmodel.getNewOrders()
    }

    override fun onStop() {
        if (::imageLoader.isInitialized) {
            imageLoader.shutdown()
        }
        viewmodel.orderUpdateResponse.removeObservers(viewLifecycleOwner)
        viewmodel.newOrders.removeObservers(viewLifecycleOwner)
        viewmodel.pastOrders.removeObservers(viewLifecycleOwner)
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        viewmodel.orderUpdateResponse.removeObservers(viewLifecycleOwner)
        viewmodel.newOrders.removeObservers(viewLifecycleOwner)
        viewmodel.pastOrders.removeObservers(viewLifecycleOwner)
    }

    fun newOrdersObserver() {
        viewmodel.newOrders.observe(viewLifecycleOwner, Observer {response ->
            root.layout_loading_new_orders.visibility = View.GONE
            root.swiperefresh_new_orders.isRefreshing = false
            when (response) {
                is KesbewaResult.Success -> {
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


fun pastOrdersObserver() {

    viewmodel.pastOrders.observe(viewLifecycleOwner, Observer {response ->
        root.layout_loading_new_orders.visibility = View.GONE
        when (response) {
            is KesbewaResult.Success -> {
                var list = response.data
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

fun orderUpdate(orderResponse: OrderRespons) {
    viewmodel.orderUpdateResponse.observe(viewLifecycleOwner, Observer {response ->
        when (response) {
            is KesbewaResult.Success -> {
                viewmodel.getNewOrders()
                Toast.makeText(
                    activity,
                    response.data.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()

              /*  if (orderResponse.itemlist.size < 20) {
                    generatePDFOnlyOnePage(orderResponse)
                } else {
                    generatePDFMoreThanOnePage(orderResponse)
                }
*/
                if (::dialogReject.isInitialized) {
                    if (dialogReject.isShowing) {
                        dialogReject.dismiss()
                    }
                }

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

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

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

    if (!viewmodel.pastOrders.hasObservers()) {
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


private fun dialogOrderReject(orderRespons: OrderRespons) {

    dialogReject = Dialog(requireContext())
    dialogReject.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialogReject.window!!.setBackgroundDrawableResource(android.R.color.transparent)
    dialogReject.setContentView(R.layout.dialog_order_update_note)
    dialogReject.setCancelable(true)

    dialogReject.textview_1.text = "ORDER REJECT-" + orderRespons.order_code


    dialogReject.layout_reject_dialog_reject.setOnClickListener {
        var rejectResaon = dialogReject.editText_note.text.toString()
        if (rejectResaon.isEmpty() || rejectResaon.isBlank()) {
            Toast.makeText(
                activity,
                "Please add reason for reject",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            orderUpdate(orderRespons)
            viewmodel.orderStatusUpdate(orderRespons, 6, rejectResaon)
        }
    }

    dialogReject.layout_reject_dialog_confrim.setOnClickListener {
        dialogReject.dismiss()
    }


    dialogReject.show()
}

fun generatePDFMoreThanOnePage(orderRespons: OrderRespons) {

    var count = 0
    var pageCount = 1

    val document = PdfDocument()
    val pageInfo = PageInfo.Builder(800, 1200, 1).create()
    var page: PdfDocument.Page = document.startPage(pageInfo)

    var canvas: Canvas = page.canvas
    val paint = Paint()
    paint.isFakeBoldText = false

    val originalImg: Bitmap =
        BitmapFactory.decodeResource(requireContext().resources, R.drawable.ic_logo)
    canvas.drawBitmap(Bitmap.createScaledBitmap(originalImg, 150, 150, true), 30.0f, 50.0f, paint)

    try {
        val bitMatrix =
            MultiFormatWriter().encode(orderRespons.order_id, BarcodeFormat.QR_CODE, 200, 200)
        val createBitmap: Bitmap = BarcodeEncoder().createBitmap(bitMatrix)
        canvas.drawBitmap(createBitmap, 590.0f, 50.0f, paint)
    } catch (e: Exception) {
    }

    paint.color = resources.getColor(R.color.colorRedTrans)
    paint.textSize = 60.0f
    canvas.drawText("UNPAID", 300.0f, 800.0f, paint)
    paint.color = resources.getColor(R.color.colorPrimary)
    paint.textSize = 16.0f
    canvas.drawText("Kesbewa", 70.0f, 230.0f, paint)
    paint.color = resources.getColor(R.color.textcolor0)

    paint.textSize = 33.0f
    canvas.drawText("INVOICE", 630.0f, 45.0f, paint)
    paint.textSize = 20.0f
    canvas.drawText("Invoice No", 50.0f, 300.0f, paint)
    canvas.drawText(": " + orderRespons.order_code, 180.0f, 300.0f, paint)
    canvas.drawText("Payment", 50.0f, 330.0f, paint)
    canvas.drawText(": cash on delivery", 180.0f, 330.0f, paint)
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
    canvas.drawText("Date ", 50.0f, 360.0f, paint)
    canvas.drawText(": " + sdf.format(orderRespons.order_date), 180.0f, 360.0f, paint)

    canvas.drawText("Dispatch Type", 50.0f, 390.0f, paint)
    canvas.drawText(": " + orderRespons.order_dispatch_type, 180.0f, 390.0f, paint)

    var dynimcY = 390f

    if ((orderRespons.order_dispatch_type == "DELIVERY")) {
        canvas.drawText("Address", 50.0f, 420.0f, paint)
        canvas.drawText(
            ": " + orderRespons.delivery_address.user_address_number,
            180.0f,
            420.0f,
            paint
        )
        canvas.drawText(
            "  " + orderRespons.delivery_address.user_address_one + ", " + orderRespons.delivery_address.user_address_two,
            180.0f,
            450.0f,
            paint
        )
        canvas.drawText(
            "  " + orderRespons.delivery_address.user_address_city,
            180.0f,
            480.0f,
            paint
        )
        dynimcY = 480f

    }



    paint.color = resources.getColor(R.color.colorPrimary)
    canvas.drawText("BILL TO :", 450.0f, 300.0f, paint)
    paint.color = resources.getColor(R.color.textcolor0)


    if (orderRespons.user.user_name.toString().length < 20) {
        canvas.drawText(orderRespons.user.user_name.toString(), 540.0f, 300.0f, paint)
    } else {
        canvas.drawText(
            orderRespons.user.user_name.toString().substring(0, 17) + "...",
            540.0f,
            300.0f,
            paint
        )
    }

    canvas.drawText(
        java.lang.String.valueOf(orderRespons.user.user_phone),
        540f,
        330f,
        paint
    )


    pdfTableCreator(canvas, paint, orderRespons, dynimcY)
    paint.color = resources.getColor(R.color.textcolor0)
    paint.textSize = 18.0f

    val itemlist = orderRespons.itemlist ?: return
    dynimcY += 80


    for (i in 0..26) {
        val item = itemlist[i]
        canvas.drawText(item.pro_name.toString(), 50.0f, dynimcY, paint);

        var price = ""
        var priceTagSize = 8 - (item.pro_price.toString()).length

        for (i in 1..priceTagSize) {
            price = "  $price"
        }
        price += item.pro_price.toString()
        canvas.drawText(price, 420.0f, dynimcY, paint)


        var qty = ""
        var qtyTagSize = 5 - (item.pro_total_qty.toString()).length

        for (i in 1..qtyTagSize) {
            qty = "  $qty"
        }
        qty += item.pro_total_qty.toString()
        canvas.drawText(qty, 520.0f, dynimcY, paint)


        var subtot = ""
        var subtotTagSize = 8 - (item.pro_total_price.toString()).length

        for (i in 1..subtotTagSize) {
            subtot = "  $subtot"
        }
        subtot += item.pro_total_price.toString()
        canvas.drawText(subtot, 690.0f, dynimcY, paint);

        dynimcY += 25f
    }

    paint.textSize = 15.0f

    canvas.drawText("Page No : 1", 700f, 1180f, paint)
    paint.textSize = 18.0f

    // pdfFooterCreator(canvas,paint,orderRespons)
    document.finishPage(page)

    var pageInfoNew = PageInfo.Builder(800, 1200, 2).create()
    page = document.startPage(pageInfoNew)
    canvas = page.canvas
    count = 1

    try {
        val bitMatrix =
            MultiFormatWriter().encode(orderRespons.order_id, BarcodeFormat.QR_CODE, 100, 100)
        val createBitmap: Bitmap = BarcodeEncoder().createBitmap(bitMatrix)
        canvas.drawBitmap(createBitmap, 690.0f, 50.0f, paint)
    } catch (e: Exception) {
    }

    paint.color = resources.getColor(R.color.colorRedTrans)
    paint.textSize = 70.0f
    canvas.drawText("UNPAID", 300.0f, 800.0f, paint)
    paint.color = resources.getColor(R.color.textcolor0)
    paint.textSize = 20.0f
    canvas.drawText("Invoice No", 50.0f, 100.0f, paint)
    canvas.drawText(": " + orderRespons.order_code, 180.0f, 100.0f, paint)

    dynimcY = 130f

    pdfTableCreator(canvas, paint, orderRespons, dynimcY)
    paint.color = resources.getColor(R.color.textcolor0)
    paint.textSize = 20.0f

    dynimcY = 200f

    for ((index, value) in itemlist.withIndex()) {
        if (index > 26) {
            if (value.pro_name.length > 25) {
                canvas.drawText(value.pro_name.substring(0, 25) + "...", 50.0f, dynimcY, paint);
            } else {
                canvas.drawText(value.pro_name, 50.0f, dynimcY, paint);
            }

            var price = ""
            var priceTagSize = 8 - (value.pro_price.toString()).length

            for (i in 1..priceTagSize) {
                price = "  $price"
            }
            price += value.pro_price.toString()
            canvas.drawText(price, 400.0f, dynimcY, paint)

            var qty = ""
            var qtyTagSize = 5 - (value.pro_total_qty.toString()).length

            for (i in 1..qtyTagSize) {
                qty = "  $qty"
            }
            qty += value.pro_total_qty.toString()
            canvas.drawText(qty, 510.0f, dynimcY, paint)


            var subtot = ""
            var subtotTagSize = 8 - (value.pro_total_price.toString()).length

            for (i in 1..subtotTagSize) {
                subtot = "  $subtot"
            }
            subtot += value.pro_total_price.toString()
            canvas.drawText(subtot, 670.0f, dynimcY, paint);


            dynimcY = dynimcY + 25f
        }
    }

    pdfFooterCreator(canvas, paint, orderRespons)

    document.finishPage(page)
    saveCreatedPDF(document, orderRespons)
}


fun generatePDFOnlyOnePage(orderRespons: OrderRespons) {

    val document = PdfDocument()
    val pageInfo = PageInfo.Builder(800, 1200, 1).create()
    val page: PdfDocument.Page = document.startPage(pageInfo)

    val canvas: Canvas = page.canvas
    val paint = Paint()
    paint.isFakeBoldText = false

    val originalImg: Bitmap =
        BitmapFactory.decodeResource(requireContext().resources, R.drawable.ic_logo)
    canvas.drawBitmap(Bitmap.createScaledBitmap(originalImg, 150, 150, true), 30.0f, 50.0f, paint)

    try {
        val bitMatrix =
            MultiFormatWriter().encode(orderRespons.order_id, BarcodeFormat.QR_CODE, 200, 200)
        val createBitmap: Bitmap = BarcodeEncoder().createBitmap(bitMatrix)
        canvas.drawBitmap(createBitmap, 590.0f, 50.0f, paint)
    } catch (e: Exception) {
    }

    paint.color = resources.getColor(R.color.colorRedTrans)
    paint.textSize = 60.0f
    canvas.drawText("UNPAID", 300.0f, 800.0f, paint)
    paint.color = resources.getColor(R.color.colorPrimary)
    paint.textSize = 16.0f
    canvas.drawText("Kesbewa", 70.0f, 230.0f, paint)
    paint.color = resources.getColor(R.color.textcolor0)

    paint.textSize = 33.0f
    canvas.drawText("INVOICE", 630.0f, 45.0f, paint)
    paint.textSize = 20.0f
    canvas.drawText("Invoice No", 50.0f, 300.0f, paint)
    canvas.drawText(": " + orderRespons.order_code, 180.0f, 300.0f, paint)
    canvas.drawText("Payment", 50.0f, 330.0f, paint)
    canvas.drawText(": cash on delivery", 180.0f, 330.0f, paint)
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
    canvas.drawText("Date ", 50.0f, 360.0f, paint)
    canvas.drawText(": " + sdf.format(orderRespons.order_date), 180.0f, 360.0f, paint)

    canvas.drawText("Dispatch Type", 50.0f, 390.0f, paint)
    canvas.drawText(": " + orderRespons.order_dispatch_type, 180.0f, 390.0f, paint)

    var dynimcY = 390f

    if ((orderRespons.order_dispatch_type == "DELIVERY")) {
        canvas.drawText("Address", 50.0f, 420.0f, paint)
        canvas.drawText(
            ": " + orderRespons.delivery_address.user_address_number,
            180.0f,
            420.0f,
            paint
        )
        canvas.drawText(
            "  " + orderRespons.delivery_address.user_address_one + ", " + orderRespons.delivery_address.user_address_two,
            180.0f,
            450.0f,
            paint
        )
        canvas.drawText(
            "  " + orderRespons.delivery_address.user_address_city,
            180.0f,
            480.0f,
            paint
        )
        dynimcY = 480f

    }



    paint.color = resources.getColor(R.color.colorPrimary)
    canvas.drawText("BILL TO :", 450.0f, 300.0f, paint)
    paint.color = resources.getColor(R.color.textcolor0)


    if (orderRespons.user.user_name.toString().length < 20) {
        canvas.drawText(orderRespons.user.user_name.toString(), 540.0f, 300.0f, paint)
    } else {
        canvas.drawText(
            orderRespons.user.user_name.toString().substring(0, 17) + "...",
            540.0f,
            300.0f,
            paint
        )
    }

    canvas.drawText(
        java.lang.String.valueOf(orderRespons.user.user_phone),
        540f,
        330f,
        paint
    )


    pdfTableCreator(canvas, paint, orderRespons, dynimcY)
    paint.color = resources.getColor(R.color.textcolor0)
    paint.textSize = 18.0f

    val itemlist = orderRespons.itemlist ?: return
    dynimcY += 80
    for (item in itemlist) {
        canvas.drawText(item.pro_name.toString(), 50.0f, dynimcY, paint);

        var price = ""
        var priceTagSize = 8 - (item.pro_price.toString()).length

        for (i in 1..priceTagSize) {
            price = "  $price"
        }
        price += item.pro_price.toString()
        canvas.drawText(price, 420.0f, dynimcY, paint)


        var qty = ""
        var qtyTagSize = 5 - (item.pro_total_qty.toString()).length

        for (i in 1..qtyTagSize) {
            qty = "  $qty"
        }
        qty += item.pro_total_qty.toString()
        canvas.drawText(qty, 520.0f, dynimcY, paint)


        var subtot = ""
        var subtotTagSize = 8 - (item.pro_total_price.toString()).length

        for (i in 1..subtotTagSize) {
            subtot = "  $subtot"
        }
        subtot += item.pro_total_price.toString()
        canvas.drawText(subtot, 690.0f, dynimcY, paint);

        dynimcY += 25f

    }

    pdfFooterCreator(canvas, paint, orderRespons)
    document.finishPage(page)
    saveCreatedPDF(document, orderRespons)


}

fun pdfTableCreator(canvas: Canvas, paint: Paint, orderRespons: OrderRespons, dim: Float) {
    paint.color = resources.getColor(R.color.colorPrimary)
    val r = Rect(20, (dim.toInt() + 20), 780, (dim.toInt() + 50))
    canvas.drawRect(r, paint)
    paint.color = resources.getColor(R.color.textcolorBlack)
    paint.textSize = 21.0f

    canvas.drawText("PRODUCT NAME", 50.0f, dim + 40f, paint)
    canvas.drawText("PRICE", 420.0f, dim + 40f, paint)
    canvas.drawText("QTY", 520.0f, dim + 40f, paint)
    canvas.drawText("SUBTOTAL", 660.0f, dim + 40f, paint)

}

fun pdfFooterCreator(canvas: Canvas, paint: Paint, orderRespons: OrderRespons) {

    var dinamicY = 990f

    paint.color = resources.getColor(R.color.textcolorBlack)
    paint.textSize = 23.0f
    canvas.drawText("Subtotal", 540.0f, dinamicY, paint)
    canvas.drawText(": RS " + orderRespons.order_subtotal_total.toString(), 650.0f, 990.0f, paint)
    if ((orderRespons.order_dispatch_type == "DELIVERY")) {
        dinamicY = 1020f
        canvas.drawText("Delivery", 540.0f, dinamicY, paint)
        canvas.drawText(
            ": RS " + orderRespons.order_delivery_chargers.toString(),
            650.0f,
            dinamicY,
            paint
        )
    }
    canvas.drawText("Discount", 540.0f, dinamicY + 30f, paint)
    canvas.drawText(": RS " + orderRespons.order_discount.toString(), 650.0f, dinamicY + 30f, paint)

    dinamicY += 30f
    paint.textSize = 15.0f
    var discount_type = ""
    when (orderRespons.order_prmo.promocode_type_code) {
        "DVW" -> discount_type = "(Wave off from delivery charges)"
        "VW" -> discount_type = "(Wave off from total value)"
        "TD" -> discount_type = "(Discount from total value)"
        "DD" -> discount_type = "(Discount from delivery charges)"
    }


    if (!orderRespons.order_prmo.promocode_id.equals(0)) {
        canvas.drawText(discount_type, 650.0f, dinamicY + 60f, paint)
        dinamicY += 60f
    }
    paint.textSize = 24.0f
    canvas.drawText("Total", 540.0f, dinamicY, paint)
    canvas.drawText(": RS " + orderRespons.order_total_price.toString(), 650.0f, dinamicY, paint)
    paint.textSize = 18.0f

    canvas.drawText(
        "Thank you for choosing kesbewa to reduce the usage of polythene and plastics",
        40.0f,
        1150f,
        paint
    )

}

fun saveCreatedPDF(doc: PdfDocument, orderRespons: OrderRespons) {

    println("sssssssssssssssssss  200")
    val directory_path =
        Environment.getExternalStorageDirectory().path + "/Kesbewa/"

    val file = File(directory_path)
    if (!file.exists()) {
        file.mkdirs()
    }

    val targetPdf = directory_path + orderRespons.order_code + "_" + "confirm" + ".pdf"
    try {
        filePath = File(targetPdf)
        doc.writeTo(FileOutputStream(filePath));
        doc.close()

           sendAdminKesbewa(targetPdf,orderRespons)
           sendAdminAshan(targetPdf,orderRespons)
           sendAdminCharith(targetPdf,orderRespons)
           sendAdminHimanshu(targetPdf,orderRespons)

    } catch (e: Exception) {

    }

}


    fun sendAdminHimanshu(path: String, orderRespons: OrderRespons) {
        MaildroidX.Builder()
            .smtp("node233.r-usdatacenter.register.lk")
            .smtpUsername("no-reply@kesbewa.com")
            .smtpPassword("]U7~Ruq0V8fV")
            .port("465")
            .type(MaildroidXType.HTML)
            .to("himanshu.fernando@gmail.com")
            .from("no-reply@kesbewa.com")
            .subject(orderRespons.order_code + " order confirmation Invoice")
            .body("invoice")
            .attachment(path)
            .onCompleteCallback(object : MaildroidX.onCompleteCallback {
                override val timeout: Long = 10000
                override fun onSuccess() {

                }

                override fun onFail(errorMessage: String) {
                }
            })
            .mail()
    }

fun sendAdminCharith(path: String, orderRespons: OrderRespons) {
    MaildroidX.Builder()
        .smtp("node233.r-usdatacenter.register.lk")
        .smtpUsername("no-reply@kesbewa.com")
        .smtpPassword("]U7~Ruq0V8fV")
        .port("465")
        .type(MaildroidXType.HTML)
        .to("ajcharith@gmail.com")
        .from("no-reply@kesbewa.com")
        .subject(orderRespons.order_code + " order confirmation Invoice")
        .body("invoice")
        .attachment(path)
        .onCompleteCallback(object : MaildroidX.onCompleteCallback {
            override val timeout: Long = 10000
            override fun onSuccess() {

            }

            override fun onFail(errorMessage: String) {
            }
        })
        .mail()
}


fun sendAdminAshan(path: String, orderRespons: OrderRespons) {
    MaildroidX.Builder()
        .smtp("node233.r-usdatacenter.register.lk")
        .smtpUsername("no-reply@kesbewa.com")
        .smtpPassword("]U7~Ruq0V8fV")
        .port("465")
        .type(MaildroidXType.HTML)
        .to("ashanwarnakula90@gmail.com")
        .from("no-reply@kesbewa.com")
        .subject(orderRespons.order_code + " order confirmation Invoice")
        .body("invoice")
        .attachment(path)
        .onCompleteCallback(object : MaildroidX.onCompleteCallback {
            override val timeout: Long = 10000
            override fun onSuccess() {

            }

            override fun onFail(errorMessage: String) {
            }
        })
        .mail()
}

fun sendAdminKesbewa(path: String, orderRespons: OrderRespons) {
    MaildroidX.Builder()
        .smtp("node233.r-usdatacenter.register.lk")
        .smtpUsername("no-reply@kesbewa.com")
        .smtpPassword("]U7~Ruq0V8fV")
        .port("465")
        .type(MaildroidXType.HTML)
        .to("himanshu.fernando@gmail.com")
        .from("no-reply@kesbewa.com")
        .subject(orderRespons.order_code + " order confirmation Invoice")
        .body("invoice")
        .attachment(path)
        .onCompleteCallback(object : MaildroidX.onCompleteCallback {
            override val timeout: Long = 10000
            override fun onSuccess() {

            }

            override fun onFail(errorMessage: String) {
            }
        })
        .mail()
}



}
