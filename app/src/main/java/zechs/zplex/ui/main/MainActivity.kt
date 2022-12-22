package zechs.zplex.ui.main

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.TranslateAnimation
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import zechs.zplex.R
import zechs.zplex.databinding.ActivityMainBinding


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.mainNavHostFragment
        ) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.fragmentMedia, R.id.castsFragment,
                R.id.episodesListFragment, R.id.fragmentList,
                R.id.fragmentCollection, R.id.watchFragment,
                R.id.bigImageFragment -> {
                    animationNavColorChange(R.color.statusBarColor)
                    hideSlideDown(binding.bottomNavigationView)
                }
                else -> {
                    animationNavColorChange(R.color.fadedColor)
                    showSlideUp(binding.bottomNavigationView)
                }
            }
        }

    }

    private fun animationNavColorChange(
        @ColorRes color: Int
    ) = lifecycleScope.launch {
        val from = window.navigationBarColor
        val to = ContextCompat.getColor(applicationContext, color)
        ValueAnimator
            .ofArgb(from, to)
            .also {
                it.addUpdateListener { animator ->
                    window.navigationBarColor = (animator.animatedValue as Int)
                }
                it.start()
            }
    }

    private fun showSlideUp(
        view: View
    ) = lifecycleScope.launch {
        if (view.isGone) {
            view.isVisible = true
            TranslateAnimation(
                0f, 0f,
                view.height.toFloat(), 0f
            ).apply {
                interpolator = AccelerateInterpolator()
                duration = 250L
            }.also {
                view.startAnimation(it)
            }
        }
    }

    private fun hideSlideDown(
        view: View
    ) = lifecycleScope.launch {
        if (view.isVisible) {
            TranslateAnimation(
                0f, 0f, 0f,
                view.height.toFloat()
            ).apply {
                interpolator = AccelerateInterpolator()
                duration = 250L
            }.also {
                view.startAnimation(it)
            }
            view.isGone = true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

//    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
//        if (event.action == MotionEvent.ACTION_DOWN) {
//            val v: View? = currentFocus
//            if (v is EditText) {
//                val outRect = Rect()
//                v.getGlobalVisibleRect(outRect)
//                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
//                    v.clearFocus()
//                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
//                }
//            }
//        }
//        return super.dispatchTouchEvent(event)
//    }

    companion object {
        const val TAG = "MainActivity"
    }

}