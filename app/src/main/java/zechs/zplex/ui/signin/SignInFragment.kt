package zechs.zplex.ui.signin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import zechs.zplex.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {

    companion object {
        const val TAG = "SignInFragment"
    }

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(
            inflater, container, /* attachToParent */false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSignInBinding.bind(view)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}