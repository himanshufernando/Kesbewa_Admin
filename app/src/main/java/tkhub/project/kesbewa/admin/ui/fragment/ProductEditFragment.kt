package tkhub.project.kesbewa.admin.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import tkhub.project.kesbewa.admin.R
import tkhub.project.kesbewa.admin.data.models.Products
import tkhub.project.kesbewa.admin.databinding.FragmentProductEditBinding
import tkhub.project.kesbewa.admin.databinding.FragmentSearchOrdersBinding
import tkhub.project.kesbewa.admin.viewmodels.past.PastViewModels
import tkhub.project.kesbewa.admin.viewmodels.products.ProductsViewModels


class ProductEditFragment : Fragment() {
    private val viewmodel: ProductsViewModels by viewModels { ProductsViewModels.LiveDataVMFactory }
    lateinit var binding: FragmentProductEditBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_product_edit, container, false)
        binding.products = viewmodel

        var pro = arguments?.getParcelable<Products>("Product")
        //set user details to ui data binding
        binding.productsDetails = pro


        return binding.root
    }

}