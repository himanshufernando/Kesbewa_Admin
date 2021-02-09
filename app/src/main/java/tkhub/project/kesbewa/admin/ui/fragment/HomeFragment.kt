package tkhub.project.kesbewa.admin.ui.fragment

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.stream.JsonReader
import kotlinx.android.synthetic.main.dialog_delivery_charges.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tkhub.project.kesbewa.admin.KesbewaAdmin

import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.db.AppDatabase
import tkhub.project.kesbewa.admin.data.models.City_v2
import tkhub.project.kesbewa.admin.data.models.Zone
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.ui.activity.MainActivity
import tkhub.project.kesbewa.admin.ui.adapters.*

/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {


    var citylist: ArrayList<City_v2> = ArrayList<City_v2>()
    lateinit var dialogDeliveryShipping: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
      var  binding = inflater.inflate(R.layout.fragment_home, container, false)

        AppPrefs.setIntKeyValuePrefs(context!!, AppPrefs.KEY_FRAGMENT_ID,2)
        (activity as MainActivity).setDrawer()



        binding.view_pager.adapter = OrdersViewPagerAdapter(this)

        TabLayoutMediator( binding.tabs,binding.view_pager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()


        binding.imageview_navigation.setOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        binding.cl_shpping.setOnClickListener {
            getCitys()

        }



        return   binding
    }


    private fun getTabTitle(position: Int): String? {
        return when (position) {
            NEW_ORDERS_PAGE_INDEX -> getString(R.string.viewpager_new_orders)
            CONFIRMED_ORDERS_PAGE_INDEX -> getString(R.string.viewpager_new_confirmed_orders)
            PACKED_ORDERS_PAGE_INDEX -> getString(R.string.viewpager_packed_orders)
            DELIVERY_ORDERS_PAGE_INDEX -> getString(R.string.viewpager_delivery_orders)
            DELIVERED_ORDERS_PAGE_INDEX -> getString(R.string.viewpager_delivered_orders)
            STORE_ORDERS_PAGE_INDEX -> getString(R.string.viewpager_store_orders)
            else -> null
        }
    }


    private fun dialogDeliveryDetails() {
        dialogDeliveryShipping = Dialog(requireContext())
        dialogDeliveryShipping.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogDeliveryShipping.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogDeliveryShipping.setContentView(R.layout.dialog_delivery_charges)
        dialogDeliveryShipping.setCancelable(true)



        val adapter = ShppingCityAdapter(requireContext(), R.layout.item_auto_complete_text_view, citylist)
        dialogDeliveryShipping.edit_searchaddress.setAdapter(adapter)
        dialogDeliveryShipping.edit_searchaddress.threshold = 1

        dialogDeliveryShipping.edit_searchaddress.setOnItemClickListener() { parent, _, position, id ->
            val selectedCity = parent.adapter.getItem(position) as City_v2?
            dialogDeliveryShipping.edit_searchaddress.setText(selectedCity?.name_en)

            var zoneDAO = AppDatabase.getInstance(requireContext()).zoneDao()

            GlobalScope.launch(Dispatchers.IO) {
                var zone = selectedCity?.area?.let { zoneDAO.getZoneFromArea(it) }
                zone?.let { setShppingData(it) }


            }

        }




        dialogDeliveryShipping.show()

    }
    private fun setShppingData(zone : Zone){
        activity?.runOnUiThread{
            dialogDeliveryShipping.txt_dates.text = zone.delivery_time
            dialogDeliveryShipping.txt_price.text = zone.delivery_charges.toString()+" RS"
        }

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
                dialogDeliveryDetails()
                reader.endArray()

            }
        }
    }


}
