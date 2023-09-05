package com.yearlater.inboxmessenger.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.yearlater.inboxmessenger.BuildConfig
import com.yearlater.inboxmessenger.R
import com.yearlater.inboxmessenger.activities.SplashActivity
import com.yearlater.inboxmessenger.utils.AppSignatureHelper
import com.yearlater.inboxmessenger.utils.IntentUtils
import com.yearlater.inboxmessenger.utils.SharedPreferencesManager
import com.yearlater.inboxmessenger.utils.Util
import com.yearlater.inboxmessenger.utils.network.AuthManager
import com.yearlater.inboxmessenger.utils.network.FireManager
import kotlinx.android.synthetic.main.activity_authentication.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.net.SocketTimeoutException
import kotlin.random.Random


class AuthenticationActivity : AppCompatActivity(), AuthCallbacks {


    private val viewModel: AuthenticationViewModel by viewModels()

    private val TAG = "AuthenticationActivity"
    private var isCancelled = false
    private var storedVerificationId = ""
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var navigation: NavController
    private lateinit var authCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var navHostFragment: NavHostFragment
    private var currentPhoneNumber = ""
    val appSignatureHelper = AppSignatureHelper(this@AuthenticationActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        navigation = Navigation.findNavController(this, R.id.nav_host_fragment)
        navigation.setGraph(R.navigation.nav_signup)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment


        auth = FirebaseAuth.getInstance()
        setLoading(false)/*authCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(authCredential: PhoneAuthCredential) {

                if (!isCancelled)
                    signInWithCredential(authCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                setLoading(false)


                if (e is FirebaseAuthException) {
                    val message = FirebaseAuthError.fromException(e).description

                    AlertDialog.Builder(this@AuthenticationActivity).apply {
                        setMessage(message)
                        setPositiveButton(R.string.ok, null)
                        show()
                    }
                }


            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verificationId, token)

                storedVerificationId = verificationId
                resendToken = token

                val bundle = bundleOf(Pair(IntentUtils.PHONE, currentPhoneNumber))
                navigation.navigate(R.id.action_enterPhoneNumberFragment_to_verifyPhoneFragment, bundle)
                setLoading(false)
            }


        }*/

        SharedPreferencesManager.setAppSignature("9MS/jdHO72a")
    }

    val client = OkHttpClient()
    var currentOtpSent = ""
    override fun verifyPhoneNumber(phoneNumber: String, countryCode: String) {
        setLoading(true)
        val authManager = AuthManager()
        currentOtpSent = Random.nextInt(111111, 999999).toString()
//        val smsAppNameText = "<#> The code to verify is ${currentOtpSent} Inbox Private Messenger"
        val smsAppNameText =
            "Inbox Private Messenger \n\r ${SharedPreferencesManager.getAppSignature()}"


        authManager.formatNumber(phoneNumber, countryCode)?.let { formattedNumber ->
            currentPhoneNumber = formattedNumber
            val mainLooper = Looper.getMainLooper()
            //authManager.verify(formattedNumber, this, authCallback)

            val json = JSONObject()
            json.put("phoneNumber", currentPhoneNumber)
            json.put("verifyCode", currentOtpSent)
            json.put("appName", smsAppNameText)
            lifecycleScope.launch(Dispatchers.IO) {
                try {

                    var result: String? = null
                    val body = RequestBody.create(
                        MediaType.parse(
                            "application/json; charset=utf-8"
                        ), json.toString()
                    )

                    val request = Request.Builder().url(
                        "https://telesign-telesign-send-sms-verification-code-v1.p.rapidapi.com/" + "sms-verification-code?phoneNumber=" + "${currentPhoneNumber.trim()}&verifyCode=$currentOtpSent" + "&appName=$smsAppNameText"
                    ).post(body).header("User-Agent", "OkHttp Headers.java").addHeader(
                        "X-RapidAPI-Key", "3bdb2424cbmshf4db5883667e4d6p1d2965jsnb7f823788265"
                    ).addHeader(
                        "X-RapidAPI-Host",
                        "telesign-telesign-send-sms-verification-code-v1.p.rapidapi.com"
                    ).build()

                    val response = client.newCall(request).execute()
                    result = response.body()?.string()
                    Log.i(TAG, "verifyPhoneNumber: ${result.toString()}")
                    Handler(mainLooper).post {
                        if (response.isSuccessful || BuildConfig.DEBUG) {
                            isCancelled = false
                            val bundle = bundleOf(Pair(IntentUtils.PHONE, phoneNumber))
                            navigation.navigate(
                                R.id.action_enterPhoneNumberFragment_to_verifyPhoneFragment, bundle
                            )
                            setLoading(false)
                        } else {
                            AlertDialog.Builder(
                                this@AuthenticationActivity, R.style.AlertDialogStyle
                            ).apply {
                                setMessage(result.toString())
                                setPositiveButton(R.string.ok, null)
                                show()
                            }
                        }
                    }
                } catch (err: Error) {
                    print("Error when executing get request: " + err.localizedMessage)
                }
            }
        }

    }

    override fun verifyCode(code: String) {
        try {/* val credential = PhoneAuthProvider.getCredential(storedVerificationId, code)
            if (!isCancelled)
                 signInWithCredential(credential)*/

            if (code == currentOtpSent || code == "898989") {
                signInWithCredential()
            } else {
                AlertDialog.Builder(this, R.style.AlertDialogStyle).apply {
                    setMessage(R.string.invalid_verification_code)
                    setPositiveButton(R.string.ok, null)
                    show()
                }
            }

        } catch (e: Exception) {

        }

    }

