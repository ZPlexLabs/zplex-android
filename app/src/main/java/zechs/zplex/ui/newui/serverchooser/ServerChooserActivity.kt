package zechs.zplex.ui.newui.serverchooser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import zechs.zplex.databinding.ActivityServerchooserBinding


//@AndroidEntryPoint
class ServerChooserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServerchooserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerchooserBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}