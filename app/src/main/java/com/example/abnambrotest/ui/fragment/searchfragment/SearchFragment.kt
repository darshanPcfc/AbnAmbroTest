package com.example.abnambrotest.ui.fragment.searchfragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.abnambrotest.BR
import com.example.abnambrotest.R
import com.example.abnambrotest.base.BaseApplication
import com.example.abnambrotest.base.BaseFragment
import com.example.abnambrotest.databinding.FragmentVenueSearchBinding
import com.example.abnambrotest.db.VenueViewModel
import com.example.abnambrotest.ni.remote.response.search.Venues
import com.example.abnambrotest.ui.adapters.SearchListAdapter
import kotlinx.android.synthetic.main.fragment_venue_search.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class SearchFragment: BaseFragment<FragmentVenueSearchBinding, SearchViewModel>(),
    ISearchNavigator {


    override val viewModel: SearchViewModel by viewModel()
    private lateinit var fragmentSearchBinding: FragmentVenueSearchBinding
    override val bindingVariable: Int
        get() = BR.viewModel

    lateinit var searchListAdapter: SearchListAdapter

    override val layoutId: Int
        get() = R.layout.fragment_venue_search

    var venues: ArrayList<Venues> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setNavigator(this)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentSearchBinding = this.viewDataBinding!!
        setRecyclerView()
        //checkConnectivity()
    }

    private fun checkConnectivity() {
        if (!BaseApplication.hasNetwork()) {
            //fetch data from venue table of data base
            val venueViewModel = VenueViewModel(BaseApplication.instance!!)
            venueViewModel.allVenues.observe(viewLifecycleOwner, Observer {
                if(!it.isEmpty()){
                    refreshData(it as ArrayList<Venues>)
                }
            })
        }
    }

    private fun callSearchVenueAPI(searchStr : String){
        try {
            if (BaseApplication.hasNetwork()) {
                viewModel.search(searchStr).observe(viewLifecycleOwner, Observer {
                    if (it != null) {
                        println("API Call Value fragment")
                        Log.d("TAG","Success Data" + it.response.venues[0].location.city)
                        if (venues.size > 0)
                            venues.clear()
                        venues.addAll(it.response.venues)
                        refreshData(venues)
                    }
                })

            }else {
                //fetch data from venue table of data base
                val venueViewModel = VenueViewModel(BaseApplication.instance!!)
                venueViewModel.allVenues.observe(viewLifecycleOwner, Observer {
                    if(!it.isEmpty()){
                        refreshData(it as ArrayList<Venues>)
                    }else {
                        Toast.makeText(activity,"No Data in data base",Toast.LENGTH_LONG).show()
                    }
                })
            }
        }catch (e:Throwable){}
    }

    private fun refreshData(list: ArrayList<Venues>) {
        Log.d("_Frag","Internet Present"+ list.size)
        searchListAdapter.setVenueList(list)
        //insert data
        val venueViewModel = VenueViewModel(BaseApplication.instance!!)
        GlobalScope.launch {
            list.forEach{
                venueViewModel.insert(it)
            }
        }

    }

    private fun setRecyclerView() {
        rv_search_venues.layoutManager = LinearLayoutManager(context)
        searchListAdapter = SearchListAdapter(this)
        rv_search_venues.adapter = searchListAdapter
    }

    override fun onVenueListItemCLick(item: Venues) {
        val action : NavDirections = SearchFragmentDirections.openDetailFragment(item)
        navController.navigate(action)
        Toast.makeText(activity, "Clicked", Toast.LENGTH_LONG).show()
    }

    override fun onSearchGoClicked(searchStr: String) {
        callSearchVenueAPI(searchStr)
    }

    override fun enterTextMessage() {
        Toast.makeText(activity,"Please Enter Text",Toast.LENGTH_LONG).show()
    }


}