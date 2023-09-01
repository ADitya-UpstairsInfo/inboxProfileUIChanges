package com.yearlater.inboxmessenger.utils.network

import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.yearlater.inboxmessenger.extensions.*
import com.yearlater.inboxmessenger.job.DeleteStatusJob
import com.yearlater.inboxmessenger.model.realms.StatusSeenBy
import com.yearlater.inboxmessenger.model.realms.TextStatus
import com.yearlater.inboxmessenger.model.constants.StatusType
import com.yearlater.inboxmessenger.model.realms.Status
import com.yearlater.inboxmessenger.model.realms.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ServerValue
import com.yearlater.inboxmessenger.R
import com.yearlater.inboxmessenger.utils.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observable.fromIterable
import io.reactivex.Single
import java.io.File
import java.util.*

class StatusManager {
    private val currentDownloadStatusOperations: MutableList<String> = ArrayList()

    fun downloadVideoStatus(id: String, url: String, file: File): Single<String> {
        //prevent duplicates download
        if (currentDownloadStatusOperations.contains(id)) return Single.error(Throwable("already downloading"))

        currentDownloadStatusOperations.add(id)
        return FireConstants.storageRef.child(url)
                .getFileRx(file)
                .map { file.path }
                .doOnSuccess {
                    RealmHelper.getInstance().setLocalPathForVideoStatus(id, file.path)
                }.doFinally {
                    currentDownloadStatusOperations.remove(id)
                }

    }

    fun setStatusSeen(uid: String, statusId: String): Completable {
        val update = mapOf<String, Any>(
                Pair("uid", FireManager.uid),
                Pair("seenAt", ServerValue.TIMESTAMP)
        )
        return FireConstants.statusSeenUidsRef.child(uid).child(statusId).child(FireManager.uid).setValueRx(update).doOnComplete {
            RealmHelper.getInstance().setStatusSeenSent(statusId)
//          Log.e("seen count" + RealmHelpertatus.getSeenCount()+" ");
        }
    }

    fun deleteStatus(statusId: String, statusType: Int): Completable {
        return FireConstants.getMyStatusRef(statusType).child(statusId).setValueRx(null).doOnComplete {
            RealmHelper.getInstance().deleteStatus(FireManager.uid, statusId)
        }
    }

    fun deleteStatuses(statuses: List<Status>): Completable {
        return fromIterable(statuses).flatMapCompletable { status ->
            return@flatMapCompletable deleteStatus(status.statusId, status.type)
        }
    }

    fun uploadStatus(filePath: String, statusType: Int, isVideo: Boolean): Single<Status> {
        Log.e("uploading status","upload");
        val fileName = Util.getFileNameFromPath(filePath)
        val status: Status = if (isVideo) StatusCreator.createVideoStatus(filePath) else StatusCreator.createImageStatus(filePath)

        return FireManager.getRef(FireManager.STATUS_TYPE, fileName).putFileRx(Uri.fromFile(File(filePath))).flatMap { uploadTask ->

            if (isVideo) {
                val filePathBucket = uploadTask.storage.path
                status.content = filePathBucket
                return@flatMap FireConstants.getMyStatusRef(statusType).child(status.statusId).updateChildrenRx(status.toMap() as Map<String, Any>).toSingle<Any> {}.map { status }
            } else {
                return@flatMap uploadTask.storage.getDownloadUrlRx()
                        .flatMapSingle { downloadUrl ->
                            status.content = downloadUrl.toString()
                            return@flatMapSingle FireConstants.getMyStatusRef(statusType).child(status.statusId).updateChildrenRx(status.toMap() as Map<String, Any>).toSingle<Any> {}.map { status }
                        }
            }
        }.doOnSuccess { status ->
            RealmHelper.getInstance().saveStatus(FireManager.uid, status)
            DeleteStatusJob.schedule(status.userId, status.statusId)
           Log.e("job done","fine");


        }
    }


    fun uploadTextStatus(textStatus: TextStatus): Completable {
        val status = StatusCreator.createTextStatus(textStatus)
        return FireConstants.getMyStatusRef(StatusType.TEXT).child(status.statusId).updateChildrenRx(status.toMap() as MutableMap<String, Any>).doOnComplete {
            RealmHelper.getInstance().saveStatus(FireManager.uid, status)
            DeleteStatusJob.schedule(status.userId, status.statusId)
        }
    }

    fun getStatusSeenByList(statusId: String): Observable<Pair<String,MutableList<StatusSeenBy>>> {
        val reference = FireConstants.statusSeenUidsRef.child(FireManager.uid).child(statusId)
        return reference.observeSingleValueEvent().flatMapObservable { dataSnapshot ->
            if (dataSnapshot.hasChildren().not())
                return@flatMapObservable Observable.empty<Pair<MutableList<User>, DataSnapshot>>()


            val usersIds = dataSnapshot.children.map { it.key }.filterNotNull()
            return@flatMapObservable UserByIdsDataSource.getUsersByIds(usersIds).map { Pair(it, dataSnapshot) }
        }.map { pair ->
            val users = pair.first
            val dataSnapshot = pair.second

            val seenBy = mutableListOf<StatusSeenBy>()

            for (child in dataSnapshot.children) {

                val uid = child.key ?: ""
                val seenAt = child.child("seenAt").value as? Long ?: 0
                val foundUser = users.firstOrNull { it.uid == uid }
                foundUser?.let { user ->
                    seenBy.add(StatusSeenBy(user, seenAt))
                    Log.e("kotlin statusmanger", user.uid)

                }
            }
            return@map Pair(statusId,seenBy)
        }.doOnNext {
            RealmHelper.getInstance().saveSeenByList(statusId,it.second)
        }

    }


}