package com.yearlater.inboxmessenger.adapters.messaging.holders

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.yearlater.inboxmessenger.R
import com.yearlater.inboxmessenger.adapters.messaging.holders.base.BaseSentHolder
import com.yearlater.inboxmessenger.common.extensions.setHidden
import com.yearlater.inboxmessenger.model.constants.DownloadUploadStat
import com.yearlater.inboxmessenger.model.realms.Message
import com.yearlater.inboxmessenger.model.realms.User
import com.yearlater.inboxmessenger.utils.Util


class SentFileHolder(context: Context, itemView: View) : BaseSentHolder(context,itemView) {

    private val tvFileSize: TextView = itemView.findViewById(R.id.tv_file_size)
    private val tvFileName: TextView = itemView.findViewById(R.id.tv_file_name)
    private val tvFileExtension: TextView = itemView.findViewById(R.id.tv_file_extension)


    private val fileIcon: ImageView = itemView.findViewById(R.id.file_icon)
    override fun bind(message: Message, user: User, position: Int) {
        super.bind(message, user, position)
        val fileExtension = Util.getFileExtensionFromPath(message.metadata).toUpperCase()
        tvFileExtension.text = fileExtension
        //set file name
        tvFileName.text = message.metadata

        //file size
        tvFileSize.text = message.fileSize

        fileIcon.setHidden(message.downloadUploadStat != DownloadUploadStat.SUCCESS, true)

    }


}
