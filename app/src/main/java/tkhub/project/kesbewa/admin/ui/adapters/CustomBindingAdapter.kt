package tkhub.project.kesbewa.admin.ui.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.bumptech.glide.Glide
import tkhub.project.kesbewa.admin.KesbewaAdmin
import tkhub.project.kesbewa.admin.data.models.CartItem
import tkhub.project.kesbewa.admin.data.models.Customer
import tkhub.project.kesbewa.admin.data.models.DeliveryAddress
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs

object CustomBindingAdapter {

    @BindingAdapter("setCurrentItems")
    @JvmStatic
    fun setCurrentItems(view: RecyclerView, cartItems: List<CartItem>?) {
        if (cartItems != null) {
            val historyAdapter = OrdersItemAdapter()
            view.adapter = historyAdapter
            historyAdapter.submitList(cartItems)
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
        var fulladdress = if(!AppPrefs.checkValidString(deliveryAddress.user_address_two!!)){
            (deliveryAddress.user_address_number + " ," +deliveryAddress.user_address_one + ", "+ "\n"
                    + deliveryAddress.user_address_two + ", "+ "\n"
                    + deliveryAddress.user_address_city + ", "+ "\n"
                    + deliveryAddress.user_address_district)
        }else{
            (deliveryAddress.user_address_number + " ," +deliveryAddress.user_address_one + ", "+ "\n"
                    + deliveryAddress.user_address_city + ", "+ "\n"
                    + deliveryAddress.user_address_district)
        }
        view.text = fulladdress
    }

    @BindingAdapter("setDispatch")
    @JvmStatic
    fun setDispatch(view: AppCompatTextView, orderRespons: OrderRespons) {
        if(orderRespons.order_dispatch_type == "STORE"){
            view.text =orderRespons.order_store_location
        }else{
            var deliveryAddress = orderRespons.delivery_address
            var fulladdress = if(!AppPrefs.checkValidString(deliveryAddress.user_address_two!!)){
                (deliveryAddress.user_address_number + " ," +deliveryAddress.user_address_one + ", "+ "\n"
                        + deliveryAddress.user_address_two + ", "+ "\n"
                        + deliveryAddress.user_address_city + ", "+ "\n"
                        + deliveryAddress.user_address_district)
            }else{
                (deliveryAddress.user_address_number + " ," +deliveryAddress.user_address_one + ", "+ "\n"
                        + deliveryAddress.user_address_city + ", "+ "\n"
                        + deliveryAddress.user_address_district)
            }
            view.text = fulladdress
        }


    }




    @BindingAdapter("setDispatchType")
    @JvmStatic
    fun setDispatchType(view: AppCompatTextView, orderRespons: OrderRespons) {

        if(orderRespons.order_dispatch_type == "STORE"){
            view.text = "STORE PICK UP"
        }else{
            view.text = "DELIVERY ADDRESS"
        }

    }



    @BindingAdapter("setCustomerName")
    @JvmStatic
    fun setCustomerName(view: AppCompatTextView, name: String) {
        if(name.length >20){
            view.text=name.substring(0,18)+"..."
        }else{
            view.text=name
        }
    }


    @BindingAdapter("setUserPastOrdersItem")
    @JvmStatic
    fun setUserPastOrdersItem(view: RecyclerView,cartItems: List<CartItem>?) {
        if (cartItems != null) {
            val historyAdapter = CustomerPastOrderItemsAdapter()
            view.adapter = historyAdapter
            historyAdapter.submitList(cartItems)
        }else{
            val historyAdapter = CustomerPastOrderItemsAdapter()
            view.adapter = historyAdapter
            historyAdapter.submitList(emptyList())

        }
    }


    @BindingAdapter("setCustomerOrderStatus")
    @JvmStatic
    fun setCustomerOrderStatus(view: AppCompatTextView,status: Int) {
         var statusInWord=""
        when(status){
             0 -> statusInWord = "New Order"
             1 -> statusInWord = "Confirmed"
             2-> statusInWord = "Packed"
             3-> statusInWord = "In Transit"
             4-> statusInWord = "Delivered"
             5-> statusInWord = "Completed"
            6-> statusInWord = "Reject"
         }
        view.text = statusInWord

    }

    @BindingAdapter("setProductCoverImage")
    @JvmStatic
    fun setProductCoverImage(view: ImageView, pro_cover_img: String) {
        view.visibility= View.VISIBLE

      Glide.with(KesbewaAdmin.applicationContext())
           .load(pro_cover_img)
          .into(view)
    }

}