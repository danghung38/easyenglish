package com.danghung.elearning.view.activity


import android.Manifest
import androidx.core.app.ActivityCompat
import com.danghung.elearning.databinding.ActivityHomeBinding
import com.danghung.elearning.view.fragment.SplashFragment
import com.danghung.elearning.viewmodel.CommomVM

class HomeActivity : BaseActivity<ActivityHomeBinding, CommomVM>() {

    override fun getClassVM(): Class<CommomVM> = CommomVM::class.java

    override fun initViewBinding(): ActivityHomeBinding =
        ActivityHomeBinding.inflate(layoutInflater)

    override fun initViews() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            100
        )
        showFragment(SplashFragment.TAG, null, false)
    }


}