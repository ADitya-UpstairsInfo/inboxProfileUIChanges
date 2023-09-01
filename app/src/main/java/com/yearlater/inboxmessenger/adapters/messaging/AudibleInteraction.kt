package com.yearlater.inboxmessenger.adapters.messaging

import com.yearlater.inboxmessenger.model.realms.Message

interface AudibleInteraction {
    fun onSeek(message: Message, progress:Int, max:Int)
    fun onSpeedUp(speedvalue: Float)
}

