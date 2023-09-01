package com.yearlater.inboxmessenger.adapters.messaging

import androidx.lifecycle.LiveData
import com.yearlater.inboxmessenger.model.AudibleState

interface AudibleBase {
    var audibleState: LiveData<Map<String, AudibleState>>?
    var audibleInteraction:AudibleInteraction?
}