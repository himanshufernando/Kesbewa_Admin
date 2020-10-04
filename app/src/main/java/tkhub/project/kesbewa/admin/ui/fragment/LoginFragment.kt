package tkhub.project.kesbewa.admin.ui.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.observe
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.fragment_login.view.*

import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.NetworkError
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.databinding.FragmentLoginBinding
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.ui.activity.MainActivity

import tkhub.project.kesbewa.admin.viewmodels.login.LoginViewModel

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {


    lateinit var binding: FragmentLoginBinding
    private val viewmodel: LoginViewModel by viewModels { LoginViewModel.LiveDataVMFactory }
    var isPasswordShow = false
    var alertDialog: AlertDialog? = null

    var appPref = AppPrefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.loginBindingView = viewmodel

        AppPrefs.setIntKeyValuePrefs(context!!, AppPrefs.KEY_FRAGMENT_ID, 1)



        binding.root.ll_password_show_hide.setOnClickListener {
            if (isPasswordShow) {
                binding.root.editText_password.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                isPasswordShow = false
                binding.root.iv_password_showhide.setImageResource(R.drawable.ic_pw_hide)
            } else {
                binding.root.editText_password.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                isPasswordShow = true
                binding.root.iv_password_showhide.setImageResource(R.drawable.ic_pw_show)
            }
        }

        (activity as MainActivity).removeDrawer()



        binding.root.constraintLayout7.setOnClickListener {

            if (!viewmodel.loginServiceResponse.hasObservers()) {
                loginServiceResponseObserver()
            }
            viewmodel.login()


        }


        return binding.root


    }

    private fun loginServiceResponseObserver() {
        viewmodel.loginServiceResponse.observe(viewLifecycleOwner, Observer {respons ->
            try {
                when(respons){
                    is KesbewaResult.Success ->{
                        NavHostFragment.findNavController(this).navigate(R.id.fragmentLoginToHome)
                        Toast.makeText(context, respons.data.errorMessage, Toast.LENGTH_SHORT).show()
                    }

                    is KesbewaResult.ExceptionError.ExError -> {
                        Toast.makeText(context, respons.exception.message, Toast.LENGTH_SHORT).show()
                    }
                    is KesbewaResult.LogicError.LogError -> {
                        Toast.makeText(context, respons.exception.errorMessage, Toast.LENGTH_SHORT).show()
                        setErrorUI(respons.exception)
                    }
                }

            } catch (ex: Exception) {

            }

        })
    }


    private fun setErrorUI(respond: NetworkError) {
        when (respond.errorCode) {
           appPref.ERROR_PHONE_NUMBER_EMPTY -> binding.root.editText_loginusername.error = respond.errorMessage
            appPref.ERROR_INCORRECT_LOGIN -> { errorUseLogin(respond) }
        }
    }

    private fun errorUseLogin(networkError: NetworkError) {
        if (alertDialog != null) {
            if (alertDialog!!.isShowing) {
                alertDialog!!.dismiss()
            }
        }
        alertDialog = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(networkError.errorTitle)
            builder.setMessage(networkError.errorMessage)
            builder.setCancelable(true)
            builder.apply {
                setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    return@OnClickListener
                })
            }
            builder.create()
            builder.show()

        } ?: throw IllegalStateException("Activity cannot be null")
    }


}
