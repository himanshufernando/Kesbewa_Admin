package tkhub.project.kesbewa.admin.ui.fragment


import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.JsonReader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import coil.api.load
import com.google.firebase.database.*
import id.ionbit.ionalert.IonAlert

import kotlinx.android.synthetic.main.dialog_products.*


import kotlinx.android.synthetic.main.fragment_invoice.*
import kotlinx.android.synthetic.main.fragment_invoice.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tkhub.project.kesbewa.admin.KesbewaAdmin
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.db.AppDatabase
import tkhub.project.kesbewa.admin.data.models.*
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.ui.activity.MainActivity
import tkhub.project.kesbewa.admin.ui.adapters.AdminUsersAdapter
import tkhub.project.kesbewa.admin.ui.adapters.CartItemAdapter
import tkhub.project.kesbewa.admin.ui.adapters.InvoiceProductsAdapter
import tkhub.project.kesbewa.admin.ui.adapters.ShppingCityAdapter
import tkhub.project.kesbewa.admin.viewmodels.products.ProductsViewModels
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class InvoiceFragment : Fragment(), View.OnClickListener {

    var citylist: ArrayList<City_v2> = ArrayList<City_v2>()
    private val adapter = InvoiceProductsAdapter()
    private val adapterAddedItems = CartItemAdapter()


    lateinit var dialogProducts: Dialog
    var alertDialog: AlertDialog? = null
    private val viewmodel: ProductsViewModels by viewModels { ProductsViewModels.LiveDataVMFactory }
    lateinit var root: View

    var productSelectedSize = ""
    var dispatchType = ""
    var storeLocation = ""

    var selectedCityName = ""
    var selectedCityDistrict = ""
    var selectedCityProvinces = ""

    var totitemCount = 0
    var discount = 0.0
    var delivery = 0.0
    var subtotal = 0.0

    var productlist: ArrayList<CartItem> = ArrayList<CartItem>()


    var database: FirebaseDatabase? = FirebaseDatabase.getInstance()
    var orderRef: DatabaseReference? = database?.getReference("KOrders")
    var adminUsersRef: DatabaseReference? = database?.getReference("AdminUsers")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_invoice, container, false)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppPrefs.setIntKeyValuePrefs(context!!, AppPrefs.KEY_FRAGMENT_ID, 7)

        root.recyclerView_cart_items.adapter = adapterAddedItems


        val adapter =
            ShppingCityAdapter(requireContext(), R.layout.item_auto_complete_text_view, citylist)
        autocompletetextview_cityq.setAdapter(adapter)
        autocompletetextview_cityq.threshold = 1

        var delivery_charges = 0.0


        autocompletetextview_cityq.setOnItemClickListener() { parent, _, position, id ->
            var selectedCity = (parent.adapter.getItem(position) as City_v2?)!!
            selectedCityName = selectedCity.name_en
            autocompletetextview_cityq.setText(selectedCity?.name_en)
            when (selectedCity?.area) {
                "A" -> {
                    delivery_charges = 250.00
                }
                "B" -> {
                    delivery_charges = 200.00
                }
                "C" -> {
                    delivery_charges = 300.00
                }
                "D" -> {
                    delivery_charges = 300.00
                }
                "E" -> {
                    delivery_charges = 350.00
                }

            }

            edittext_delivery.setText(delivery_charges.toString())

        }


        edittext_delivery.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

                setFinalTotal()
            }
        })


        edittext_6s.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {

                setFinalTotal()
            }
        })


        imageview_navigation_product.setOnClickListener {
            (activity as MainActivity).openDrawer()
        }


        radiogroup_dispatch_type_invoice.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.store -> {
                    cl_dispatch_store.visibility = View.VISIBLE
                    cl_delivery_details.visibility = View.GONE
                    dispatchType = "STORE"
                }

                R.id.delivery -> {
                    cl_dispatch_store.visibility = View.GONE
                    cl_delivery_details.visibility = View.VISIBLE
                    dispatchType = "DELIVERY"
                    cl_delivery_details_two.visibility = View.VISIBLE
                }

            }

        }


        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.negombo -> {
                    storeLocation = "NEGOMBO"
                }

            }

        }



        btn_save.setOnClickListener {
            dialogProductAdd()
        }



        textView_submit.setOnClickListener {


            when {
                (edittext_1s.text.toString().isNullOrEmpty()) -> {
                    Toast.makeText(
                        requireContext(),
                        "මන් බුතයෙක් ට ද Invoice කරන්නෙ, නමක් ගහපන් බල්ලො !!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                (edittext_2s.text.toString().isNullOrEmpty()) -> {
                    Toast.makeText(
                        requireContext(),
                        "Contact Number එක නෑතුව තොගෙ අහවල් එකට කත කරල ද customer ව contact කරන්නෙ !!",
                        Toast.LENGTH_LONG
                    ).show()

                }

                (edittext_2s.text.toString().length != 10) -> {
                    Toast.makeText(
                        requireContext(),
                        "Contact Number එකෙ වෑරදි අයේ check කරපන් !!",
                        Toast.LENGTH_LONG
                    ).show()

                }
                (edittext_3s.text.toString().isNullOrEmpty()) -> {
                    Toast.makeText(
                        requireContext(),
                        "Email එකත් ඉල්ලලා දපාන් Invoices යවන්න ඔන !!",
                        Toast.LENGTH_LONG
                    ).show()
                }


                (!validateEmailAddress(edittext_3s.text.toString())) -> {
                    Toast.makeText(
                        requireContext(),
                        "Email address එකෙ වෑරදි අයේ check කරපන් !!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                (dispatchType == "") -> {
                    Toast.makeText(
                        requireContext(),
                        "Dispatch Type එකත් දපාන්  !!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                ((dispatchType == "STORE").and(storeLocation == "")) -> {
                    Toast.makeText(
                        requireContext(),
                        "Store Location එකත් දපන්,දෑනට තියෙන්නෙ Negombo විතරයි නෙ එක select කරපන්  !!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                ((dispatchType == "DELIVERY").and(
                    edittext_15q.text.toString().isNullOrEmpty()
                )) -> {
                    Toast.makeText(
                        requireContext(),
                        "Addres Number එකත් දපන් !!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                ((dispatchType == "DELIVERY").and(edittext_9q.text.toString().isNullOrEmpty())) -> {
                    Toast.makeText(
                        requireContext(),
                        "පාරක හරි lane එකෙ හරි නම දපන් !!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                ((dispatchType == "DELIVERY").and(edittext_9q.text.toString().isNullOrEmpty())) -> {
                    Toast.makeText(
                        requireContext(),
                        "පාරක හරි lane එකෙ හරි නම දපන් !!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                ((dispatchType == "DELIVERY").and(selectedCityName == "")) -> {
                    Toast.makeText(
                        requireContext(),
                        "City එක Select කරපන්,type කරලා එන list එකෙන් select කරන්න ඔනෙ !!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                (productlist.isEmpty()) -> {
                    Toast.makeText(
                        requireContext(),
                        "Product නෑතුව invoice හදන්න උබට පිස්සුද බන් !!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {

                    layout_loading_invoice.visibility = View.VISIBLE

                    var deliveryAddress = DeliveryAddress()
                    var deliveryCharges = 0.0
                    if (dispatchType == "DELIVERY") {
                        deliveryAddress.apply {
                            address_id = genarateUniqCode().toString()
                            user_address_number = edittext_15q.text.toString()
                            user_address_one = edittext_9q.text.toString()
                            user_address_two = edittext_10q.text.toString()
                            user_address_city = selectedCityName
                            user_address_district = selectedCityDistrict
                            user_address_province = selectedCityProvinces
                        }

                        try {
                            deliveryCharges = edittext_delivery.text.toString().toDouble()
                        } catch (num: NumberFormatException) {

                        }

                    }


                    var usercode = genarateUniqCode().toString()
                    var customer = Customer().apply {
                        user_id = usercode
                        user_name = edittext_1s.text.toString()
                        user_phone = edittext_2s.text.toString()
                        user_email = edittext_3s.text.toString()
                        user_type = 2
                    }

                    var orderDetails = OrderRespons()
                    orderDetails.delivery_address = deliveryAddress
                    orderDetails.itemlist = productlist
                    orderDetails.order_android_id = getAndroidid()
                    orderDetails.order_date = 44454
                    orderDetails.order_delivery_chargers = deliveryCharges
                    orderDetails.order_discount = discount
                    orderDetails.order_dispatch_type = dispatchType
                    orderDetails.order_status = "0"
                    orderDetails.order_status_code = 0
                    orderDetails.order_store_location = storeLocation
                    orderDetails.order_subtotal_total = subtotal
                    orderDetails.order_total_price = ((subtotal - discount) + delivery)
                    orderDetails.order_total_qty = totitemCount
                    orderDetails.order_user_id = usercode
                    orderDetails.user = customer


                    var adminUsersCode = genarateUniqCode().toString()

                   var admin =  AppPrefs.getUserPrefs(requireContext(), AppPrefs.KEY_USER)
                    val c = Calendar.getInstance().time
                    val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())









                    orderRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val value = dataSnapshot.childrenCount

                            var orderCode = "AD$value-" + ((1..1000).random().toString())
                            orderDetails.order_code = orderCode

                            var unxId = genarateUniqCode()
                            orderDetails.order_id = unxId.toString()+"_"+orderCode
                            orderRef?.child(unxId.toString()+"_"+orderCode)?.setValue(orderDetails)

                            layout_loading_invoice.visibility = View.GONE


                            var adminUsersRespons = AdminUsersRespons().apply {
                                admin_user_id = adminUsersCode
                                admin_user =admin.admin_name
                                admin_user_date=df.format(c)
                                delivery_address =deliveryAddress
                                admin_user_user =customer

                            }

                           adminUsersRef?.child(unxId.toString()+"_"+orderCode)?.setValue(adminUsersRespons)


                            IonAlert(requireContext(), IonAlert.SUCCESS_TYPE)
                                .setTitleText("Successful")
                                .setContentText("Order has been placed successfully !")
                                .setConfirmText("OK")
                                .setConfirmClickListener(IonAlert.ClickListener { sDialog ->
                                    sDialog.dismissWithAnimation()
                                    claerdata()

                                })
                                .show()

                        }

                        override fun onCancelled(error: DatabaseError) {
                            layout_loading_invoice.visibility = View.GONE
                            IonAlert(requireContext(), IonAlert.ERROR_TYPE)
                                .setTitleText("Fail!!")
                                .setContentText("Order adding fail due to "+error.message)
                                .setConfirmText("OK")
                                .setConfirmClickListener(IonAlert.ClickListener { sDialog ->
                                    sDialog.dismissWithAnimation()
                                })
                                .show()
                        }
                    })

                }

            }


        }


        cl_delivery_address_expand.setOnClickListener {
            if (cl_delivery_details_two.isVisible) {
                cl_delivery_details_two.visibility = View.GONE
                var addresse = edittext_15q.text.toString() + edittext_9q.text.toString()

                if (!addresse.isNullOrEmpty()) {
                    "$addresse..."
                }
                textview_33q.text = "Deliver Address :$addresse"
            } else {
                cl_delivery_details_two.visibility = View.VISIBLE

                textview_33q.text = "Deliver Address"
            }
        }




        getAdminUsers()

        edittext_1s.setOnItemClickListener() { parent, _, position, id ->
            val selectedUser = parent.adapter.getItem(position) as AdminUsersRespons?
             edittext_1s.setText(selectedUser?.admin_user_user!!.user_name)
            edittext_2s.setText(selectedUser?.admin_user_user!!.user_phone)
            edittext_3s.setText(selectedUser?.admin_user_user!!.user_email)

            edittext_15q.setText(selectedUser?.delivery_address.user_address_number)
            edittext_9q.setText(selectedUser?.delivery_address.user_address_one)
            edittext_10q.setText(selectedUser?.delivery_address.user_address_two)
            autocompletetextview_cityq.setText(selectedUser?.delivery_address.user_address_city)
            autocompletetextview_cityq.showDropDown()

        }



    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)

    }


    override fun onResume() {
        super.onResume()

        getCitys()


    }

    override fun onStop() {
        super.onStop()

        viewmodel.productsList.removeObservers(viewLifecycleOwner)
    }


    override fun onPause() {
        super.onPause()

    }

    override fun onClick(v: View) {


    }

    fun claerdata(){
        edittext_1s.setText("")
        edittext_2s.setText("")
        edittext_3s.setText("")
        edittext_6s.setText("")

        radiogroup_dispatch_type_invoice.clearCheck()
        radioGroup.clearCheck()


        edittext_15q.setText("")
        edittext_9q.setText("")
        edittext_10q.setText("")
        edittext_6s.setText("")


        textview_items.text = ""
        textview_discount.text = ""
        textview_delivery.text = ""
        textview_subtotal.text = ""
        textview_needtopay.text = ""

        productlist.clear()
        adapterAddedItems.notifyDataSetChanged()

    }

    fun productsListObserver() {
        viewmodel.productsList.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is KesbewaResult.Success -> {
                    var list = response.data
                    var sortList = list.sortedBy { it.pro_sort }


                    adapter.submitList(sortList)
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


    private fun dialogProductAdd() {
        cl_delivery_details_two.visibility = View.GONE
        var addresse = edittext_15q.text.toString() + edittext_9q.text.toString()

        if (!addresse.isNullOrEmpty()) {
            "$addresse..."
        }
        textview_33q.text = "Deliver Address :$addresse"





        setAddedItems()

        dialogProducts = Dialog(requireContext())
        dialogProducts.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogProducts.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogProducts.setContentView(R.layout.dialog_products)
        dialogProducts.setCancelable(true)


        var prodSizeList = ArrayList<ProductSize>()

        var selectProduct = Products()



        dialogProducts.recyclerView_add_products.adapter = adapter


        if (!viewmodel.productsList.hasObservers()) {
            productsListObserver()
        }
        viewmodel.getProducts()

        adapter.setOnItemClickListener(object : InvoiceProductsAdapter.ClickListener {
            override fun onClick(product: Products, aView: View) {

                selectProduct = product



                dialogProducts.cl_recyclerView_add_products.visibility = View.GONE
                dialogProducts.cl_selectedProduct_products.visibility = View.VISIBLE


                dialogProducts.img.scaleType = ImageView.ScaleType.CENTER_CROP
                dialogProducts.img.load(product.pro_cover_img)


                dialogProducts.textview_invoice_pro_name.text = product.pro_name
                dialogProducts.textview_invoice_pro_code.text = product.pro_code
                dialogProducts.textview_invoice_pro_price_selling.text =
                    "Selling " + product.pro_price.toString() + " LKR"
                dialogProducts.textview_regular.text =
                    "Regular " + product.pro_price_regular.toString() + " LKR"
                dialogProducts.textview_invoice_pro_itemcost.text =
                    "Per Item Cost " + product.pro_per_item_cost.toString() + " LKR"

                dialogProducts.edt_price.setText(product.pro_price.toString())


                dialogProducts.textview__size_s.visibility = View.GONE
                dialogProducts.textview__size_m.visibility = View.GONE
                dialogProducts.textview__size_l.visibility = View.GONE


                val ref = database?.getReference("Products")!!.child(product.pro_id.toString())
                    .child("size")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var childsize = dataSnapshot.children.count()

                        for (postSnapshot in dataSnapshot.children) {
                            val post = postSnapshot.getValue(ProductSize::class.java)

                            prodSizeList.add(post!!)

                            if (post?.size.equals("S")) {
                                dialogProducts.textview__size_s.visibility = View.VISIBLE

                                if (childsize == 1) {
                                    productSelectedSize = "S"
                                    dialogProducts.textview__size_s.setBackgroundResource(R.color.colorRed)
                                    dialogProducts.textview__size_s.setTextColor(Color.parseColor("#FFFFFF"))
                                }

                            }

                            if (post?.size.equals("M")) {
                                dialogProducts.textview__size_m.visibility = View.VISIBLE

                                if (childsize == 1) {
                                    productSelectedSize = "M"
                                    dialogProducts.textview__size_m.setBackgroundResource(R.color.colorRed)
                                    dialogProducts.textview__size_m.setTextColor(Color.parseColor("#FFFFFF"))
                                }
                            }

                            if (post?.size.equals("L")) {
                                dialogProducts.textview__size_l.visibility = View.VISIBLE

                                if (childsize == 1) {
                                    productSelectedSize = "L"
                                    dialogProducts.textview__size_l.setBackgroundResource(R.color.colorRed)
                                    dialogProducts.textview__size_l.setTextColor(Color.parseColor("#FFFFFF"))
                                }
                            }

                        }


                    }

                    override fun onCancelled(error: DatabaseError) {


                    }
                })


            }
        })





        dialogProducts.textview_add.setOnClickListener {
            var proQTY = 0.0
            var proPrice = 0.0

            try {
                proQTY = dialogProducts.edt_qty.text.toString().toDouble()
            } catch (num: NumberFormatException) {
            }

            try {
                proPrice = dialogProducts.edt_qty.text.toString().toDouble()
            } catch (num: NumberFormatException) {
            }


            when {
                (dialogProducts.edt_qty.text.toString().isNullOrEmpty()) -> {
                    Toast.makeText(
                        requireContext(),
                        "Quantity එකක් දාපන් හුත්තො !!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                (dialogProducts.edt_price.text.toString().isNullOrEmpty()) -> {
                    Toast.makeText(requireContext(), "Price එකක් දාපන් පකො !!", Toast.LENGTH_LONG)
                        .show()

                }
                (productSelectedSize.isEmpty().or(productSelectedSize == "")) -> {
                    Toast.makeText(requireContext(), "Size එකක් දාපන්  !!", Toast.LENGTH_LONG)
                        .show()

                }
                (dialogProducts.edt_qty.text.toString() == "0") -> {
                    Toast.makeText(
                        requireContext(),
                        "Quantity එකෙ  0 දන්න බෑහෑ වෙසියෙ !!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                (dialogProducts.edt_price.text.toString().isNullOrEmpty()) -> {
                    Toast.makeText(requireContext(), "Price එකක් දාපන් පකො !!", Toast.LENGTH_LONG)
                        .show()

                }

                else -> {

                    var key = genarateUniqCode()

                    var itemPrice = dialogProducts.edt_price.text.toString().toDouble()
                    var qty = dialogProducts.edt_qty.text.toString().toInt()
                    var tot = qty * itemPrice

                    var proName = selectProduct.pro_name
                    if (dialogProducts.checkBox.isChecked) {
                        proName = selectProduct.pro_name + " with customized laser engraving"
                    }


                    var cartitem = CartItem().apply {
                        cart_id = key
                        pro_id = selectProduct.pro_id
                        pro_name = proName
                        pro_price = itemPrice
                        pro_size = productSelectedSize
                        pro_image = selectProduct.pro_cover_img
                        pro_colour = ""
                        pro_colour_code = ""
                        pro_total_qty = dialogProducts.edt_qty.text.toString().toInt()
                        pro_total_price = tot
                        cartStatus = true
                        pro_code = selectProduct.pro_code
                        pro_weight = selectProduct.pro_weight
                        pro_stock = selectProduct.pro_stock
                    }
                    productlist.add(cartitem)

                    adapterAddedItems.notifyDataSetChanged()
                    dialogProducts.dismiss()


                    setFinalTotal()


                    Toast.makeText(
                        requireContext(),
                        "Invoice එකට product එකක් add වුනා !!!",
                        Toast.LENGTH_LONG
                    ).show()


                }

            }


        }

        dialogProducts.textview_cancel.setOnClickListener {

            dialogProducts.cl_recyclerView_add_products.visibility = View.VISIBLE
            dialogProducts.cl_selectedProduct_products.visibility = View.GONE
        }


        dialogProducts.textview__size_s.setOnClickListener {
            productSelectedSize = "S"

            dialogProducts.textview__size_s.setBackgroundResource(R.color.colorRed)
            dialogProducts.textview__size_s.setTextColor(Color.parseColor("#FFFFFF"))

            dialogProducts.textview__size_m.setBackgroundResource(R.color.textcolor5)
            dialogProducts.textview__size_m.setTextColor(Color.parseColor("#000000"))

            dialogProducts.textview__size_l.setBackgroundResource(R.color.textcolor5)
            dialogProducts.textview__size_l.setTextColor(Color.parseColor("#000000"))

            for (item in prodSizeList) {

                if (item.size == "S") {
                    dialogProducts.edt_price.setText(item.price.toString())
                }


            }


        }

        dialogProducts.textview__size_m.setOnClickListener {
            productSelectedSize = "M"

            dialogProducts.textview__size_m.setBackgroundResource(R.color.colorRed)
            dialogProducts.textview__size_m.setTextColor(Color.parseColor("#FFFFFF"))

            dialogProducts.textview__size_s.setBackgroundResource(R.color.textcolor5)
            dialogProducts.textview__size_s.setTextColor(Color.parseColor("#000000"))

            dialogProducts.textview__size_l.setBackgroundResource(R.color.textcolor5)
            dialogProducts.textview__size_l.setTextColor(Color.parseColor("#000000"))

            for (item in prodSizeList) {

                if (item.size == "M") {
                    dialogProducts.edt_price.setText(item.price.toString())
                }


            }

        }

        dialogProducts.textview__size_l.setOnClickListener {
            productSelectedSize = "L"


            dialogProducts.textview__size_l.setBackgroundResource(R.color.colorRed)
            dialogProducts.textview__size_l.setTextColor(Color.parseColor("#FFFFFF"))

            dialogProducts.textview__size_s.setBackgroundResource(R.color.textcolor5)
            dialogProducts.textview__size_s.setTextColor(Color.parseColor("#000000"))

            dialogProducts.textview__size_m.setBackgroundResource(R.color.textcolor5)
            dialogProducts.textview__size_m.setTextColor(Color.parseColor("#000000"))


            for (item in prodSizeList) {
                if (item.size == "L") {
                    dialogProducts.edt_price.setText(item.price.toString())
                }
            }


        }


        dialogProducts.show()
    }


    private fun setAddedItems() {

        adapterAddedItems.submitList(productlist)

        adapterAddedItems.setOnItemClickListener(object : CartItemAdapter.ClickListener {
            override fun onClick(cartitemsellect: CartItem, aView: View, position: Int) {

                when (aView.id) {
                    R.id.textview_btn_remove -> {
                        productlist.remove(cartitemsellect)
                        adapterAddedItems.notifyDataSetChanged()
                        setFinalTotal()
                    }

                    R.id.cl_qty_minues -> {
                        var currentqty = cartitemsellect.pro_total_qty
                        if (currentqty != 1) {
                            productlist.remove(cartitemsellect)
                            currentqty = currentqty?.minus(1)
                            cartitemsellect.pro_total_qty = currentqty
                            cartitemsellect.pro_total_price = (cartitemsellect.pro_price?.times(
                                currentqty!!
                            ))

                            productlist.add(cartitemsellect)
                            adapterAddedItems.notifyItemChanged(position)
                            setFinalTotal()
                        }


                    }

                    R.id.cl_qty_add -> {
                        var currentqty = cartitemsellect.pro_total_qty
                        productlist.remove(cartitemsellect)
                        currentqty = currentqty?.plus(1)
                        cartitemsellect.pro_total_qty = currentqty
                        cartitemsellect.pro_total_price = (cartitemsellect.pro_price?.times(
                            currentqty!!
                        ))
                        productlist.add(cartitemsellect)
                        adapterAddedItems.notifyItemChanged(position)
                        setFinalTotal()
                    }


                }

            }
        })


    }


    fun getCitys() {

        KesbewaAdmin.applicationContext().assets.open("cities_v2.json").use { inputStream ->
            JsonReader(inputStream.reader()).use { reader ->
                reader.beginArray()
                while (reader.hasNext()) {
                    var _id: String = ""
                    var _district_id: String = ""
                    var _name_en: String = ""
                    var _postcode: String = ""
                    var _latitude: String = ""
                    var _longitude: String = ""
                    var _area: String = ""
                    reader.beginObject()
                    while (reader.hasNext()) {
                        val name = reader.nextName()
                        when (name) {
                            "id" -> {
                                _id = reader.nextString()
                            }
                            "district_id" -> {
                                _district_id = reader.nextString()
                            }
                            "name_en" -> {
                                _name_en = reader.nextString()
                            }
                            "postcode" -> {
                                _postcode = reader.nextString()
                            }
                            "latitude" -> {
                                _latitude = reader.nextString()
                            }
                            "longitude" -> {
                                _longitude = reader.nextString()
                            }
                            "area" -> {
                                _area = reader.nextString()
                            }
                            else -> {
                                reader.skipValue()
                            }
                        }
                    }
                    reader.endObject()
                    var city = City_v2().apply {
                        id = _id
                        district_id = _district_id
                        name_en = _name_en
                        postcode = _postcode
                        latitude = _latitude
                        longitude = _longitude
                        area = _area

                    }
                    citylist.add(city)
                }


                reader.endArray()

            }
        }
    }

    fun genarateUniqCode(): Long {
        val c: Calendar = Calendar.getInstance()
        var numberFromTime =
            c.get(Calendar.DATE).toString() +
                    c.get(Calendar.HOUR).toString() +
                    c.get(Calendar.MINUTE).toString() +
                    c.get(Calendar.SECOND).toString() +
                    c.get(Calendar.MILLISECOND).toString() + ((1..100000).random()).toString()

        return numberFromTime.toLong()
    }

    fun validateEmailAddress(email: String): Boolean {
        return Pattern.compile(
            "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                    + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
        ).matcher(email).matches()
    }

    fun getAndroidid(): String {
        return Settings.Secure.getString(
            requireContext().contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    fun setFinalTotal() {

        try {
            discount = edittext_6s.text.toString().toDouble()
        } catch (num: NumberFormatException) {
        }
        try {
            delivery = edittext_delivery.text.toString().toDouble()
        } catch (num: NumberFormatException) {
        }

        totitemCount = 0
        subtotal = 0.0
        var totKellow = 0

        for (item in productlist) {
            totitemCount += item.pro_total_qty!!
            subtotal += item.pro_total_price!!


            println("ssssssssssssssssssssssssssssss delivery pro_weight   "+item.pro_weight)
            println("ssssssssssssssssssssssssssssss delivery pro_total_qty   "+ item.pro_total_qty)


            var itemkm = item.pro_weight!!* item.pro_total_qty!!
            totKellow += itemkm.toInt()

            println("ssssssssssssssssssssssssssssss delivery totKellow   "+ totKellow)

        }

        var deliverChargesPerkellow  =0.0
        var additnalKg = totKellow/1000.toInt()


        if(additnalKg!=0){
            deliverChargesPerkellow = (additnalKg * 50).toDouble()
        }


        var finalDelivery = 0.0
        if(delivery !=0.0){
            finalDelivery = delivery + deliverChargesPerkellow
        }


        textview_items.text = totitemCount.toString()
        textview_discount.text = discount.toString()
        textview_delivery.text = finalDelivery.toString()
        textview_subtotal.text = subtotal.toString()
        textview_needtopay.text = ((subtotal - discount) + delivery).toString()
    }

    private fun getAdminUsers(){

        println("ssssssssssssssssssssssssssssssssss getAdminUsers ")
        layout_loading_invoice.visibility = View.VISIBLE

        adminUsersRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var list = ArrayList<AdminUsersRespons>()
                for (postSnapshot in dataSnapshot.children) {
                    val post = postSnapshot.getValue(AdminUsersRespons::class.java)
                    list.add(post!!)

                    println("ssssssssssssssssssssssssssssssssss post "+post)

                }


                println("ssssssssssssssssssssssssssssssssss AdminUsersAdapter ")
                val adapter = AdminUsersAdapter(requireContext(), R.layout.item_auto_complete_text_view, list)
                edittext_1s.setAdapter(adapter)
                edittext_1s.threshold = 1
                layout_loading_invoice.visibility = View.GONE



            }

            override fun onCancelled(error: DatabaseError) {

                println("ssssssssssssssssssssssssssssssssss onCancelled "+error)
            }
        })

    }


}