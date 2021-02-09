package tkhub.project.kesbewa.admin.ui.fragment

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import coil.request.LoadRequest
import coil.size.Scale
import com.google.gson.Gson
import id.ionbit.ionalert.IonAlert
import kotlinx.android.synthetic.main.dialog_customer_details.*
import kotlinx.android.synthetic.main.fragment_confirmed_orders.view.*
import androidx.lifecycle.Observer
import co.nedim.maildroidx.MaildroidX
import co.nedim.maildroidx.MaildroidXType
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.NetworkError
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.services.network.InternetConnection
import tkhub.project.kesbewa.admin.ui.adapters.ConfirmedOrdersAdapter
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
class ConfirmedOrdersFragment : Fragment() {


    private val viewmodel: HomeViewModels by viewModels { HomeViewModels.LiveDataVMFactory }
    lateinit var root: View
    var alertDialog: AlertDialog? = null
    private val adapter = ConfirmedOrdersAdapter()
    private val adapterCustomerPast = CustomerPastOrdersAdapter()

    lateinit var imageLoader: ImageLoader
    lateinit var dialogCustomer: Dialog
    lateinit var dialogReject: Dialog
    lateinit var filePath: File
    lateinit var mStorage: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_confirmed_orders, container, false)
        root.recyclerView_confirmed_orders.adapter = adapter



        adapter.setOnItemClickListener(object : ConfirmedOrdersAdapter.ClickListener {
            override fun onClick(orderRespons: OrderRespons, aView: View,adapterPosition: Int) {
                when (aView.id) {
                    R.id.imageview_customer_details -> {
                        if (::dialogCustomer.isInitialized) {
                            if (dialogCustomer.isShowing) {
                                dialogCustomer.dismiss()
                            }
                        }
                        dialogCustomerDetails(orderRespons)
                    }
                    R.id.imageview_arrow_con -> {
                        println("ssssssssssssssssssssssssss")
                        orderRespons.is_order_details_expain = !orderRespons.is_order_details_expain
                        adapter.notifyItemChanged(adapterPosition)

                    }

                    R.id.imageview_address_details -> {
                        val bundle =
                            bundleOf("deliveryAddress" to Gson().toJson(orderRespons.delivery_address))
                        view?.findNavController()?.navigate(R.id.fragmentHomeToMap, bundle)
                    }

                    R.id.textview_packed -> {
                        IonAlert(requireContext(), IonAlert.ERROR_TYPE)
                            .setTitleText("Are you sure?")
                            .setContentText("Are you sure, you want to Packed ?")
                            .setConfirmText("Yes")
                            .setConfirmClickListener(IonAlert.ClickListener { sDialog ->
                                if (!viewmodel.orderUpdateResponse.hasObservers()) {
                                    orderUpdateObserver(orderRespons)
                                }
                                if (orderRespons.order_dispatch_type == "STORE") {
                                    viewmodel.orderStatusUpdate(orderRespons, 7, "")
                                } else {
                                    viewmodel.orderStatusUpdate(orderRespons, 2, "")
                                }

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

        root.swiperefresh_confirmed_orders.setOnRefreshListener {
            if (!viewmodel.confirmedOrdersResponse.hasObservers()) {
                confirmedOrdersResponseObserver()
            }
            viewmodel.getConformedOrders()
        }


        return root
    }

    override fun onResume() {
        super.onResume()
        if (!InternetConnection.checkInternetConnection()) {
            errorAlertDialog(AppPrefs.errorNoInternet())
        }

        if (!viewmodel.confirmedOrdersResponse.hasObservers()) {
            confirmedOrdersResponseObserver()
        }
        viewmodel.getConformedOrders()
    }

    override fun onStop() {
        if (::imageLoader.isInitialized) {
            imageLoader.shutdown()
        }
        viewmodel.orderUpdateResponse.removeObservers(viewLifecycleOwner)
        viewmodel.confirmedOrdersResponse.removeObservers(viewLifecycleOwner)

        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        viewmodel.orderUpdateResponse.removeObservers(viewLifecycleOwner)
        viewmodel.confirmedOrdersResponse.removeObservers(viewLifecycleOwner)
    }

    fun confirmedOrdersResponseObserver() {

        viewmodel.confirmedOrdersResponse.observe(viewLifecycleOwner, Observer { response ->
            root.layout_loading_confirmed_orders.visibility = View.GONE
            root.swiperefresh_confirmed_orders.isRefreshing = false
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

    fun orderUpdateObserver(orderResponse: OrderRespons) {

        viewmodel.orderUpdateResponse.observe(viewLifecycleOwner, Observer { response ->
            root.layout_loading_confirmed_orders.visibility = View.GONE
            when (response) {
                is KesbewaResult.Success -> {

                    if (!viewmodel.confirmedOrdersResponse.hasObservers()) {
                        confirmedOrdersResponseObserver()
                    }
                    viewmodel.getConformedOrders()
                    Toast.makeText(
                        activity,
                        response.data.errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()

                    if (orderResponse.order_dispatch_type == "STORE") {

                        if (orderResponse.itemlist.size < 20) {
                            generatePDFOnlyOnePage(orderResponse)
                        } else {
                            generatePDFMoreThanOnePage(orderResponse)
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
                    Toast.makeText(activity, response.exception.errorMessage, Toast.LENGTH_SHORT)
                        .show()
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

    fun generatePDFMoreThanOnePage(orderRespons: OrderRespons) {

        var count = 0
        var pageCount = 1

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(800, 1200, 1).create()
        var page: PdfDocument.Page = document.startPage(pageInfo)

        var canvas: Canvas = page.canvas
        val paint = Paint()
        paint.isFakeBoldText = false

        val originalImg: Bitmap =
            BitmapFactory.decodeResource(requireContext().resources, R.drawable.ic_logo)
        canvas.drawBitmap(Bitmap.createScaledBitmap(originalImg, 80, 80, true), 40.0f, 50.0f, paint)

        try {
            val bitMatrix =
                MultiFormatWriter().encode(orderRespons.order_id, BarcodeFormat.QR_CODE, 100, 100)
            val createBitmap: Bitmap = BarcodeEncoder().createBitmap(bitMatrix)
            canvas.drawBitmap(createBitmap, 690.0f, 50.0f, paint)
        } catch (e: Exception) {
        }


        paint.color = resources.getColor(R.color.colorPrimary)
        paint.textSize = 12.0f
        canvas.drawText("Kesbewa", 55.0f, 150.0f, paint)
        paint.color = resources.getColor(R.color.textcolor0)

        paint.textSize = 20.0f
        canvas.drawText("INVOICE", 700.0f, 45.0f, paint)


        paint.textSize = 13.0f
        canvas.drawText("Invoice No", 50.0f, 200.0f, paint)
        canvas.drawText(": " + orderRespons.order_code, 180.0f, 200.0f, paint)


        canvas.drawText("Payment", 50.0f, 220.0f, paint)
        canvas.drawText(": cash on delivery", 180.0f, 220.0f, paint)


        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        canvas.drawText("Date ", 50.0f, 240.0f, paint)
        canvas.drawText(": " + sdf.format(orderRespons.order_date), 180.0f, 240.0f, paint)

        canvas.drawText("Dispatch Type", 50.0f, 260.0f, paint)
        canvas.drawText(": " + orderRespons.order_dispatch_type, 180.0f, 260.0f, paint)

        var dynimcY = 260f

        if ((orderRespons.order_dispatch_type == "DELIVERY")) {
            canvas.drawText("Address", 50.0f, 280.0f, paint)
            canvas.drawText(
                ": " + orderRespons.delivery_address.user_address_number,
                180.0f,
                280.0f,
                paint
            )
            canvas.drawText(
                "  " + orderRespons.delivery_address.user_address_one + ", " + orderRespons.delivery_address.user_address_two,
                180.0f,
                300.0f,
                paint
            )
            canvas.drawText(
                "  " + orderRespons.delivery_address.user_address_city,
                180.0f,
                320.0f,
                paint
            )
            dynimcY = 320f

        }




        canvas.drawText("BILL TO :", 450.0f, 200.0f, paint)
        paint.color = resources.getColor(R.color.colorPrimary)

        if (orderRespons.user.user_name.toString().length < 20) {
            canvas.drawText(orderRespons.user.user_name.toString(), 540.0f, 200.0f, paint)
        } else {
            canvas.drawText(
                orderRespons.user.user_name.toString().substring(0, 17) + "...",
                540.0f,
                200.0f,
                paint
            )
        }

        canvas.drawText(
            java.lang.String.valueOf(orderRespons.user.user_phone),
            540f,
            220f,
            paint
        )

        paint.color = resources.getColor(R.color.textcolor0)
        canvas.drawText("STATUS :", 450.0f, 240.0f, paint)

        paint.color = resources.getColor(R.color.colorRed)
        canvas.drawText("PAID", 540.0f, 240.0f, paint)


        pdfTableCreator(canvas, paint, orderRespons, dynimcY)


        paint.color = resources.getColor(R.color.textcolor0)
        paint.textSize = 13.0f

        val itemlist = orderRespons.itemlist ?: return
        dynimcY += 80


        for (i in 0..26) {
            val item = itemlist[i]
            canvas.drawText(item.pro_name + "(" + item.pro_size + ")", 50.0f, dynimcY, paint);

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

            dynimcY += 18f
        }

        paint.textSize = 10.0f

        canvas.drawText("Page No : 1", 700f, 1180f, paint)



        document.finishPage(page)

        var pageInfoNew = PdfDocument.PageInfo.Builder(800, 1200, 2).create()
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


        paint.color = resources.getColor(R.color.textcolor0)
        paint.textSize = 14.0f
        canvas.drawText("Invoice No", 50.0f, 100.0f, paint)
        canvas.drawText(": " + orderRespons.order_code, 180.0f, 100.0f, paint)


        //////
        dynimcY = 130f

        pdfTableCreator(canvas, paint, orderRespons, dynimcY)
        paint.color = resources.getColor(R.color.textcolor0)
        paint.textSize = 13.0f

        dynimcY = 200f

        for ((index, value) in itemlist.withIndex()) {
            if (index > 26) {
                if (value.pro_name.length > 25) {
                    canvas.drawText(
                        value.pro_name.substring(
                            0,
                            25
                        ) + "(" + value.pro_size + ") ...", 50.0f, dynimcY, paint
                    );
                } else {
                    canvas.drawText(
                        value.pro_name + "(" + value.pro_size + ")",
                        50.0f,
                        dynimcY,
                        paint
                    );
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


                dynimcY += 18f
            }
        }

        pdfFooterCreator(canvas, paint, orderRespons)

        document.finishPage(page)
        saveCreatedPDF(document, orderRespons)
    }


    fun generatePDFOnlyOnePage(orderRespons: OrderRespons) {

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(800, 1200, 1).create()
        val page: PdfDocument.Page = document.startPage(pageInfo)

        val canvas: Canvas = page.canvas
        val paint = Paint()
        paint.isFakeBoldText = false

        val originalImg: Bitmap =
            BitmapFactory.decodeResource(requireContext().resources, R.drawable.ic_logo)
        canvas.drawBitmap(Bitmap.createScaledBitmap(originalImg, 80, 80, true), 40.0f, 50.0f, paint)

        try {
            val bitMatrix =
                MultiFormatWriter().encode(orderRespons.order_id, BarcodeFormat.QR_CODE, 100, 100)
            val createBitmap: Bitmap = BarcodeEncoder().createBitmap(bitMatrix)
            canvas.drawBitmap(createBitmap, 690.0f, 50.0f, paint)
        } catch (e: Exception) {
        }


        paint.color = resources.getColor(R.color.colorPrimary)
        paint.textSize = 12.0f
        canvas.drawText("Kesbewa", 55.0f, 150.0f, paint)
        paint.color = resources.getColor(R.color.textcolor0)

        paint.textSize = 20.0f
        canvas.drawText("INVOICE", 700.0f, 45.0f, paint)


        paint.textSize = 13.0f
        canvas.drawText("Invoice No", 50.0f, 200.0f, paint)
        canvas.drawText(": " + orderRespons.order_code, 180.0f, 200.0f, paint)


        canvas.drawText("Payment", 50.0f, 220.0f, paint)
        canvas.drawText(": cash on delivery", 180.0f, 220.0f, paint)


        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        canvas.drawText("Date ", 50.0f, 240.0f, paint)
        canvas.drawText(": " + sdf.format(orderRespons.order_date), 180.0f, 240.0f, paint)

        canvas.drawText("Dispatch Type", 50.0f, 260.0f, paint)
        canvas.drawText(": " + orderRespons.order_dispatch_type, 180.0f, 260.0f, paint)

        var dynimcY = 260f

        if ((orderRespons.order_dispatch_type == "DELIVERY")) {
            canvas.drawText("Address", 50.0f, 280.0f, paint)
            canvas.drawText(
                ": " + orderRespons.delivery_address.user_address_number,
                180.0f,
                280.0f,
                paint
            )
            canvas.drawText(
                "  " + orderRespons.delivery_address.user_address_one + ", " + orderRespons.delivery_address.user_address_two,
                180.0f,
                300.0f,
                paint
            )
            canvas.drawText(
                "  " + orderRespons.delivery_address.user_address_city,
                180.0f,
                320.0f,
                paint
            )
            dynimcY = 320f

        }




        canvas.drawText("BILL TO :", 450.0f, 200.0f, paint)
        paint.color = resources.getColor(R.color.colorPrimary)

        if (orderRespons.user.user_name.toString().length < 20) {
            canvas.drawText(orderRespons.user.user_name.toString(), 540.0f, 200.0f, paint)
        } else {
            canvas.drawText(
                orderRespons.user.user_name.toString().substring(0, 17) + "...",
                540.0f,
                200.0f,
                paint
            )
        }

        canvas.drawText(
            java.lang.String.valueOf(orderRespons.user.user_phone),
            540f,
            220f,
            paint
        )

        paint.color = resources.getColor(R.color.textcolor0)
        canvas.drawText("STATUS :", 450.0f, 240.0f, paint)

        paint.color = resources.getColor(R.color.colorRed)
        canvas.drawText("PAID", 540.0f, 240.0f, paint)


        pdfTableCreator(canvas, paint, orderRespons, dynimcY)



        paint.color = resources.getColor(R.color.textcolor0)
        paint.textSize = 13.0f


        val itemlist = orderRespons.itemlist ?: return


        dynimcY += 65
        for (item in itemlist) {
            canvas.drawText(item.pro_name + "(" + item.pro_size + ")", 50.0f, dynimcY, paint);

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

            dynimcY += 18f

        }

        if (orderRespons.itemlist.size <= 5) {
            pdfFooterCreator(canvas, paint, orderRespons, dynimcY)
        } else {
            pdfFooterCreator(canvas, paint, orderRespons)
        }

        document.finishPage(page)
        saveCreatedPDF(document, orderRespons)


    }

    fun pdfTableCreator(canvas: Canvas, paint: Paint, orderRespons: OrderRespons, dim: Float) {

        paint.color = resources.getColor(R.color.colorPrimary)
        val r = Rect(20, (dim.toInt() + 20), 780, (dim.toInt() + 40))
        canvas.drawRect(r, paint)
        paint.color = resources.getColor(R.color.textcolorBlack)
        paint.textSize = 13.0f

        canvas.drawText("PRODUCT NAME", 50.0f, dim + 35f, paint)
        canvas.drawText("PRICE", 430.0f, dim + 35f, paint)
        canvas.drawText("QTY", 540.0f, dim + 35f, paint)
        canvas.drawText("SUBTOTAL", 700.0f, dim + 35f, paint)

    }

    fun pdfFooterCreator(canvas: Canvas, paint: Paint, orderRespons: OrderRespons, dim: Float) {
        var dinamicY = dim + 10f
        paint.color = resources.getColor(R.color.textcolorBlack)
        val r = Rect(20, (dim.toInt()), 780, (dim.toInt()))
        canvas.drawRect(r, paint)

        dinamicY += 20f

        paint.color = resources.getColor(R.color.textcolorBlack)
        paint.textSize = 13.0f
        canvas.drawText("Subtotal", 600.0f, dinamicY, paint)
        canvas.drawText(
            ": RS " + orderRespons.order_subtotal_total.toString(),
            690.0f,
            dinamicY,
            paint
        )


        if ((orderRespons.order_dispatch_type == "DELIVERY")) {
            canvas.drawText("Delivery", 600.0f, dinamicY + 20, paint)
            canvas.drawText(
                ": RS " + orderRespons.order_delivery_chargers.toString(),
                690.0f,
                dinamicY + 20,
                paint
            )

            dinamicY += 20f
        }


        canvas.drawText("Discount", 600.0f, dinamicY + 20f, paint)
        canvas.drawText(
            ": RS " + orderRespons.order_discount.toString(),
            690.0f,
            dinamicY + 20f,
            paint
        )

        dinamicY += 20f

        paint.textSize = 12.0f
        paint.color = resources.getColor(R.color.colorRed)
        var discount_type = ""
        when (orderRespons.order_promo.promocode_type_code) {
            "DVW" -> discount_type = "(Wave off from delivery charges)"
            "VW" -> discount_type = "(Wave off from total value)"
            "TD" -> discount_type = "(Discount from total value)"
            "DD" -> discount_type = "(Discount from delivery charges)"
        }
        if (!orderRespons.order_promo.promocode_id.equals(0)) {
            canvas.drawText(discount_type, 600.0f, dinamicY + 15f, paint)
            dinamicY += 35f
        }

        paint.color = resources.getColor(R.color.textcolorBlack)
        paint.textSize = 16.0f

        canvas.drawText("Total", 600.0f, dinamicY, paint)
        canvas.drawText(
            ": RS " + orderRespons.order_total_price.toString(),
            690.0f,
            dinamicY,
            paint
        )
        paint.textSize = 14.0f

        canvas.drawText(
            "Thank you for choosing kesbewa to reduce the usage of polythene and plastics",
            40.0f,
            dinamicY + 20,
            paint
        )

    }


    fun pdfFooterCreator(canvas: Canvas, paint: Paint, orderRespons: OrderRespons) {

        var dinamicY = 990f

        paint.color = resources.getColor(R.color.textcolorBlack)
        val r = Rect(20, (dinamicY.toInt()), 780, (dinamicY.toInt()))
        canvas.drawRect(r, paint)

        dinamicY += 20f


        paint.color = resources.getColor(R.color.textcolorBlack)
        paint.textSize = 13.0f
        canvas.drawText("Subtotal", 600.0f, dinamicY, paint)
        canvas.drawText(
            ": RS " + orderRespons.order_subtotal_total.toString(),
            690.0f,
            dinamicY,
            paint
        )


        if ((orderRespons.order_dispatch_type == "DELIVERY")) {
            canvas.drawText("Delivery", 600.0f, dinamicY + 20, paint)
            canvas.drawText(
                ": RS " + orderRespons.order_delivery_chargers.toString(),
                690.0f,
                dinamicY + 20,
                paint
            )

            dinamicY += 20f
        }


        canvas.drawText("Discount", 600.0f, dinamicY + 20f, paint)
        canvas.drawText(
            ": RS " + orderRespons.order_discount.toString(),
            690.0f,
            dinamicY + 20f,
            paint
        )

        dinamicY += 20f

        paint.textSize = 12.0f
        paint.color = resources.getColor(R.color.colorRed)
        var discount_type = ""
        when (orderRespons.order_promo.promocode_type_code) {
            "DVW" -> discount_type = "(Wave off from delivery charges)"
            "VW" -> discount_type = "(Wave off from total value)"
            "TD" -> discount_type = "(Discount from total value)"
            "DD" -> discount_type = "(Discount from delivery charges)"
        }
        if (!orderRespons.order_promo.promocode_id.equals(0)) {
            canvas.drawText(discount_type, 600.0f, dinamicY + 15f, paint)
            dinamicY += 35f
        }

        paint.color = resources.getColor(R.color.textcolorBlack)
        paint.textSize = 16.0f

        canvas.drawText("Total", 600.0f, dinamicY, paint)
        canvas.drawText(
            ": RS " + orderRespons.order_total_price.toString(),
            690.0f,
            dinamicY,
            paint
        )
        paint.textSize = 14.0f

        canvas.drawText(
            "Thank you for choosing kesbewa to reduce the usage of polythene and plastics",
            40.0f,
            dinamicY + 20,
            paint
        )


    }


    fun saveCreatedPDF(doc: PdfDocument, orderRespons: OrderRespons) {

        val directory_path =
            Environment.getExternalStorageDirectory().path + "/Kesbewa/"

        val file = File(directory_path)
        if (!file.exists()) {
            file.mkdirs()
        }

        val targetPdf = directory_path + orderRespons.order_code + "_" + "complete" + ".pdf"


        try {
            filePath = File(targetPdf)
            doc.writeTo(FileOutputStream(filePath));
            doc.close()


            mStorage = FirebaseStorage.getInstance().reference
            var file = Uri.fromFile(File(targetPdf))
            val riversRef = mStorage.child("Invoice/${file.lastPathSegment}")


            var uploadTask = riversRef.putFile(file)
            uploadTask.addOnFailureListener {

            }.addOnSuccessListener { taskSnapshot ->

            }


            sendAdminKesbewa(targetPdf, orderRespons)
            sendAdminAshan(targetPdf, orderRespons)
            sendAdminCharith(targetPdf, orderRespons)
            sendUser(targetPdf, orderRespons)
            sendAdminHimanshu(targetPdf, orderRespons)

        } catch (e: Exception) {

        }

    }

    fun sendUser(path: String, orderRespons: OrderRespons) {
        MaildroidX.Builder()
            .smtp("node233.r-usdatacenter.register.lk")
            .smtpUsername("no-reply@kesbewa.com")
            .smtpPassword("]U7~Ruq0V8fV")
            .port("465")
            .type(MaildroidXType.HTML)
            .to(orderRespons.user.user_email.toString())
            .from("no-reply@kesbewa.com")
            .subject(orderRespons.order_code + " order confirmation Invoice")
            .body("invoice")
            .attachment(path)
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
                        "Invoice Email NOT send to user " + errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
            .mail()
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
            .subject(orderRespons.order_code + " order complete Invoice")
            .body("invoice")
            .attachment(path)
            .onCompleteCallback(object : MaildroidX.onCompleteCallback {
                override val timeout: Long = 10000
                override fun onSuccess() {
                    Toast.makeText(
                        requireContext(),
                        "Invoice Email send to Admin",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFail(errorMessage: String) {
                    Toast.makeText(
                        requireContext(),
                        "Invoice Email NOT send to user " + errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
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
            .subject(orderRespons.order_code + " order complete Invoice")
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
            .subject(orderRespons.order_code + " order complete Invoice")
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
            .to("info.kesbewa@gmail.com")
            .from("no-reply@kesbewa.com")
            .subject(orderRespons.order_code + " order complete Invoice")
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
