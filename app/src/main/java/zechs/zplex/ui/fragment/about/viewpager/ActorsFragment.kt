package zechs.zplex.ui.fragment.about.viewpager

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import zechs.zplex.R
import zechs.zplex.adapter.ActorsAdapter
import zechs.zplex.databinding.FragmentCastsBinding
import zechs.zplex.models.tvdb.actors.Data
import zechs.zplex.ui.activity.ZPlexActivity
import zechs.zplex.ui.fragment.about.AboutViewModel
import zechs.zplex.utils.Resource

class ActorsFragment : Fragment(R.layout.fragment_casts) {

    private var _binding: FragmentCastsBinding? = null
    private val binding get() = _binding!!

    private lateinit var actorsAdapter: ActorsAdapter

    private lateinit var aboutViewModel: AboutViewModel

    private val thisTAG = "ActorsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCastsBinding.bind(view)

        actorsAdapter = ActorsAdapter()

        binding.rvCasts.apply {
            adapter = actorsAdapter
            layoutManager = GridLayoutManager(activity, 2)
        }

        aboutViewModel = (activity as ZPlexActivity).aboutViewModel
        isLoading(true)

        aboutViewModel.actors.observe(viewLifecycleOwner, { responseMedia ->
            when (responseMedia) {
                is Resource.Success -> {
                    responseMedia.data?.let {
                        isLoading(false)
                        actorsAdapter.differ.submitList(it.data?.toList())
                    }
                }

                is Resource.Error -> {
                    binding.apply {
                        pbCasts.visibility = View.VISIBLE
                        btnRetryCasts.visibility = View.VISIBLE
                        rvCasts.visibility = View.GONE
                    }
                    responseMedia.message?.let { message ->
                        Toast.makeText(
                            context,
                            "An error occurred: $message",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(thisTAG, "An error occurred: $message")
                    }
                }

                is Resource.Loading -> {
                    isLoading(true)
                    actorsAdapter.differ.submitList(listOf<Data>().toList())
                }
            }
        })

    }

    private fun isLoading(isLoading: Boolean) {
        binding.apply {
            pbCasts.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
            rvCasts.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
            btnRetryCasts.visibility = View.INVISIBLE
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.apply {
            pbCasts.visibility = View.INVISIBLE
            rvCasts.visibility = View.INVISIBLE
            btnRetryCasts.visibility = View.INVISIBLE
        }
        binding.rvCasts.adapter = null
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.root.requestLayout()
    }
}