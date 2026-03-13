package com.danghung.elearning.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.danghung.elearning.App
import com.danghung.elearning.R
import com.danghung.elearning.Storage
import com.danghung.elearning.view.OnMainCallback
import com.danghung.elearning.view.fragment.BaseFragment

@Suppress("DEPRECATION")
abstract class BaseActivity<V : ViewBinding, M : ViewModel> : AppCompatActivity(),
    View.OnClickListener, OnMainCallback {

    protected lateinit var binding: V
    protected lateinit var viewModel: M

    override fun onCreate(data: Bundle?) {
        super.onCreate(data)
        binding = initViewBinding()
        setContentView(binding.root)
        setUpStatusBar()
        viewModel = ViewModelProvider(this)[getClassVM()]
        initViews()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val count = supportFragmentManager.backStackEntryCount
                if (count == 0) {
                    askForExitApp()
                } else {
                    supportFragmentManager.popBackStack()
                }
            }
        })
    }


    private fun setUpStatusBar() {
        val root = findViewById<View>(R.id.ln_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            v.setPadding(0, top, 0, 0)
            insets
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        WindowInsetsControllerCompat(window, root).isAppearanceLightStatusBars = true
    }

    private fun askForExitApp() {
        AlertDialog.Builder(this).setTitle("Alert").setMessage("Close this app?")
            .setPositiveButton("Close") { _, _ ->
                finish()
            }.setNegativeButton("Don't") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    abstract fun initViews()

    abstract fun initViewBinding(): V

    abstract fun getClassVM(): Class<M>


    @SuppressLint("PrivateResource")
    override fun onClick(v: View) {
        v.startAnimation(AnimationUtils.loadAnimation(this, androidx.appcompat.R.anim.abc_fade_in))
        clickView(v)
    }

    protected open fun clickView(v: View) {
        //do nothing
    }

    protected fun getStorage(): Storage {
        return App.instance.storage
    }

    protected fun notify(msg: String?) {
        Toast.makeText(App.instance, msg, Toast.LENGTH_SHORT).show()
    }

    protected fun notify(msg: Int) {
        Toast.makeText(App.instance, msg, Toast.LENGTH_SHORT).show()
    }

    override fun showFragment(tag: String, data: Any?, isBacked: Boolean) {
        try {
            val clazz = Class.forName(tag)
            val constructor = clazz.getConstructor()
            val baseFragment: BaseFragment<*, *> = constructor.newInstance() as BaseFragment<*, *>
            baseFragment.setOnCallBack(this)
            baseFragment.setAttachData(data)
            val trans = supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.ln_main, baseFragment, tag)

            if (isBacked) {
                trans.addToBackStack(null)
            }
            trans.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun showLoading() {
        binding.root.findViewById<View>(R.id.viewLoading)?.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        binding.root.findViewById<View>(R.id.viewLoading)?.visibility = View.GONE
    }


}