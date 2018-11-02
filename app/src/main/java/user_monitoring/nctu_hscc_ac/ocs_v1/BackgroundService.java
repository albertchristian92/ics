package user_monitoring.nctu_hscc_ac.ocs_v1;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import labelingStudy.nctu.minuku.config.Constants;
import labelingStudy.nctu.minuku.manager.MinukuStreamManager;
import labelingStudy.nctu.minuku.manager.SessionManager;

/**
 * Created by AC on 7/17/2018.
 */

public class BackgroundService extends Service {
    private int ongoingNotificationID = 42;
    private String ongoingNotificationText = Constants.RUNNING_APP_DECLARATION;
    final static String CONNECTIVITY_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    NotificationManager mNotificationManager;
    MinukuStreamManager streamManager;
    private ScheduledExecutorService mScheduledExecutorService;
    WifiReceiver mWifiReceiver;
    IntentFilter intentFilter;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("Test","Background Service activate");
        intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        mWifiReceiver = new WifiReceiver();
        mWifiReceiver.setContext(this); //  bener ko hrus e gni cba run dlu ae
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        streamManager = MinukuStreamManager.getInstance();
        mScheduledExecutorService = Executors.newScheduledThreadPool(Constants.NOTIFICATION_UPDATE_THREAD_SIZE);
        registerReceiver(mWifiReceiver, intentFilter);

        //building the ongoing notification to the foreground
        startForeground(ongoingNotificationID, getOngoingNotification(ongoingNotificationText));
        if(!InstanceManager.isInitialized()) {

            InstanceManager.getInstance(this);
            SessionManager.getInstance(this);

            updateNotificationAndStreamManagerThread();
        }

        return START_REDELIVER_INTENT; //START_STICKY_COMPATIBILITY;
    }
    private Notification getOngoingNotification(String text){

        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle(Constants.APP_FULL_NAME);
        bigTextStyle.bigText(text);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent pending = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder noti = new Notification.Builder(this);

        return noti.setContentTitle(Constants.APP_FULL_NAME)
                .setContentText(text)
                .setStyle(bigTextStyle)
                .setContentIntent(pending)
                .setAutoCancel(false)
                .setOngoing(true)
                .build();
    }

    private void updateNotificationAndStreamManagerThread(){

        mScheduledExecutorService.scheduleAtFixedRate(
                updateStreamManagerRunnable,
                60,
                Constants.STREAM_UPDATE_FREQUENCY,
                TimeUnit.SECONDS);
    }

    Runnable updateStreamManagerRunnable = new Runnable() {
        @Override
        public void run() {

            try {

                streamManager.updateStreamGenerators();
            }catch (Exception e){

            }
        }
    };
}
