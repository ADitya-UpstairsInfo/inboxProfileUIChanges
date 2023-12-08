package com.yearlater.inboxmessenger.job;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.yearlater.inboxmessenger.utils.RealmBackupRestore;
import com.evernote.android.job.DailyJob;
import com.evernote.android.job.JobRequest;
import com.yearlater.inboxmessenger.utils.RealmBackupRestore;

import java.util.concurrent.TimeUnit;

public class DailyBackupJob extends DailyJob {
    public static void schedule(Context context) {
        //this will schedule a job to backup messages everyday between 2,3 AM
        try {
            DailyJob.schedule(new JobRequest.Builder(
                            JobIds.JOB_TAG_BACKUP_MESSAGES),
                    TimeUnit.HOURS.toMillis(2),
                    TimeUnit.HOURS.toMillis(3)
            );
        }catch (NullPointerException nullPointerException){
            Toast.makeText(context,"Unable to create backup job",Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    protected DailyJobResult onRunDailyJob(@NonNull Params params) {
        new RealmBackupRestore(null).backup();
        return DailyJobResult.SUCCESS;
    }
}
