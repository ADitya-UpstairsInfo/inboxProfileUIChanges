package com.yearlater.inboxmessenger.adapters.messaging.holders

import ak.sh.ay.musicwave.MusicWave
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.yearlater.inboxmessenger.R
import com.yearlater.inboxmessenger.adapters.messaging.AudibleBase
import com.yearlater.inboxmessenger.adapters.messaging.AudibleInteraction
import com.yearlater.inboxmessenger.adapters.messaging.holders.base.BaseSentHolder
import com.yearlater.inboxmessenger.common.extensions.setHidden
import com.yearlater.inboxmessenger.model.AudibleState
import com.yearlater.inboxmessenger.model.ImageItem
import com.yearlater.inboxmessenger.model.constants.DownloadUploadStat
import com.yearlater.inboxmessenger.model.realms.Message
import com.yearlater.inboxmessenger.model.realms.User
import com.yearlater.inboxmessenger.utils.AdapterHelper
import com.yearlater.inboxmessenger.utils.ServiceHelper


class SentAudioHolder(context: Context, itemView: View) : BaseSentHolder(context, itemView), AudibleBase {

    var waveView: MusicWave = itemView.findViewById(R.id.wave_view)
    var playBtn: ImageView = itemView.findViewById(R.id.voice_play_btn)
    var seekBar: SeekBar = itemView.findViewById(R.id.voice_seekbar)
    private val tvAudioSize: TextView = itemView.findViewById(R.id.tv_audio_size)
    var tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
    var imgHeadset: ImageView = itemView.findViewById(R.id.img_headset)
    var speedup : TextView = itemView.findViewById(R.id.speed_up)
    var playspeed : Float = 1f

    override var audibleState: LiveData<Map<String, AudibleState>>? = null
    override var audibleInteraction: AudibleInteraction? = null

    override fun bind(message: Message, user: User, position: Int) {
        super.bind(message, user, position)


        //Set Initial Values
        seekBar.progress = 0
        playBtn.setImageResource(AdapterHelper.getPlayIcon(false))

        //if it's sending set the audio size
        if (message.downloadUploadStat != DownloadUploadStat.SUCCESS) {
            tvAudioSize.visibility = View.VISIBLE
            tvAudioSize.text = message.metadata
        } else {
            //otherwise hide the audio textview
            tvAudioSize.visibility = View.GONE
        }

        tvDuration.text = message.mediaDuration

        playBtn.setHidden(message.downloadUploadStat != DownloadUploadStat.SUCCESS, true)
        lifecycleOwner?.let {


            audibleState?.observe(it, Observer { audioRecyclerStateMap ->
                if (audioRecyclerStateMap.containsKey(message.messageId)) {
                    audioRecyclerStateMap[message.messageId]?.let { mAudioRecyclerState ->


                        if (mAudioRecyclerState.currentDuration != null)
                            tvDuration.text = mAudioRecyclerState.currentDuration

                        if (mAudioRecyclerState.progress != -1) {
                            seekBar.progress = mAudioRecyclerState.progress
                        }

                        if (mAudioRecyclerState.max != -1) {
                            val max = mAudioRecyclerState.max
                            seekBar.max = max
                        }

                        if (mAudioRecyclerState.isPlaying) {
                            imgHeadset.visibility = View.GONE
                            waveView.visibility = View.VISIBLE
                        } else {
                            imgHeadset.visibility = View.VISIBLE
                            waveView.visibility = View.GONE
                        }

                        if (mAudioRecyclerState.waves != null)
                            waveView.updateVisualizer(mAudioRecyclerState.waves)

                        playBtn.setImageResource(AdapterHelper.getPlayIcon(mAudioRecyclerState.isPlaying))

                    }
                } else {
                    tvDuration.text = message.mediaDuration
                    imgHeadset.visibility = View.VISIBLE
                    waveView.visibility = View.GONE
                }
            })
        }

        playBtn.setOnClickListener {
            interaction?.onContainerViewClick(adapterPosition, itemView, message)
        }
        speedup.setOnClickListener{


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

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser)
                    audibleInteraction?.onSeek(message, progress, seekBar.max)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }


}

