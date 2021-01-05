package tkhub.project.kesbewa.admin.ui.fragment

import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_add_product_images.view.*
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.NetworkError
import tkhub.project.kesbewa.admin.data.models.ProductImage
import tkhub.project.kesbewa.admin.data.models.Products
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.services.network.InternetConnection
import tkhub.project.kesbewa.admin.viewmodels.products.ProductsViewModels


class AddProductImagesFragment : Fragment() {



    private val viewmodel: ProductsViewModels by viewModels { ProductsViewModels.LiveDataVMFactory }
    lateinit var root: View
    lateinit var selectedProduct : Products
    var alertDialog: AlertDialog? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        selectedProduct = arguments?.getParcelable<Products>("Product")!!
        root =  inflater.inflate(R.layout.fragment_add_product_images, container, false)


        root.textView_pro_image_name.text = (selectedProduct.pro_code+"_"+selectedProduct.pro_id)
        root.textView_pro_name.text = selectedProduct.pro_name
        root.edittext_image_url.setText(selectedProduct.pro_code)


        root.textview_addimage.setOnClickListener {

            if (!InternetConnection.checkInternetConnection()) {
                errorAlertDialog(AppPrefs.errorNoInternet())
            }else{

                if (!viewmodel.addProductsImageResponse.hasObservers()) {
                    addProductsImageResponseObserver()
                }

                var proImage = ProductImage().apply {
                    img_url = root.edittext_image_url.text.toString()
                    pro_id = selectedProduct.pro_id.toString()
                    pro_code = selectedProduct.pro_code
                }

                viewmodel.addProductImage(proImage)

            }

        }

        return root
    }

    override fun onResume() {
        super.onResume()


    }

    override fun onStop() {
        viewmodel.addProductsImageResponse.removeObservers(viewLifecycleOwner)
        super.onStop()
    }


    fun addProductsImageResponseObserver() {
        viewmodel.addProductsImageResponse.observe(viewLifecycleOwner, Observer {response ->
            when (response) {
                is KesbewaResult.Success -> {
                    Toast.makeText(
                        activity,
                        response.data.errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is KesbewaResult.ExceptionError.ExError -> {
                    var errorAddress = NetworkError()
                    errorAddress.errorMessage = response.exception.message.toString()
                    errorAddress.errorCode = ""

                    errorAlertDialog(errorAddress)
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

}