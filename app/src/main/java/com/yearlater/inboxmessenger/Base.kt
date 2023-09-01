package com.yearlater.inboxmessenger

import io.reactivex.disposables.CompositeDisposable

interface Base {
    val disposables:CompositeDisposable
}