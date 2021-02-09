package tkhub.project.kesbewa.admin.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter

import androidx.annotation.LayoutRes
import kotlinx.android.synthetic.main.item_auto_complete_text_view.view.*
import tkhub.project.kesbewa.admin.data.models.AdminUsersRespons


class AdminUsersAdapter(private val c: Context, @LayoutRes private val layoutResource: Int, private val citys: ArrayList<AdminUsersRespons>) :
    ArrayAdapter<AdminUsersRespons>(c, layoutResource, citys) {

    var filteredCityes: List<AdminUsersRespons> = listOf()

    override fun getCount(): Int = filteredCityes.size

    override fun getItem(position: Int): AdminUsersRespons = filteredCityes[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(c).inflate(layoutResource, parent, false)

        view.tvMovieName.text = filteredCityes[position].admin_user_user.user_name


        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                @Suppress("UNCHECKED_CAST")
                filteredCityes = filterResults.values as List<AdminUsersRespons>
                notifyDataSetChanged()
            }

            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val queryString = charSequence?.toString()?.toLowerCase()

                val filterResults = FilterResults()
                filterResults.values = if (queryString == null || queryString.isEmpty())
                    citys
                else
                    citys.filter {
                        it.admin_user_user.user_name!!.toLowerCase().contains(queryString)
                    }
                return filterResults
            }
        }
    }
}