package com.yearlater.inboxmessenger.adapters.messaging.holders

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.yearlater.inboxmessenger.R
import com.yearlater.inboxmessenger.adapters.messaging.AudibleBase
import com.yearlater.inboxmessenger.adapters.messaging.AudibleInteraction
import com.yearlater.inboxmessenger.adapters.messaging.holders.base.BaseReceivedHolder
import com.yearlater.inboxmessenger.common.extensions.setHidden
import com.yearlater.inboxmessenger.model.AudibleState
import com.yearlater.inboxmessenger.model.constants.DownloadUploadStat
import com.yearlater.inboxmessenger.model.realms.Message
import com.yearlater.inboxmessenger.model.realms.User
import com.yearlater.inboxmessenger.services.AudioService
import com.yearlater.inboxmessenger.utils.AdapterHelper
import com.yearlater.inboxmessenger.utils.ListUtil
import com.yearlater.inboxmessenger.utils.ServiceHelper

import de.hdodenhof.circleimageview.CircleImageView

class ReceivedVoiceMessageHolder(context: Context, itemView: View) : BaseReceivedHolder(context,itemView),AudibleBase {

    var playBtn: ImageView = itemView.findViewById<View>(R.id.voice_play_btn) as ImageView
    var seekBar: SeekBar = itemView.findViewById<View>(R.id.voice_seekbar) as SeekBar
    var speedup: TextView = itemView.findViewById(R.id.speed_up) as TextView
    private val circleImg: CircleImageView = itemView.findViewById<View>(R.id.voice_circle_img) as CircleImageView
    var tvDuration: TextView = itemView.findViewById<View>(R.id.tv_duration) as TextView

    private val voiceMessageStat: ImageView = itemView.findViewById(R.id.voice_message_stat)
    override var audibleState: LiveData<Map<String, AudibleState>>?=null

    override var audibleInteraction: AudibleInteraction? = null

    var playspeed : Float = 1f
    override fun bind(message: Message, user: User, position: Int) {
        super.bind(message, user, position)


        //set initial values
        seekBar.progress = 0
        playBtn.setImageResource(AdapterHelper.getPlayIcon(false))
        loadUserPhoto(user,message.fromId, circleImg)
        tvDuration.text = message.mediaDuration

        voiceMessageStat.setImageResource(AdapterHelper.getVoiceMessageIcon(message.isVoiceMessageSeen))
        playBtn.setHidden(message.downloadUploadStat != DownloadUploadStat.SUCCESS, true)

        lifecycleOwner?.let {


            audibleState?.observe(it, Observer { audioRecyclerStateMap ->
                if (audioRecyclerStateMap.containsKey(message.messageId)) {
                    audioRecyclerStateMap[message.messageId]?.let { mAudioRecyclerState ->


                        if (mAudioRecyclerState.currentDuration != null) {
                            tvDuration.text = mAudioRecyclerState.currentDuration
                            Log.e("Current",mAudioRecyclerState.currentDuration.toString())
                        }

                        if (mAudioRecyclerState.progress != -1) {
                            seekBar.progress = mAudioRecyclerState.progress
                        }

                        if (mAudioRecyclerState.max != -1) {
                            val max = mAudioRecyclerState.max
                            seekBar.max = max
                        }

                        playBtn.setImageResource(AdapterHelper.getPlayIcon(mAudioRecyclerState.isPlaying))

                    }
                } else {
                    tvDuration.text = message.mediaDuration
                }
            })
        }

        playBtn.setOnClickListener {

            interaction?.onContainerViewClick(adapterPosition, itemView, message)
            var str = speedup.text.toString()
            if(str.equals("1x")) {

                playspeed = 1f
            }else if(str.equals("1.5x")) {

                playspeed = 1.5f
            }else {
                speedup.setText("2x");
                playspeed = 2f
            }
            Log.e("recived speed",playspeed.toString())
            audibleInteraction?.onSpeedUp(playspeed)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    audibleInteraction?.onSeek(message, progress, seekBar.max)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        speedup.setOnClickListener{
            Log.e("speedup","Sa");

            var str = speedup.text.toString()
            if(str.equals("1x")) {
                speedup.setText("1.5x");
                playspeed = 1.5f
            }else if(str.equals("1.5x")) {
                speedup.setText("2x");
                playspeed = 2f
            }else {
                speedup.setText("1x");
                playspeed = 1f
            }

            audibleInteraction?.onSpeedUp(playspeed)

        }




    }
    private fun loadUserPhoto(user: User, fromId: String, imageView: ImageView) {

        //if it's a group load the user image
        if (user.isGroupBool && user.group.users != null) {
            val mUser = ListUtil.getUserById(fromId, user.group.users)
            if (mUser != null && mUser.thumbImg != null) {
                Glide.with(context).load(mUser.thumbImg).into(imageView)
            }
        } else {
            if (user.thumbImg != null) Glide.with(context).load(user.thumbImg).into(imageView)
        }
    }


}
