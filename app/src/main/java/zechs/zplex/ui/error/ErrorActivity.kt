package zechs.zplex.ui.error

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import zechs.zplex.R
import zechs.zplex.databinding.ActivityErrorBinding
import zechs.zplex.utils.ext.ifNullOrEmpty

@AndroidEntryPoint
class ErrorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityErrorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE)
            ?: getString(R.string.unknown_error)
        val stackTrace = intent.getStringExtra(EXTRA_STACK_TRACE)
        binding.errorMessageTextView.text = errorMessage.ifEmpty { "Unknown error!" }
        binding.stackTraceTextView.text = stackTrace?.ifNullOrEmpty { "No stack trace" }
    }

    companion object {
        const val EXTRA_ERROR_MESSAGE = "extra_error_message"
        const val EXTRA_STACK_TRACE = "extra_stack_trace"
    }
}