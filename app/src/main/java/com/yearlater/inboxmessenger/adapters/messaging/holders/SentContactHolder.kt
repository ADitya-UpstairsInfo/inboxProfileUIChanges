package com.yearlater.inboxmessenger.adapters.messaging.holders

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.yearlater.inboxmessenger.R
import com.yearlater.inboxmessenger.adapters.messaging.ContactHolderBase
import com.yearlater.inboxmessenger.adapters.messaging.ContactHolderInteraction
import com.yearlater.inboxmessenger.adapters.messaging.holders.base.BaseSentHolder
import com.yearlater.inboxmessenger.model.realms.Message
import com.yearlater.inboxmessenger.model.realms.User

class SentContactHolder(context: Context, itemView: View) : BaseSentHolder(context,itemView),ContactHolderBase {

    private val tvContactName: TextView = itemView.findViewById(R.id.tv_contact_name)
    private val btnMessageContact: Button = itemView.findViewById(R.id.btn_message_contact)

    override var contactHolderInteraction: ContactHolderInteraction? = null

    override fun bind(message: Message, user: User, position: Int) {
        super.bind(message, user, position)
        //set contact name
        tvContactName.text = message.content


        //send a message to this contact if installed this app
        btnMessageContact.setOnClickListener {
            contactHolderInteraction?.onMessageClick(message.contact)
        }

    }



}

