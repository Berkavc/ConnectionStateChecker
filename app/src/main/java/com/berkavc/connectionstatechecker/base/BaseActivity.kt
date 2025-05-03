package com.berkavc.connectionstatechecker.base

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner

abstract class BaseActivity<T : BaseViewModel, B : ViewDataBinding> : AppCompatActivity() {
    abstract val layoutRes: Int
    abstract val viewModel: T

    abstract var viewLifeCycleOwner: LifecycleOwner

    open fun arrangeUI() {}

    open fun gatherArgs() {}

    open fun initBinding() {
        this._binding?.lifecycleOwner = this
        viewLifeCycleOwner = this
    }

    abstract fun observeViewModel()


    private var _binding: B? = null
    val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.window.setSoftInputMode(SOFT_INPUT_ADJUST_PAN)
        _binding = DataBindingUtil.inflate(layoutInflater, layoutRes, null, false)
        setContentView(_binding!!.root)
        gatherArgs()
        initBinding()
        arrangeUI()
        observeViewModel()
    }


    override fun onDestroy() {
        super.onDestroy()
//        _binding = null
    }

    fun navigateToNextActivity(activity: Activity, intent: Intent) {
        activity.finish()
        startActivity(intent)
        overridePendingTransition(0,0)
    }

    fun navigateToWithoutPopNextActivity(intent: Intent) {
        startActivity(intent)
        overridePendingTransition(0,0)
    }

    fun finishActivityWithoutAnimation(){
        finish()
        overridePendingTransition(0,0)
    }

    fun resetActivity(activity: Activity){
        val intent = Intent(activity , activity.javaClass)
        activity.finish()
        startActivity(intent)
        overridePendingTransition(0,0)
    }
}
