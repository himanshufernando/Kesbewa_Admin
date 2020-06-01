package tkhub.project.kesbewa.admin.viewmodels.past

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import tkhub.project.kesbewa.admin.KesbewaAdmin
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.repo.HomeRepo
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs

class PastViewModels(app: Context) : ViewModel() {

    var ctx = app
    var appPref = AppPrefs
    var repo = HomeRepo(ctx)




    private val _completeOrders = MutableLiveData<Int>()
    val completeOrders: LiveData<Int> = _completeOrders


    private val _rejectOrders = MutableLiveData<Int>()
    val rejectOrders: LiveData<Int> = _rejectOrders

    private val _orderUpddate = MutableLiveData<OrderRespons>()
    val orderUpddate: LiveData<OrderRespons> = _orderUpddate


    val isOrdersLoaderVisible = ObservableField<Boolean>()




    private val _searchOrdersByCode = MutableLiveData<String>()
    val searchOrdersByCode: LiveData<String> = _searchOrdersByCode


    private val _filterOrders = MutableLiveData<String>()
    val filterOrders: LiveData<String> = _filterOrders


    private val _userPastOrders = MutableLiveData<String>()
    val userPastOrders: LiveData<String> = _userPastOrders


    init {

    }

    fun getCompleteOrders() {
        _completeOrders.value = (1..1000).random()
        isOrdersLoaderVisible.set(false)
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

    fun getRejectOrders() {
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

    fun orderStatusUpdate(orderRespons: OrderRespons, status: Int, note: String) {
        orderRespons.order_status_note = note
        orderRespons.order_status = status.toString()
        orderRespons.order_status_code = status
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

    val searchQuery: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val orderList: MutableLiveData<ArrayList<OrderRespons>> by lazy {
        MutableLiveData<ArrayList<OrderRespons>>()
    }

    fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if(s.length > 3){
            searchQuery.value = s.toString()
            _searchOrdersByCode.value = s.toString()
        }
    }


    val ordersByCode = searchOrdersByCode.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            var currentRes = repo.getOrdersBuyOrder(id)
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


    fun filterList(){

        _filterOrders.value = searchQuery.value
    }


    val filterdOrders = filterOrders.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            var res = repo.getOrderFilter(orderList.value!!,id)
            try {
                emit(KesbewaResult.Success(res))
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


    object LiveDataVMFactory : ViewModelProvider.Factory {
        var app: Context = KesbewaAdmin.applicationContext()
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PastViewModels(app) as T

        }
    }
}