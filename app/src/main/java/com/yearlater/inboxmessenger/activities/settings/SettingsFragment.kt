package com.yearlater.inboxmessenger.activities.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.theartofdev.edmodo.cropper.CropImage
import com.yearlater.inboxmessenger.R
import com.yearlater.inboxmessenger.activities.SplashActivity
import com.yearlater.inboxmessenger.model.constants.GroupEventTypes
import com.yearlater.inboxmessenger.model.realms.GroupEvent
import com.yearlater.inboxmessenger.utils.*
import com.yearlater.inboxmessenger.utils.SharedPreferencesManager.clearSharedPref
import com.yearlater.inboxmessenger.utils.network.FireManager
import kotlinx.android.synthetic.main.fragment_settings.*
import java.io.IOException


class SettingsFragment : Fragment(), View.OnClickListener {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_profile.setOnClickListener(this)
        tv_notifications.setOnClickListener(this)
        tv_security.setOnClickListener(this)
        tv_chat.setOnClickListener(this)
        tv_privacy_policy.setOnClickListener(this)
        tv_about.setOnClickListener(this)
        tv_wallpaper.setOnClickListener(this)
        tv_logout.setOnClickListener(this)
    }

    override fun onClick(view: View) {

        when (view.id) {

            R.id.tv_profile -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_profilePreferenceFragment)
            R.id.tv_notifications -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_notificationPreferenceFragment)
            R.id.tv_security -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_securityPreferencesFragment)
            R.id.tv_chat -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_chatSettingsPreferenceFragment2)
            R.id.tv_privacy_policy -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_privacyPolicyFragment)
            R.id.tv_logout -> logoutDialog()
            R.id.tv_about -> Navigation.findNavController(view).navigate(R.id.action_settingsFragment_to_aboutFragment2)
            R.id.tv_wallpaper->setFun()

        }



    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                val file = DirManager.genereateWallpaperFile()
                try {
                    //copy image to the Wallpaper Folder
                    FileUtils.copyFile(resultUri.path, file)
                    SharedPreferencesManager.setWallpaperPath(file.path)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun setFun() {


        val dialog = AlertDialog.Builder(requireActivity(),R.style.AlertDialogStyle)
        dialog.setPositiveButton(R.string.choose_wallpaper) { dialogInterface, i ->
            CropImage.activity()
                    .start(requireActivity(), this@SettingsFragment)
        }.setNegativeButton(R.string.restore_default_wallpaper) { dialogInterface, i -> SharedPreferencesManager.setWallpaperPath("") }.show()
        false
    }

    private fun logoutDialog() {


        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext(),R.style.AlertDialog)
        builder.setTitle(R.string.logout)
            .setMessage(R.string.logout_confirm)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes, DialogInterface.OnClickListener { _, _ ->
                logout()
                clearSharedPref()
            })
            .show()
    }
    private fun logout() {
        FirebaseAuth.getInstance().signOut();
        Log.e("called logout","ok");
        val intent = Intent(activity, SplashActivity::class.java)
        startActivity(intent)
        activity?.finish()

    }


}