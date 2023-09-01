package com.yearlater.inboxmessenger.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.yearlater.inboxmessenger.R
import com.yearlater.inboxmessenger.activities.authentication.AuthenticationActivity
import com.yearlater.inboxmessenger.activities.main.MainActivity
import com.yearlater.inboxmessenger.activities.setup.SetupUserActivity
import com.yearlater.inboxmessenger.utils.DetachableClickListener
import com.yearlater.inboxmessenger.utils.PermissionsUtil
import com.yearlater.inboxmessenger.utils.SharedPreferencesManager
import com.yearlater.inboxmessenger.utils.network.FireManager
import kotlinx.android.synthetic.main.activity_agree_privacy_policy.*


class
AgreePrivacyPolicyActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 451

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agree_privacy_policy)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = this.resources.getColor(R.color.colorBlack)
        }

        btn_agree.setOnClickListener {

            showContactsConfirmationDialog()

        }


        header.setOnClickListener(View.OnClickListener {
            Log.e("click", "click working")
            val url = "https://drive.inkl.in/register"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        })


    }

    private fun showContactsConfirmationDialog() {
        val dialog = AlertDialog.Builder(this, R.style.AlertDialogStyle)
        dialog.setTitle("Agreement")
        dialog.setCancelable(false)


        val view = LayoutInflater.from(this).inflate(R.layout.privacy_policy_dialog,null,false)
        dialog.setView(view)

        val tv = view.findViewById<TextView>(R.id.tv_privacy_policy_dialog)

        val checkBox = view.findViewById<CheckBox>(R.id.chb_agree)
        checkBox.text = "By Checking this, You agree to the collection and use of information in accordance with this Privacy Policy"


        getHtml4(tv)

        dialog.setNegativeButton("DECLINE",null)

        dialog.setPositiveButton("AGREE") { dialog, which ->
            SharedPreferencesManager.setAgreedToPrivacyPolicy(true)

            if (!FireManager.isLoggedIn())
                startLoginActivity()
            else
                startNextActivity()
        }

        val mDialog = dialog.show()
        mDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        checkBox.setOnCheckedChangeListener { buttonView,isChecked ->

            mDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = isChecked

        }

    }




    private fun requestPermissions() {
        if(Build.VERSION.SDK_INT < 33)
        ActivityCompat.requestPermissions(this, PermissionsUtil.permissions, PERMISSION_REQUEST_CODE)
        else
            ActivityCompat.requestPermissions(this, PermissionsUtil.permissions13, PERMISSION_REQUEST_CODE)
    }
    private fun startPrivacyPolicyActivity() {
        val intent = Intent(this, AgreePrivacyPolicyActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }



    private fun getHtml4(textView: TextView){

        val html = resources.getString(R.string.privacy_policy_html)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            textView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT))
        } else {
            textView.setText(Html.fromHtml(html))
        }

    }

    private fun startLoginActivity() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun startNextActivity() {
        if (SharedPreferencesManager.isUserInfoSaved()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, SetupUserActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (PermissionsUtil.permissionsGranted(grantResults)) {
            if (!FireManager.isLoggedIn())
                startLoginActivity()
            else
                startNextActivity()
        } else
            showAlertDialog()
    }

    private fun showAlertDialog() {

        val positiveClickListener = DetachableClickListener.wrap { dialogInterface, i -> requestPermissions() }

        val negativeClickListener = DetachableClickListener.wrap { dialogInterface, i -> finish() }


        val builder = AlertDialog.Builder(this)
                .setTitle(R.string.missing_permissions)
                .setMessage(R.string.you_have_to_grant_permissions)
                .setPositiveButton(R.string.ok, positiveClickListener)
                .setNegativeButton(R.string.no_close_the_app, negativeClickListener)
                .create()

        //avoid memory leaks
        positiveClickListener.clearOnDetach(builder)
        negativeClickListener.clearOnDetach(builder)
        builder.show()
    }
}
