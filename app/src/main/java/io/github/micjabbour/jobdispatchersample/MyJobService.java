package io.github.micjabbour.jobdispatchersample;

import android.content.Context;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

/**
 * Created by Mike on 19/08/2017.
 */

public class MyJobService extends JobService {
    private final static String MY_JOBSERVICE_LOG_TAG = "MJS";
    private final static String MY_JOBSERVICE_TAG = "MJS_TAG";

    public static void scheduleJob(Context context) {
        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        //schedule the job using our parameters
        Job job = dispatcher.newJobBuilder()
                .setService(MyJobService.class) // the JobService that will be called
                .setTag(MY_JOBSERVICE_TAG)      //uniquely identifies the job
                .setRecurring(true)             //a recurring job
                .setLifetime(Lifetime.FOREVER) //persist job past device reboots
                .setTrigger(Trigger.executionWindow(20, 40)) //anywhere between 20 and 40 seconds
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setConstraints(Constraint.ON_ANY_NETWORK) //require an internet connection
                .build();
        dispatcher.mustSchedule(job);
    }

    @Override
    public boolean onStartJob(final JobParameters job) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(MY_JOBSERVICE_LOG_TAG, "running task. . .");
                try {
                    //a dummy task that takes 5 seconds to finish
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(MY_JOBSERVICE_LOG_TAG, "task finished");
                //finish job, to release wake lock
                jobFinished(job, false); //no need to reschedule
            }
        }).start();
        return true; //the job is still running in the background
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false; //this job ended successfully, no need to retry
    }
}
