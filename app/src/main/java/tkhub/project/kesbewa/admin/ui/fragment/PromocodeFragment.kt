package tkhub.project.kesbewa.admin.ui.fragment

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import id.ionbit.ionalert.IonAlert
import kotlinx.android.synthetic.main.fragment_promocode.*
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.Promo
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.services.network.InternetConnection
import tkhub.project.kesbewa.admin.ui.activity.MainActivity
import java.util.*


class PromocodeFragment : Fragment(), View.OnClickListener {


    var promoTypeCode = ""
    var createdCode = ""
    var promoType = 0
    var promoValue = 0.0

    companion object {

        var database: FirebaseDatabase? = FirebaseDatabase.getInstance()
        var promoCode: DatabaseReference? = database?.getReference("PromoCodes")


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_promocode, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppPrefs.setIntKeyValuePrefs(context!!, AppPrefs.KEY_FRAGMENT_ID, 6)

        textview_DD.setOnClickListener(this)
        textview_TD.setOnClickListener(this)
        textview_DVW.setOnClickListener(this)
        textview_VW.setOnClickListener(this)
        textview_createcode.setOnClickListener(this)
        imageview_navigation_past.setOnClickListener(this)


    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)

    }


    override fun onResume() {
        super.onResume()

    }

    override fun onStop() {


        super.onStop()
    }

    override fun onPause() {
        super.onPause()

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.textview_DD -> {
                promoTypeCode = "DD"
                promoType = 1
                textview_DD.setBackgroundResource(R.color.colorRed)
                textview_DD.setTextColor(Color.parseColor("#FFFFFF"))


                textview_TD.setBackgroundResource(R.color.textcolor5)
                textview_TD.setTextColor(Color.parseColor("#000000"))

                textview_DVW.setBackgroundResource(R.color.textcolor5)
                textview_DVW.setTextColor(Color.parseColor("#000000"))

                textview_VW.setBackgroundResource(R.color.textcolor5)
                textview_VW.setTextColor(Color.parseColor("#000000"))


                textview_1.text = "Set Discount value for delivery charges"


            }

            R.id.textview_TD -> {
                promoTypeCode = "TD"
                promoType = 2
                textview_TD.setBackgroundResource(R.color.colorRed)
                textview_TD.setTextColor(Color.parseColor("#FFFFFF"))


                textview_DD.setBackgroundResource(R.color.textcolor5)
                textview_DD.setTextColor(Color.parseColor("#000000"))

                textview_DVW.setBackgroundResource(R.color.textcolor5)
                textview_DVW.setTextColor(Color.parseColor("#000000"))

                textview_VW.setBackgroundResource(R.color.textcolor5)
                textview_VW.setTextColor(Color.parseColor("#000000"))

                textview_1.text = "Set Discount value for total Bill"

            }

            R.id.textview_DVW -> {
                promoTypeCode = "DVW"
                promoType = 3
                textview_DVW.setBackgroundResource(R.color.colorRed)
                textview_DVW.setTextColor(Color.parseColor("#FFFFFF"))


                textview_DD.setBackgroundResource(R.color.textcolor5)
                textview_DD.setTextColor(Color.parseColor("#000000"))

                textview_TD.setBackgroundResource(R.color.textcolor5)
                textview_TD.setTextColor(Color.parseColor("#000000"))

                textview_VW.setBackgroundResource(R.color.textcolor5)
                textview_VW.setTextColor(Color.parseColor("#000000"))

                textview_1.text = "Set Wave off value for delivery charges"

            }

            R.id.textview_VW -> {
                promoTypeCode = "VW"
                promoType = 4
                textview_VW.setBackgroundResource(R.color.colorRed)
                textview_VW.setTextColor(Color.parseColor("#FFFFFF"))


                textview_DD.setBackgroundResource(R.color.textcolor5)
                textview_DD.setTextColor(Color.parseColor("#000000"))

                textview_TD.setBackgroundResource(R.color.textcolor5)
                textview_TD.setTextColor(Color.parseColor("#000000"))

                textview_DVW.setBackgroundResource(R.color.textcolor5)
                textview_DVW.setTextColor(Color.parseColor("#000000"))


                textview_1.text = "Set Wave off value for total Bill"

            }


            R.id.textview_createcode -> {

                textview_code.visibility = View.GONE
                textview_55.visibility = View.GONE

                try {
                    promoValue = edittext_value.text.toString().toDouble()
                } catch (num: NumberFormatException) {

                }

                when {
                    (!InternetConnection.checkInternetConnection()) -> {
                        IonAlert(requireContext(), IonAlert.ERROR_TYPE)
                            .setTitleText("No Internet")
                            .setContentText("No internet access please check your connection and retry !")
                            .setConfirmText("OK")
                            .setConfirmClickListener(IonAlert.ClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                            })
                            .show()

                    }

                    promoTypeCode.isEmpty() -> {
                        IonAlert(requireContext(), IonAlert.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("Please select Promo Type")
                            .setConfirmText("OK")
                            .setConfirmClickListener(IonAlert.ClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                            })
                            .show()

                    }
                    edittext_value.text.toString().isEmpty() -> {

                        IonAlert(requireContext(), IonAlert.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("Please set the value")
                            .setConfirmText("OK")
                            .setConfirmClickListener(IonAlert.ClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                            })
                            .show()
                    }
                    promoValue <= 0 -> {
                        IonAlert(requireContext(), IonAlert.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("Please enter valid value")
                            .setConfirmText("OK")
                            .setConfirmClickListener(IonAlert.ClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                            })
                            .show()
                    }
                    ((promoTypeCode == "DVW").and(promoValue > 350)) -> {
                        IonAlert(requireContext(), IonAlert.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("Maximum delivery value is 350 RS")
                            .setConfirmText("OK")
                            .setConfirmClickListener(IonAlert.ClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                            })
                            .show()
                    }


                    (((promoTypeCode == "DD").or(promoTypeCode == "TD")).and(promoValue > 100)) -> {
                        IonAlert(requireContext(), IonAlert.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("Discount present value must be within 1-100")
                            .setConfirmText("OK")
                            .setConfirmClickListener(IonAlert.ClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                            })
                            .show()
                    }


                    else -> {
                        cl_progress.visibility = View.VISIBLE
                        crateCode()
                    }
                }


            }

            R.id.imageview_navigation_past -> {
                (activity as MainActivity).openDrawer()
            }


        }

    }


    private fun checkPromoCode(code: String) {

        val query: Query = promoCode?.orderByChild("promocode")!!.equalTo(code)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.childrenCount == 0L) {
                    savePromoCode(code)

                } else {
                    crateCode()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                cl_progress.visibility = View.GONE
                IonAlert(requireContext(), IonAlert.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Please try again !!")
                    .setConfirmText("OK")
                    .setConfirmClickListener(IonAlert.ClickListener { sDialog ->
                        sDialog.dismissWithAnimation()
                    })
                    .show()
            }
        })


    }

    private fun savePromoCode(code: String) {

        var unxId = genarateUniqCode()
        val tsLong = System.currentTimeMillis() / 1000
        var user = AppPrefs.getUserPrefs(requireContext(), AppPrefs.KEY_USER)
        var promoCOde = Promo().apply {
            promo_type = promoType.toLong()
            promocode = code
            promocode_add_date = tsLong
            promocode_add_user = user.admin_name
            promocode_id = unxId
            promocode_type_code = promoTypeCode
            promocode_validate = true
            promocode_value = promoValue

        }

        promoCode?.child(unxId.toString())?.setValue(promoCOde)
            ?.addOnSuccessListener {


                textview_code.text = promoCOde.promocode
                textview_code.visibility = View.VISIBLE
                textview_55.visibility = View.VISIBLE


                copyTextToClipboard(promoCOde.promocode)

                cl_progress.visibility = View.GONE

                Toast.makeText(
                    activity,
                    "Successfully created Promo code",
                    Toast.LENGTH_SHORT
                ).show()
            }
            ?.addOnFailureListener {
                Toast.makeText(
                    activity,
                    "Not Successfully created Promo code,Please try again !!",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }


    private fun crateCode() {
        createdCode = promoTypeCode + ((1..100).random()).toString()
        checkPromoCode(createdCode)


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

    private fun copyTextToClipboard(code: String) {
        val clipboardManager =
            activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", code)
        clipboardManager.setPrimaryClip(clipData)

    }
}