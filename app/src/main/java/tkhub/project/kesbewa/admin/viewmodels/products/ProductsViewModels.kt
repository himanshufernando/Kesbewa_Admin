package tkhub.project.kesbewa.admin.viewmodels.products

import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import tkhub.project.kesbewa.admin.KesbewaAdmin
import tkhub.project.kesbewa.admin.data.db.AppDatabase
import tkhub.project.kesbewa.admin.data.db.ProductsDao
import tkhub.project.kesbewa.admin.data.models.CartItem
import tkhub.project.kesbewa.admin.data.models.OrderRespons
import tkhub.project.kesbewa.admin.data.models.ProductImage
import tkhub.project.kesbewa.admin.data.models.Products
import tkhub.project.kesbewa.admin.data.responsmodel.KesbewaResult
import tkhub.project.kesbewa.admin.repo.ProductRepo
import tkhub.project.kesbewa.admin.services.Perfrences.AppPrefs

class ProductsViewModels(productsDao: ProductsDao, app: Context) : ViewModel() {

    var ctx = app
    var appPref = AppPrefs
    var repo = ProductRepo(productsDao,ctx)

    private val _getProducts = MutableLiveData<Int>()
    val getProducts: LiveData<Int> = _getProducts

    private val _addProductsImage = MutableLiveData<ProductImage>()
    val addProductsImage: LiveData<ProductImage> = _addProductsImage


    private val _updateSoldOut = MutableLiveData<Products>()
    val updateSoldOut: LiveData<Products> = _updateSoldOut


    val addProductsList: MutableLiveData<ArrayList<CartItem>> by lazy {
        MutableLiveData<ArrayList<CartItem>>()
    }


    init {

    }


    fun updateSoldOut(pro : Products){

        println("ssssssssssssssssssssssssssssss   55555  "+pro.pro_id.toString())
        _updateSoldOut.value = pro

    }

    val updateSoldOutResponse = _updateSoldOut.switchMap { pro ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            try {
                println("ssssssssssssssssssssssssssssss   999999  "+pro.pro_id.toString())
                var cart = repo.updateSoldOut(pro)
                emit(KesbewaResult.Success(cart))
            } catch (ioException: Exception) {
                emit(KesbewaResult.ExceptionError.ExError(ioException))
            }

        }
    }




    fun getProducts(){
        _getProducts.value = (1..1000).random()

    }

    val productsList = getProducts.switchMap { status ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            try {
                var cart = repo.getProduct()
                emit(KesbewaResult.Success(cart))
            } catch (ioException: Exception) {
                emit(KesbewaResult.ExceptionError.ExError(ioException))
            }

        }
    }




    fun addProductImage(pro : ProductImage){
        _addProductsImage.value  = pro


    }


    val addProductsImageResponse = addProductsImage.switchMap { id ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            var currentRes = repo.addProductImage(id)
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
        private val productsDao = AppDatabase.getInstance(app).productsDao()
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProductsViewModels(productsDao,app) as T

        }
    }
}