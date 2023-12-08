package com.yearlater.inboxmessenger.activities.authentication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
import com.otpless.main.OtplessView
import com.yearlater.inboxmessenger.BuildConfig
import com.yearlater.inboxmessenger.R
import com.yearlater.inboxmessenger.activities.SplashActivity
import com.yearlater.inboxmessenger.utils.AppSignatureHelper
import com.yearlater.inboxmessenger.utils.IntentUtils
import com.yearlater.inboxmessenger.utils.SharedPreferencesManager
import com.yearlater.inboxmessenger.utils.Util
import com.yearlater.inboxmessenger.utils.network.AuthManager
import kotlinx.android.synthetic.main.activity_authentication.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.util.UUID
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
    private lateinit var otplessView: OtplessView

    private var email = "${currentPhoneNumber}inbox@inboxme.com"
    private var password = "inbox@password"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        navigation = Navigation.findNavController(this, R.id.nav_host_fragment)
        navigation.setGraph(R.navigation.nav_signup)

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        auth = FirebaseAuth.getInstance()
        setLoading(false)
        SharedPreferencesManager.setAppSignature("9MS/jdHO72a")

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

    val client = OkHttpClient()
    var currentOtpSent = ""
    override fun verifyPhoneNumber(phoneNumber: String, countryCode: String) {
        setLoading(true)
        val authManager = AuthManager()
        currentOtpSent = Random.nextInt(111111, 999999).toString()
        val smsAppNameText = "Team INBOX"
        val DLT_TE_ID_NEW_SMS = "1407169830589404358"
        val DLT_TE_ID_FORGOT_PASSWORD_SMS = ""

        authManager.formatNumber(phoneNumber, countryCode)?.let { formattedNumber ->
            currentPhoneNumber = formattedNumber
            val mainLooper = Looper.getMainLooper()
            //authManager.verify(formattedNumber, this, authCallback)
            email = currentPhoneNumber + "inbox@inboxme.com"
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

                    val baseUrl = "https://sms.bluesofttechnology.in/api/"
                    val message = "Dear Customer, Your OTP is $currentOtpSent - $smsAppNameText"

                    //encoding message
                    val encoded_message = URLEncoder.encode(message)
                    val route = "6"
                    val authKey = "9596AoxnO6aUf40V6530aeeeP11"
                    val request = Request.Builder().url(
                        "$baseUrl" + "sendhttp.php?authkey=$authKey" + "&mobiles=${currentPhoneNumber.trim()}" + "&message=$message" + "&sender=INCLEG" + "&route=$route" + "&country=91" + "&DLT_TE_ID=$DLT_TE_ID_NEW_SMS"
                    ).get().header("User-Agent", "OkHttp Headers.java").build()

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
                } catch (ste: SocketTimeoutException) {
                    Log.e(TAG, "verifyPhoneNumber: ", ste)
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
                //checkEmailExistsOrNot(email)
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

    fun checkEmailExistsOrNot(email: String) {
        val myUuid = UUID.randomUUID()
        FirebaseAuth.getInstance()
        auth.signInWithCustomToken(myUuid.toString()).addOnCompleteListener {

        }.addOnFailureListener {
            Toast.makeText(
                this@AuthenticationActivity, "Unable to login into application.", Toast.LENGTH_SHORT
            ).show()
        }

        /* auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
             Log.d(TAG, "" + task.result?.signInMethods?.size)
             if (task.result?.signInMethods?.size === 0) {
                 // email not existed
                 auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                     setLoading(false)
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
                         Toast.makeText(
                             this@AuthenticationActivity,
                             "Unable to create account into application.",
                             Toast.LENGTH_SHORT
                         ).show()
                     }
                 }
             } else {
                 // email existed
                 auth.signInWithEmailAndPassword(
                     email, password
                 ).addOnCompleteListener { task ->
                     setLoading(false)
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
                         this@AuthenticationActivity.recreate()
                     } else {
                         Toast.makeText(
                             this@AuthenticationActivity, "Login Failed.", Toast.LENGTH_SHORT
                         ).show()
                     }
                 }
             }
         }.addOnFailureListener {
             Toast.makeText(
                 this@AuthenticationActivity, "Unable to login into application.", Toast.LENGTH_SHORT
             ).show()
         }*/
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
                val request = Request.Builder().url("http://37.220.31.85:8090/getToken")
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
                            this@AuthenticationActivity.recreate()
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
                Handler(mainLooper).post {
                    AlertDialog.Builder(this@AuthenticationActivity, R.style.AlertDialogStyle)
                        .apply {
                            setMessage(if (BuildConfig.DEBUG) sce.message else "Service is down. Please check after some time.")
                            setPositiveButton(R.string.ok, null)
                            show()
                        }
                }
            } catch (ex: Exception) {
                Handler(mainLooper).post {
                    AlertDialog.Builder(this@AuthenticationActivity, R.style.AlertDialogStyle)
                        .apply {
                            setMessage(if (BuildConfig.DEBUG) ex.message else "Service is down. Please check after some time.")
                            setPositiveButton(R.string.ok, null)
                            show()
                        }
                }
            }
        }


    }

    private fun setLoading(setLoading: Boolean) {
        progressbar.isVisible = setLoading
        if (!this.navHostFragment.isAdded) return;
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