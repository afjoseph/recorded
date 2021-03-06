package com.joseph.mailme.ui.recording

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.joseph.mailme.BuildConfig
import com.joseph.mailme.R
import com.joseph.mailme.ui.base.BasePermissionsActivity
import com.joseph.mailme.ui.recording_service.RecordingService
import kotlinx.android.synthetic.main.activity_recording.*

class RecordingActivity :
        BasePermissionsActivity(),
        RecordingFragment.RecordingFragmentListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)

        setupFragment()

        setupAdmob()
    }

    private fun setupAdmob() {
        adView.let {
            MobileAds.initialize(this, BuildConfig.ADMOB_APP_ID)

            val adRequestBuilder = AdRequest.Builder()

            if (BuildConfig.DEBUG) {
                adRequestBuilder.addTestDevice("13B5197226B86E6646A642C81B04B86D")
            }

            it.loadAd(adRequestBuilder.build())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Check if this intent came from a notification
        if (!intent.hasExtra(RecordingService.NOTIFICATION_INTENT_EXTRA)) {
            return
        }

        // Send a signal to the fragment to reset UI as if the recording is active
        val recordingFragment = getFragment() as RecordingFragment
        recordingFragment.resetUi(true)
    }

    override fun getDesiredPermissions(): Array<String> =
            arrayOf(android.Manifest.permission.RECORD_AUDIO)

    override fun onBackPressed() {
        dismissActivityAndCleanupUi()
    }

    override fun dismissActivityAndCleanupUi() {
        adView.visibility = View.GONE
        finishAfterTransition()
    }

    private fun setupFragment() {
        addFragment(R.id.fragment_container, RecordingFragment())
    }
}