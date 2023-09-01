package com.yearlater.inboxmessenger.adapters.messaging.holders.base

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.yearlater.inboxmessenger.R
import com.yearlater.inboxmessenger.model.realms.Message
import com.yearlater.inboxmessenger.model.realms.User
import com.yearlater.inboxmessenger.utils.AdapterHelper

open class BaseSentHolder(context: Context, itemView: View) : BaseHolder(context,itemView) {

    var messageStatImg:ImageView? = itemView.findViewById(R.id.message_stat_img)


    override fun bind(message: Message, user: User, position: Int) {
        super.bind(message, user, position)


        //imgStat (received or read)
        messageStatImg?.setImageResource(AdapterHelper.getMessageStatDrawable(message.messageStat))


    }




}