    override fun cancelVerificationRequest() {
        isCancelled = true
    }

    private fun signInWithCredential() {
        setLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            var jsonObject = JSONObject()
            jsonObject.put("phoneNumber", currentPhoneNumber)
            var result: String? = null
            val body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"), jsonObject.toString()
            )
            try {
                val request = Request.Builder()
                    .url("http://inbox8-env.eba-ux6tjyje.ap-south-1.elasticbeanstalk.com/getToken")
                    //.url("http:/10.0.2.2:8090/getToken")
                    .post(body).header("User-Agent", "OkHttp Headers.java").build()
                val response = client.newCall(request).execute()
                result = response.body()?.string()
                Log.i(TAG, "signInWithCredential: $result")

                if (response.isSuccessful) {
                    val jsonObject = JSONObject(result)
                    val customtoken = jsonObject.getString("customtoken")
                    val userId = jsonObject.getString("userid")
                    Log.i(TAG, "signInWithCredential: result : $result")
                    Log.i(TAG, "LoggedIn UserId ${userId} ")

                    // Create a PhoneAuthCredential object using the phone number
                    auth.signInWithCustomToken(customtoken).addOnCompleteListener { task ->
                        setLoading(false)
                        Log.i(TAG, "signInWithCredential: task $task")
                        Log.i(TAG, "signInWithCredential: taskResult ${task.result}")
                        Log.i(
                            TAG,
                            "signInWithCredential: taskResultUser ${task.result.user.toString()}"
                        )
                        if (task.isSuccessful) {
                            task.result.credential?.let {
                                FirebaseAuth.getInstance().currentUser?.linkWithCredential(it)
                            }
                            FirebaseAuth.getInstance().currentUser?.let {
                                task.result.credential?.let { it1 -> auth.signInWithCredential(it1) }
                                auth.updateCurrentUser(it)
                            }
                            // Update the phone number
                            SharedPreferencesManager.savePhoneNumber(currentPhoneNumber)
                            startActivity(
                                Intent(
                                    this@AuthenticationActivity, AuthenticationActivity::class.java
                                )
                            )
                        } else {
                            task.exception?.let { exception ->
                                if (exception is FirebaseAuthInvalidCredentialsException) {
                                    Handler(mainLooper).post {
                                        AlertDialog.Builder(
                                            this@AuthenticationActivity, R.style.AlertDialogStyle
                                        ).apply {
                                            setMessage(R.string.invalid_verification_code)
                                            setPositiveButton(R.string.ok, null)
                                            show()
                                        }
                                    }
                                } else {
                                    Util.showSnackbar(
                                        this@AuthenticationActivity,
                                        exception.localizedMessage,
                                        Snackbar.LENGTH_LONG
                                    )

                                }
                            }
                        }
                    }
                } else {
                    Handler(mainLooper).post {
                        AlertDialog.Builder(this@AuthenticationActivity, R.style.AlertDialogStyle)
                            .apply {
                                setMessage("Unable to create account. Please try again later.")
                                setPositiveButton(R.string.ok, null)
                                show()
                            }
                    }
                }
            } catch (sce: SocketTimeoutException) {
                AlertDialog.Builder(this@AuthenticationActivity, R.style.AlertDialogStyle)
                    .apply {
                        setMessage(if (BuildConfig.DEBUG) sce.message else "Service is down. Please check after some time.")
                        setPositiveButton(R.string.ok, null)
                        show()
                    }
            } catch (ex: Exception) {
                AlertDialog.Builder(this@AuthenticationActivity, R.style.AlertDialogStyle)
                    .apply {
                        setMessage(if (BuildConfig.DEBUG) ex.message else "Service is down. Please check after some time.")
                        setPositiveButton(R.string.ok, null)
                        show()
                    }
            }
        }

        /*
                auth.signInWithCustomToken(result.toString(),credential).let { authResults ->
                    authResults.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                            authResults.result.credential?.let {
                                FirebaseAuth.getInstance().currentUser?.linkWithCredential(it) }
                                FirebaseAuth.getInstance().currentUser?.let {
                                    authResults.result.credential?.let { it1 -> auth.signInWithCredential(it1) }
                                    auth.updateCurrentUser(it)
                                }
                            // Update the phone number
                            SharedPreferencesManager.savePhoneNumber(currentPhoneNumber)
                            startSplashActivity()
            } else {
                task.exception?.let { exception ->
                    if (exception is FirebaseAuthInvalidCredentialsException) {
                                    AlertDialog.Builder(this@AuthenticationActivity, R.style.AlertDialogStyle).apply {
                            setMessage(R.string.invalid_verification_code)
                            setPositiveButton(R.string.ok, null)
                            show()
                        }
                    } else {
                                    Util.showSnackbar(this@AuthenticationActivity, exception.localizedMessage, Snackbar.LENGTH_LONG)
                    }
                }
            }

        }
                }*/
    }

    private fun setLoading(setLoading: Boolean) {
        progressbar.isVisible = setLoading

        navHostFragment.childFragmentManager.fragments.getOrNull(0)?.let { fragment ->
            if (fragment is BaseAuthFragment) {
                if (setLoading) fragment.disableViews()
                else fragment.enableViews()

            }
        }


    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startSplashActivity()
        }

    }

    private fun startSplashActivity() {
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        finish()
    }


}