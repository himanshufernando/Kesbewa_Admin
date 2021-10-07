package tkhub.project.kesbewa.admin.ui.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load

import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*
import tkhub.project.kesbewa.admin.KesbewaAdmin
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.*
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import java.text.SimpleDateFormat
import java.util.*


object CustomBindingAdapter {



    @BindingAdapter("setOrderDescriptionExpand")
    @JvmStatic
    fun setOrderDescriptionExpand(view: ConstraintLayout, status: Boolean) {
        if (status) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }



    @BindingAdapter("setProductDescription")
    @JvmStatic
    fun setProductDescription(view: AppCompatTextView, description: String) {

        var discription = ""
        var delimiter = "~"
        val parts = description!!.split(delimiter)
        for (po in parts) {
            discription = "$discription* $po\n\n"
        }
        view.text = discription

    }

    @BindingAdapter("setProductSizeItems")
    @JvmStatic
    fun setProductSizeItems(view: RecyclerView, productSize: ProductsModel?) {
        if (productSize != null) {
            val proAdapter = ProductSizeAdapter()
            view.adapter = proAdapter
            proAdapter.submitList(productSize.size)
        }
    }


    @BindingAdapter("setCurrentItems")
    @JvmStatic
    //  fun setCurrentItems(view: RecyclerView, cartItems: List<CartItem>?) {
    fun setCurrentItems(view: RecyclerView, orderrespons: OrderRespons?) {
        if (orderrespons != null) {
            val historyAdapter = OrdersItemAdapter()
            view.adapter = historyAdapter
            historyAdapter.submitList(orderrespons.itemlist)
        }
    }
    @BindingAdapter("cart_image")
    @JvmStatic
    fun cart_img(view: ImageView, pro_cover_img: String?) {
        try {
            Glide.with(KesbewaAdmin.applicationContext())
                .load(pro_cover_img)
                .apply(RequestOptions.centerCropTransform())
                .override(800, 800)
                .format(DecodeFormat.PREFER_RGB_565)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_no_image)
                .placeholder(R.drawable.ic_no_image)
                .into(view)

        } catch (e: Exception) {
            Glide.with(KesbewaAdmin.applicationContext())
                .load(R.drawable.ic_no_image)
                .error(R.drawable.ic_no_image)
                .into(view)

        }
    }

    @BindingAdapter("setOrderItemImage")
    @JvmStatic
    fun setOrderItemImage(view: ImageView, pro_cover_img: String?) {
        view.scaleType = ImageView.ScaleType.CENTER_CROP
        view.load(pro_cover_img)
    }

    @BindingAdapter("setDeliveryAddress")
    @JvmStatic
    fun setDeliveryAddress(view: AppCompatTextView, deliveryAddress: DeliveryAddress) {
        var fulladdress = if (!AppPrefs.checkValidString(deliveryAddress.user_address_two!!)) {
            (deliveryAddress.user_address_number + " ," + deliveryAddress.user_address_one + ", " + "\n"
                    + deliveryAddress.user_address_two + ", " + "\n"
                    + deliveryAddress.user_address_city + ", " + "\n"
                    + deliveryAddress.user_address_district)
        } else {
            (deliveryAddress.user_address_number + " ," + deliveryAddress.user_address_one + ", " + "\n"
                    + deliveryAddress.user_address_city + ", " + "\n"
                    + deliveryAddress.user_address_district)
        }
        view.text = fulladdress
    }

    @BindingAdapter("setDispatch")
    @JvmStatic
    fun setDispatch(view: AppCompatTextView, orderRespons: OrderRespons) {
        if (orderRespons.order_dispatch_type == "STORE") {
            view.text = orderRespons.order_store_location
        } else {
            var deliveryAddress = orderRespons.delivery_address
            var fulladdress = if (!AppPrefs.checkValidString(deliveryAddress.user_address_two!!)) {
                (deliveryAddress.user_address_number + " ," + deliveryAddress.user_address_one + ", " + "\n"
                        + deliveryAddress.user_address_two + ", " + "\n"
                        + deliveryAddress.user_address_city + ", " + "\n"
                        + deliveryAddress.user_address_district)
            } else {
                (deliveryAddress.user_address_number + " ," + deliveryAddress.user_address_one + ", " + "\n"
                        + deliveryAddress.user_address_city + ", " + "\n"
                        + deliveryAddress.user_address_district)
            }
            view.text = fulladdress
        }


    }


    @BindingAdapter("setOrderDetailsExpand")
    @JvmStatic
    fun setOrderDetailsExpand(view: RecyclerView, status: Boolean) {
        if (status) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    @BindingAdapter("setDispatchType")
    @JvmStatic
    fun setDispatchType(view: AppCompatTextView, orderRespons: OrderRespons) {

        if (orderRespons.order_dispatch_type == "STORE") {
            view.text = "STORE PICK UP"
        } else {
            view.text = "DELIVERY ADDRESS"
        }

    }


    @BindingAdapter("setCustomerName")
    @JvmStatic
    fun setCustomerName(view: AppCompatTextView, name: String) {
        if (name.length > 20) {
            view.text = name.substring(0, 18) + "..."
        } else {
            view.text = name
        }
    }


    @BindingAdapter("setUserPastOrdersItem")
    @JvmStatic
    fun setUserPastOrdersItem(view: RecyclerView, cartItems: List<CartItem>?) {
        if (cartItems != null) {
            val historyAdapter = CustomerPastOrderItemsAdapter()
            view.adapter = historyAdapter
            historyAdapter.submitList(cartItems)
        } else {
            val historyAdapter = CustomerPastOrderItemsAdapter()
            view.adapter = historyAdapter
            historyAdapter.submitList(emptyList())

        }
    }


    @BindingAdapter("setCustomerOrderStatus")
    @JvmStatic
    fun setCustomerOrderStatus(view: AppCompatTextView, status: Int) {
        var statusInWord = ""
        when (status) {
            0 -> statusInWord = "New Order"
            1 -> statusInWord = "Confirmed"
            2 -> statusInWord = "Packed"
            3 -> statusInWord = "In Transit"
            4 -> statusInWord = "Delivered"
            5 -> statusInWord = "Completed"
            6 -> statusInWord = "Reject"
        }
        view.text = statusInWord

    }

    @BindingAdapter("setProductCoverImage")
    @JvmStatic
    fun setProductCoverImage(view: ImageView, pro_cover_img: String) {
        view.visibility = View.VISIBLE

        Glide.with(KesbewaAdmin.applicationContext())
            .load(pro_cover_img)
            .into(view)
    }


    @BindingAdapter("setOrderDate")
    @JvmStatic
    fun setOrderDate(view: AppCompatTextView, date: Long) {
        val cal: Calendar = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = date
        val sdf = SimpleDateFormat("dd MMMM, yyyy")
        view.text = sdf.format(cal.time)

    }

    @BindingAdapter("setTimeStampToString")
    @JvmStatic
    fun setTimeStampToString(view: TextView, timeStamp: Long) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        view.text = sdf.format(timeStamp)
    }


    @BindingAdapter("setPromoType")
    @JvmStatic
    fun setPromoType(view: AppCompatTextView, orderRespons: OrderRespons) {
        var type = ""
        if (orderRespons.order_discount > 0) {
            when (orderRespons.order_promo.promocode_type_code) {
                "DVW" -> type = "(Wave off from delivery charges)"
                "VW" -> type = "(Wave off from total value)"
                "TD" -> type = "(Discount from total value)"
                "DD" -> type = "(Discount from delivery charges)"
            }

        }

        view.text = type

    }


    @BindingAdapter("setOrderNote")
    @JvmStatic
    fun setOrderNote(view: AppCompatTextView, orderRespons: OrderRespons) {
        if ((orderRespons.order_note == "null") || (orderRespons.order_note.isEmpty())) {
            view.text = "No note for this order"
        } else {
            view.text = orderRespons.order_note
        }

    }


}