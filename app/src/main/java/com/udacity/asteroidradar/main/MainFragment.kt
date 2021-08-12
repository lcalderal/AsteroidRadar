package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.api.AsteroidApiFilter
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import kotlinx.android.synthetic.main.fragment_main.view.*
import timber.log.Timber

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        val activity = requireNotNull(this.activity)
        ViewModelProvider(this, MainViewModel.Factory(activity.application)).get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        val adapter = AsteroidListAdapter(AsteroidListener { asteroid ->
            viewModel.onAsteroidClicked(asteroid)
        })
        binding.asteroidRecycler.adapter = adapter

        val decorator = DividerItemDecoration(context,DividerItemDecoration.VERTICAL)
        decorator.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!)
        binding.asteroidRecycler.addItemDecoration(decorator)

        setHasOptionsMenu(true)

        viewModel.goToDetail.observe(viewLifecycleOwner, Observer { asteroid ->
            asteroid?.let {
                this.findNavController().navigate(
                    MainFragmentDirections.actionShowDetail(asteroid))
                viewModel.onGoToDetailCompleted()
            }
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
        Timber.d("Creating options menu")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Timber.d("Item selected: ${item.itemId}")
        viewModel.updateFilter(
            when(item.itemId) {
                R.id.show_saved_menu -> AsteroidApiFilter.SAVED
                R.id.show_today_menu -> AsteroidApiFilter.TODAY
                else -> AsteroidApiFilter.WEEK
            }
        )
        return true
    }
}
