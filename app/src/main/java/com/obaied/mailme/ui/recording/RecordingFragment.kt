package com.joseph.mailme.ui.recording

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.joseph.colours.Colour
import com.joseph.mailme.R
import com.joseph.mailme.data.local.PrefManager
import com.joseph.mailme.ui.anims.ActivityCircularTransform
import com.joseph.mailme.ui.base.BaseActivity
import com.joseph.mailme.ui.base.BasePermissionsFragment
import com.joseph.mailme.ui.recording_service.RecordingService_ClientController
import com.joseph.mailme.util.d
import kotlinx.android.synthetic.main.activity_recording.*
import kotlinx.android.synthetic.main.fragment_recording.*
import javax.inject.Inject

/**
 * Created by ab on 10.10.17.
 */
class RecordingFragment :
        BasePermissionsFragment(),
        RecordingService_ClientController.ControllerListener,
        RecordingMvpView {
    @Inject lateinit var presenter: RecordingPresenter
    @Inject lateinit var prefManager: PrefManager

    private var fragmentListener: RecordingFragmentListener? = null
    private val recordingServiceClientController =
            RecordingService_ClientController(this)

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (activity is RecordingFragmentListener) {
            fragmentListener = activity as RecordingFragmentListener
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        ActivityCircularTransform.setup(activity, fragment_container)

        return inflater?.inflate(R.layout.fragment_recording, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (recordingServiceClientController.isServiceRunning()) {
            resetUi(true)
        } else {
            resetUi(false)
        }

        presenter.attachView(this)
    }

    override fun onStart() {
        super.onStart()

        recordingServiceClientController.onStart()
    }

    override fun onStop() {
        super.onStop()

        recordingServiceClientController.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun onDetach() {
        super.onDetach()
        fragmentListener = null
    }

    override fun showErrorMessage(message: String) {
        toastLog(message)
    }

    override fun fromClientController_OnRecordingStarted() {
        d { "fromClientController_OnRecordingStarted" }
        btn_toggle_recording.text = getString(R.string.stop)

        playRecordingAnimation()
    }

    override fun fromClientController_OnRecordingStopped() {
        d { "fromClientController_OnRecordingStopped" }

        // Show the user with a pop-up to save or discard the recording
        val noteHint = getTempRecordingPath()
                .split("/")
                .last()
                .removeSuffix(BaseActivity.THREEGP)

        SaveRecordingDialog(activity, noteHint, object : SaveRecordingDialog.OnClick {
            override fun onClick_Discard() {
                val tempPath = getTempRecordingPath()

                presenter.clearTempRecording(tempPath)
            }

            override fun onClick_Save(noteName: String) {
                var to = BaseActivity.getVoiceNotesDir() + "/$noteName"

                // If "to" does not have an extension, add an extension
                if (!to.endsWith(BaseActivity.THREEGP))
                    to += BaseActivity.THREEGP

                val from = getTempRecordingPath()
                d { "to [$to] || from [$from]" }

                presenter.renameLastRecording(to, from)
            }
        }).show()
    }

    override fun fromClientController_OnRecordingProgress(duration: String) {
        text_duration.let {
            it.text = duration
        }
    }

    override fun tempRecordingCleared() {
        toastLog("File discarded")

        fragmentListener?.dismissActivityAndCleanupUi()
    }

    override fun onRecordingRenamed() {
        toastLog("File saved")

        fragmentListener?.dismissActivityAndCleanupUi()
    }

    private fun playRecordingAnimation() {
        var translate: ObjectAnimator? = null
        var reveal: ObjectAnimator? = null
        var crossFade: ObjectAnimator? = null

        btn_toggle_recording.let {
            translate = ObjectAnimator.ofFloat(it, "translationY", 200f)
        }

        text_duration.let {
            reveal = ObjectAnimator.ofFloat(it, View.ALPHA, 0f, 1f)
        }

        image_mic.let {
            crossFade = ObjectAnimator.ofArgb(it, "colorFilter", Colour.holoRedLightColor())
        }

        val animatorSet = AnimatorSet()
        animatorSet.duration = 700L

        animatorSet.playTogether(
                translate,
                reveal,
                crossFade
        )
        animatorSet.start()
    }

    fun resetUi(isRecordingActive: Boolean) {
        btn_toggle_recording.let {
            it.setOnClickListener {
                if (!didGrantPermissions()) {
                    return@setOnClickListener
                }

                recordingServiceClientController.toggleRecording(activity,
                        getTempRecordingPath())
            }
        }

        if (!isRecordingActive) {
            btn_toggle_recording.let {
                it.text = getString(R.string.start)
            }
        } else {
            btn_toggle_recording.let {
                it.text = getString(R.string.stop)
                it.translationY = 200f
            }

            text_duration.let {
                it.alpha = 1f
            }

            image_mic.let {
                it.setColorFilter(Colour.holoRedLightColor())
            }
        }
    }

    private fun getTempRecordingPath(): String =
            prefManager.read(activity, getString(R.string.preference_temp_recording_path))

    interface RecordingFragmentListener {
        fun dismissActivityAndCleanupUi()
    }
}