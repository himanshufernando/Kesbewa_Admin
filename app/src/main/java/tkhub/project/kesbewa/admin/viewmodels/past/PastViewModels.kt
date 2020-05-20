package tkhub.project.kesbewa.admin.viewmodels.past

import android.content.Context
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import androidx.navigation.Navigation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.Dispatchers
import tkhub.project.kesbewa.admin.KesbewaAdmin
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.repo.homeRepo
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs

class PastViewModels(app: Context) : ViewModel() {

    var ctx = app
    var appPref = AppPrefs
    var repo = homeRepo(ctx)


    private val _completeOrders = MutableLiveData<Int>()
    val completeOrders: LiveData<Int> = _completeOrders


    private val _rejectOrders = MutableLiveData<Int>()
    val rejectOrders: LiveData<Int> = _rejectOrders

    private val _orderUpddate = MutableLiveData<OrderRespons>()
    val orderUpddate: LiveData<OrderRespons> = _orderUpddate

    init {

    }
    fun getCompleteOrders(){
        _completeOrders.value = (1..1000).random()
    }


    val completeOrdersResponse = completeOrders.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            var currentRes = repo.getComplteOrders()
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

    fun getRejectOrders(){
        _rejectOrders.value = (1..1000).random()
    }

    val rejectOrdersResponse = rejectOrders.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            var currentRes = repo.getRejectOrders()
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

    fun orderStatusUpdate(orderRespons: OrderRespons,status:Int,note:String){
        orderRespons.order_status_note =note
        orderRespons.order_status=status.toString()
        orderRespons.order_status_code=status
        _orderUpddate.value = orderRespons

    }


    val orderUpdateResponse = orderUpddate.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            var orderRes = repo.updateOrdersStatus(id)
            try {
                orderRes.collect { value ->
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
            return PastViewModels(app) as T

        }
    }
}