package com.yearlater.inboxmessenger.adapters.messaging

import com.yearlater.inboxmessenger.model.realms.RealmContact

interface ContactHolderInteraction {
    fun onMessageClick(contact: RealmContact)
    fun onAddContactClick(contact: RealmContact)
}