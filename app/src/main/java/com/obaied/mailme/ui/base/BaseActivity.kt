package com.obaied.mailme.ui.base

import android.app.Fragment
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.obaied.mailme.ui.navigation.Navigator
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasFragmentInjector
import javax.inject.Inject

/**
 * Created by ab on 19/03/2017.
 */

abstract class BaseActivity :
        AppCompatActivity(),
        HasFragmentInjector {
    private var isAttachToWindow = false

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttachToWindow = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        isAttachToWindow = false
    }

    /**
     * Adds a [android.support.v4.app.Fragment] to this activity's layout.
     *
     * @param containerViewId The container view to where add the fragment.
     * @param fragment The fragment to be added.
     */
    protected fun addFragment(containerViewId: Int, fragment: Fragment) {
        val fragmentTransaction = this.fragmentManager.beginTransaction()
        fragmentTransaction.add(containerViewId, fragment)
        fragmentTransaction.commit()
    }

    override fun fragmentInjector(): AndroidInjector<Fragment>
            = fragmentInjector
}
