package tkhub.project.kesbewa.admin.ui.adapters

import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import tkhub.project.kesbewa.admin.data.models.CartItem
import tkhub.project.kesbewa.admin.data.models.Customer
import tkhub.project.kesbewa.admin.data.models.DeliveryAddress
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
             1 -> statusInWord = "New Order"
             2 -> statusInWord = "Confirmed"
             3-> statusInWord = "Packed"
             4-> statusInWord = "In Transit"
             5-> statusInWord = "Delivered"
             6-> statusInWord = "Completed"
         }
        view.text = statusInWord

    }
}