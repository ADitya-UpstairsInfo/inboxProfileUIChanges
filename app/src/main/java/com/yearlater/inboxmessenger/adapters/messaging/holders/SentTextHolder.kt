package com.yearlater.inboxmessenger.adapters.messaging.holders

import android.content.Context
import android.view.View
import android.widget.TextView
import com.vanniktech.emoji.EmojiTextView
import com.vanniktech.emoji.EmojiUtils
import com.yearlater.inboxmessenger.R
import com.yearlater.inboxmessenger.adapters.messaging.holders.base.BaseSentHolder
import com.yearlater.inboxmessenger.model.realms.Message
import com.yearlater.inboxmessenger.model.realms.User


// sent message with type text
class SentTextHolder(context: Context, itemView: View) : BaseSentHolder(context,itemView) {
    private var tvMessageContent: EmojiTextView = itemView.findViewById(R.id.tv_message_content)
    private var tvIncreption: TextView = itemView.findViewById(R.id.tvIncreption)

    override fun bind(message: Message, user: User, position: Int) {
        super.bind(message, user, position)

        val emojiInformation = EmojiUtils.emojiInformation(message.content)
        val res: Int

        res = if (emojiInformation.isOnlyEmojis && emojiInformation.emojis.size == 1) {
            R.dimen.emoji_size_single_emoji
        } else if (emojiInformation.isOnlyEmojis && emojiInformation.emojis.size > 1) {
            R.dimen.emoji_size_only_emojis
        } else {
            R.dimen.emoji_size_default
        }

        tvMessageContent.setEmojiSizeRes(res, false)
        tvMessageContent.text = message.content

        if(position==0){
            tvIncreption.visibility = View.VISIBLE
        }else{
            tvIncreption.visibility = View.GONE
        }
    }

}

