package tkhub.project.kesbewa.admin.viewmodels.products

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import tkhub.project.kesbewa.admin.KesbewaAdmin
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.repo.ProductRepo
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs

class ProductsViewModels(app: Context) : ViewModel() {

    var ctx = app
    var appPref = AppPrefs
    var repo = ProductRepo(ctx)

    private val _getProducts = MutableLiveData<Int>()
    val getProducts: LiveData<Int> = _getProducts


    init {
        _getProducts.value = (1..1000).random()
    }

    val productsList = getProducts.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            var currentRes = repo.getProduct()
            try {
                currentRes.collect { value ->
                    if (value == null) {
                        emit(KesbewaResult.LogicError.LogError(AppPrefs.errorSomethingWentWrong()))
                    } else {
                        emit(KesbewaResult.Success(value))
                    }
                }
            } catch (ioException: Exception) {
                emit(KesbewaResult.ExceptionError.ExError(ioException))
            }
        }
    }



    object LiveDataVMFactory : ViewModelProvider.Factory {
        var app: Context = KesbewaAdmin.applicationContext()
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProductsViewModels(app) as T

        }
    }
}