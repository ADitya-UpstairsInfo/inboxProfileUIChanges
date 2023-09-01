package com.yearlater.inboxmessenger.adapters.messaging.holders

import android.content.Context
import android.view.View
import android.widget.TextView
import com.vanniktech.emoji.EmojiTextView
import com.yearlater.inboxmessenger.R
import com.yearlater.inboxmessenger.adapters.messaging.holders.base.BaseReceivedHolder
import com.yearlater.inboxmessenger.model.realms.Message
import com.yearlater.inboxmessenger.model.realms.User

// received message with type text
class ReceivedTextHolder(context: Context, itemView: View) : BaseReceivedHolder(context,itemView) {

    private var tvMessageContent: EmojiTextView = itemView.findViewById(R.id.tv_message_content)
    private var tvIncreption: TextView = itemView.findViewById(R.id.tvIncreption)

    override fun bind(message: Message, user: User, position: Int) {
        super.bind(message, user, position)
        tvMessageContent.text = message.content
        if(position==0){
            tvIncreption.visibility = View.VISIBLE
        }else{
            tvIncreption.visibility = View.GONE
        }
    }


}