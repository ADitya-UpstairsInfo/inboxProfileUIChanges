package com.yearlater.inboxmessenger.activities.authentication

sealed class ViewState {
    data class Verify(val phoneNumber: String) : ViewState()


}