package tkhub.project.kesbewa.admin.ui.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_map.view.*
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.DeliveryAddress
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.viewmodels.home.HomeViewModels


/**
 * A simple [Fragment] subclass.
 */
class MapFragment : Fragment() {

    private val viewmodel: HomeViewModels by viewModels { HomeViewModels.LiveDataVMFactory }

    lateinit var deliveryAddress: DeliveryAddress


    private val callback = OnMapReadyCallback { googleMap ->

        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                context as Activity,
                R.raw.new_map_style
            )
        );

        val mapLocation : LatLng = LatLng(deliveryAddress.user_lat!!,deliveryAddress.user_lon!!)

        googleMap.addMarker(MarkerOptions().position(mapLocation).title("Current Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapLocation, 17f));



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var layout =inflater.inflate(R.layout.fragment_map, container, false)
        var arg =  arguments?.getString("deliveryAddress")
        deliveryAddress = Gson().fromJson(arg,DeliveryAddress::class.java)


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


        layout.appCompatTextView_address.text = fulladdress


        layout.imageView_google_map.setOnClickListener {

            var uriString = "google.navigation:q=${deliveryAddress.user_lat},${deliveryAddress.user_lon}"
            uriString += "&mode=d"

            val gmmIntentUri = Uri.parse(uriString)
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(activity!!.packageManager) != null) {
                startActivity(mapIntent)
            }
        }

        return layout
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}
