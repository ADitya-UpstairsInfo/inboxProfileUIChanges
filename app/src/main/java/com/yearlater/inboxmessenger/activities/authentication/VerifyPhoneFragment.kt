package com.yearlater.inboxmessenger.activities.authentication

import android.app.AlertDialog
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.yearlater.inboxmessenger.R
import com.yearlater.inboxmessenger.receivers.SmsBroadcastReceiver
import com.yearlater.inboxmessenger.utils.IntentUtils
import kotlinx.android.synthetic.main.fragment_verify_phone.et_otp
import kotlinx.android.synthetic.main.fragment_verify_phone.tv_otp_info

class VerifyPhoneFragment : BaseAuthFragment(), SmsBroadcastReceiver.SmsListener {

    private var smsBroadcastReceiver = SmsBroadcastReceiver()
    private var TAG = "VerifyPhoneFragment"


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verify_phone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getString(IntentUtils.PHONE)?.let { phone ->
            tv_otp_info.text = requireActivity().getString(R.string.enter_the_otp_sent_to, phone)
        }

        et_otp.doOnTextChanged { text, _, _, _ ->
            if (text?.length == 6) {
                et_otp.onEditorAction(EditorInfo.IME_ACTION_DONE)
                completeRegistration()
            }
        }

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    AlertDialog.Builder(requireActivity()).apply {
                        setMessage(R.string.cancel_verification_confirmation_message)
                        setNegativeButton(R.string.no, null)
                        setPositiveButton(R.string.yes) { _, _ ->
                            callbacks?.cancelVerificationRequest()
                            Navigation.findNavController(et_otp).navigateUp()
                        }
                        show()
                    }

                }
            }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        startSmsRetriever()
        smsBroadcastReceiver.setListener(this@VerifyPhoneFragment)
    }

    private fun startSmsRetriever() {
        val client = this.activity?.applicationContext?.let { SmsRetriever.getClient(it) }
        val task = client?.startSmsRetriever()
        task?.addOnSuccessListener {
            // SMS retrieval started
            Log.i(TAG, "startSmsRetriever: send the otp now.")
        }
        task?.addOnFailureListener {
            // SMS retrieval failed to start
            Log.i(TAG, "startSmsRetriever: SMS reading failed. $it")
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        this.activity?.registerReceiver(smsBroadcastReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        this.activity?.unregisterReceiver(smsBroadcastReceiver)
    }

    private fun completeRegistration() {
        callbacks?.verifyCode(et_otp.text.toString())
    }

    override fun enableViews() {
        super.enableViews()
        et_otp.isEnabled = true
    }

    override fun disableViews() {
        super.disableViews()
        et_otp.isEnabled = false

    }

    override fun onSmsReceived(message: String) {
        Log.i(TAG, "onSmsReceived: for Inobx : ${message.extract6DigitOtp()}")
        et_otp.setText("" + message.extract6DigitOtp())
        et_otp.onEditorAction(EditorInfo.IME_ACTION_DONE)
    }

    override fun onSmsTimeOut() {
        Log.d(TAG, "onSmsTimeOut: Time out. Fir se bhejop")
    }


    fun String.extract6DigitOtp(): Int {
        val sms = "401634 is your Inbox Messenger    9MS/jdHO72a verification code."
        var result = this.filter { it.isDigit() }
        Log.i("Otp", "onSmsReceived: OTP : ${result.chunked(6)[0]}")
        return result.chunked(6)[0].toInt()
    }
}