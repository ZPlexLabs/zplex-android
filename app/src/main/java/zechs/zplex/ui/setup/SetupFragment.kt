package zechs.zplex.ui.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import zechs.zplex.databinding.FragmentSetupBinding

class SetupFragment : Fragment() {

    companion object {
        const val TAG = "SetupFragment"
    }

    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupBinding.inflate(
            inflater, container, /* attachToParent */false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSetupBinding.bind(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}