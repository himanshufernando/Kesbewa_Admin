package tkhub.project.kesbewa.admin.viewmodels.home

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.Dispatchers
import tkhub.project.kesbewa.admin.KesbewaAdmin
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.repo.HomeRepo
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs

class HomeViewModels(app: Context) : ViewModel() {

    var ctx = app
    var appPref = AppPrefs
    var repo = HomeRepo(ctx)


   var isHomeProgressBarVisible = ObservableField<Boolean>()

    //new orders
    private val _newOrdersGet = MutableLiveData<Int>()
    val newOrdersGet: LiveData<Int> = _newOrdersGet


    private val _userPastOrders = MutableLiveData<String>()
    val userPastOrders: LiveData<String> = _userPastOrders




    private val _orderUpddate = MutableLiveData<OrderRespons>()
    val orderUpddate: LiveData<OrderRespons> = _orderUpddate



    private val _confirmedOrders = MutableLiveData<Int>()
    val confirmedOrders: LiveData<Int> = _confirmedOrders

    private val _packedOrders = MutableLiveData<Int>()
    val packedOrders: LiveData<Int> = _packedOrders

    private val _deliveryOrders = MutableLiveData<Int>()
    val deliveryOrders: LiveData<Int> = _deliveryOrders


    private val _deliveredOrders = MutableLiveData<Int>()
    val deliveredOrders: LiveData<Int> = _deliveredOrders

    private val _completeOrders = MutableLiveData<Int>()
    val completeOrders: LiveData<Int> = _completeOrders

    private val _rejectOrders = MutableLiveData<Int>()
    val  rejectOrders: LiveData<Int> = _rejectOrders

    init {
        isHomeProgressBarVisible.set(false)
    }

    fun getNewOrders(){
        _newOrdersGet.value = (1..1000).random()
    }

    val newOrders = newOrdersGet.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            var currentRes = repo.getNewOrders()
            isHomeProgressBarVisible.set(false)
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



    fun getConformedOrders(){
        _confirmedOrders.value = (1..1000).random()
    }

    val confirmedOrdersResponse = confirmedOrders.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            var currentRes = repo.getConfirmedOrders()
            isHomeProgressBarVisible.set(false)
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


    fun getPackedOrders(){
        _packedOrders.value = (1..1000).random()
    }

    val packedOrdersResponse = packedOrders.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            var currentRes = repo.getPackedOrders()
            isHomeProgressBarVisible.set(false)
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


    fun getDeliveryOrders(){
        _deliveryOrders.value = (1..1000).random()
    }

    val deliveryOrdersResponse = deliveryOrders.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            var currentRes = repo.getDeliveryOrders()
            isHomeProgressBarVisible.set(false)
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



    fun getDeliveredOrders(){
        _deliveredOrders.value = (1..1000).random()
    }

    val deliveredOrdersResponse = deliveredOrders.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            var currentRes = repo.getDeliveredOrders()
            isHomeProgressBarVisible.set(false)
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


    //user pass orders
    fun getUserPastOrders(userid: String?) {
        _userPastOrders.value = userid
    }

    val pastOrders = userPastOrders.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            var currentRes = repo.getUserPastOrders(id)
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


    //update

    fun orderStatusUpdate(orderRespons: OrderRespons,status:Int,note:String){
        isHomeProgressBarVisible.set(true)
        orderRespons.order_status_note =note
        orderRespons.order_status=status.toString()
        orderRespons.order_status_code=status

        _orderUpddate.value = orderRespons

    }



    val orderUpdateResponse = orderUpddate.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            var orderRes = repo.updateOrdersStatus(id)
            isHomeProgressBarVisible.set(false)
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
            return HomeViewModels(app) as T

        }
    }
}