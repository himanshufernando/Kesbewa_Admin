package tkhub.project.kesbewa.admin.viewmodels.login

import android.content.Context
import android.view.View
import androidx.databinding.ObservableField

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import androidx.navigation.Navigation
import tkhub.project.kesbewa.admin.KesbewaAdmin
import tkhub.project.kesbewa.admin.data.models.User
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.repo.LoginRepo
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs
import tkhub.project.kesbewa.admin.R
import java.lang.Exception

class LoginViewModel(app: Context) : ViewModel() {


    var ctx = app
    var appPref = AppPrefs
    var repo = LoginRepo(ctx)

    lateinit var layoutView: View

    //Login
    var editTextLoginPhone = MutableLiveData<String>()
    var editTextLoginPassword = MutableLiveData<String>()

    val isLoginVisible = ObservableField<Boolean>()

    private val _loginServiceCall = MutableLiveData<User>()
    val loginServiceCall: LiveData<User> = _loginServiceCall


    init {
        isLoginVisible.set(false)
    }



    fun login(view: View) {
        isLoginVisible.set(true)
        layoutView = view
        var user = User("",0,"",
            editTextLoginPhone.value.toString(),"",
            editTextLoginPassword.value.toString()
        )
        _loginServiceCall.value=user
    }


    val loginServiceResponse = loginServiceCall.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            isLoginVisible.set(false)
            try {
                var loginRes = repo.loginUser(id)
                loginRes.collect { value ->
                    if (value.errorCode == appPref.SUCCESS_LOGGING) {
                        emit(KesbewaResult.Success(value))
                       Navigation.findNavController(layoutView).navigate(R.id.fragmentLoginToHome)
                    } else {
                        emit(KesbewaResult.LogicError.LogError(value))
                    }
                }
            } catch (ex: Exception) {
                emit(KesbewaResult.ExceptionError.ExError(ex))
            }
        }
    }


    object LiveDataVMFactory : ViewModelProvider.Factory {
        var app: Context = KesbewaAdmin.applicationContext()
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(app) as T

        }
    }

}